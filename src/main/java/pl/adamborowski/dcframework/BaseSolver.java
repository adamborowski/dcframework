package pl.adamborowski.dcframework;

import com.google.common.base.Throwables;
import org.apache.activemq.ActiveMQConnectionFactory;
import pl.adamborowski.dcframework.api.AddressingQueueSender;
import pl.adamborowski.dcframework.api.GlobalQueueReceiver;
import pl.adamborowski.dcframework.api.GlobalQueueSender;
import pl.adamborowski.dcframework.api.OwningQueueReceiver;
import pl.adamborowski.dcframework.comm.LocalQueueSupplier;
import pl.adamborowski.dcframework.comm.RemoteTransferManager;
import pl.adamborowski.dcframework.comm.SharingLocalQueue;
import pl.adamborowski.dcframework.comm.TaskCache;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

public class BaseSolver<Params, Result> extends Solver<Params, Result> {

    private SharingLocalQueue<Params, Result> sharingLocalQueue;
    private TaskFactory<Params, Result> taskFactory;
    private int randomThreshold;
    private int maxThreshold;
    private int minThreshold;
    private long supplierInterval;

    @Override
    protected AbstractWorker createWorker() {
        return new Worker();
    }


    @Override
    protected void init() {
        try {
            initBroker();
        } catch (JMSException e) {
            log.error("Error broker initialization:", e);
            Throwables.propagate(e);
        }
        performInitialTask();
    }

    private void initBroker() throws JMSException {
        taskFactory = new SimpleTaskFactory<>(nodeId);

        final LocalQueue localQueue = new SimpleLocalQueue<>();

        final ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        final Connection connection = factory.createConnection();
        connection.start();
        final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);


        final TaskCache cache = new TaskCache();
        final GlobalQueueSender globalSender = new GlobalQueueSender(session);
        final AddressingQueueSender addressingSender = new AddressingQueueSender(session);
        final RemoteTransferManager transferManager = new RemoteTransferManager(nodeId, cache, localQueue, globalSender, addressingSender, taskFactory);
        final GlobalQueueReceiver globalReceiver = new GlobalQueueReceiver(session, transferManager);
        final OwningQueueReceiver owningQueueReceiver = new OwningQueueReceiver(session, transferManager, nodeId);
        final LocalQueueSupplier supplier = new LocalQueueSupplier(localQueue, globalReceiver, supplierInterval, minThreshold);


        sharingLocalQueue = new SharingLocalQueue<>(localQueue, transferManager, maxThreshold, randomThreshold);
    }

    private void performInitialTask() {
        if (nodeId == 0) {
            Task<Params, Result> rootTask = taskFactory.createRootTask(initialParams);
            sharingLocalQueue.add(rootTask);
        } else {
            // slave will wait to queue become not empty
        }
    }

    @Override
    public void finish() {
        log.info("Max local queue count = " + sharingLocalQueue.getMaxCount());
    }

    private class Worker extends AbstractWorker {

        Vector<Task<Params, Result>> output;
        Vector<Task<Params, Result>> input;
        List<Task> processedTasks = new LinkedList<>();

        @Override
        public void init() {
            log.info(Thread.currentThread().getName() + " started.");
            input = new Vector<>(batchSize);
            output = new Vector<>(batchSize);
        }

        @Override
        public void step() throws InterruptedException {
            output.clear();
            input.clear();
            log.debug("Step");
            sharingLocalQueue.drainTo(input, batchSize);
            log.debug("Got " + input.size() + " elements.");

            for (Task<Params, Result> task : input) {
                synchronized (task) {
                    processedTasks.add(task);
                    if (task.isRootTask() && task.inState(Task.State.COMPUTED)) {

                        //this->output(common + "root task = " + to_string(task->result));
                        task.setState(Task.State.DEAD);
                        complete(task.getResult());
                        //this is the root node = we just have the final result
                    } else if (task.inState(Task.State.AWAITING)) {
                        //this node need's calculation or has to be divided
                        if (problem.testDivide(task.getParams())) {
                            //this->output(common + "divide");
                            // divide task into smaller tasks and push to queue
                            final Problem.DividedParams<Params> dividedParams = problem.divide(task.getParams());
                            final Task<Params, Result> leftTask = taskFactory.createChildTask(dividedParams.leftParams, true);
                            final Task<Params, Result> rightTask = taskFactory.createChildTask(dividedParams.rightParams, false);

                            leftTask.wire(task, rightTask);
                            rightTask.wire(task, leftTask);

                            output.add(leftTask);
                            output.add(rightTask);
                        } else {
                            task.setComputed(problem.compute(task.getParams()), nodeId);
                            output.add(task);
                        }
                    } else if (task.inState(Task.State.COMPUTED)) {
                        if (task.isLeft()) {
                            if (task.readyToMerge()) {
                                synchronized (task.getBrother()) {
                                    final Task<Params, Result> parent = task.getParent();
                                    Result leftResult = task.getResult();
                                    Result rightResult = task.getBrother().getResult();
                                    if (rightResult == null) {
                                        System.out.println("FAIL, result=null");
                                        System.exit(111);
                                    }
                                    log.debug(leftResult.toString() + ", " + rightResult.toString());
                                    parent.setComputed(problem.merge(leftResult, rightResult), nodeId);
                                    task.markAsDead();
                                    task.getBrother().markAsDead();
                                    output.add(parent);
                                }
                            } else {
                                output.add(task);
                            }
                        } else {
                            log.debug("Right brother computed but it doesn't manage merging.");
                        }
                    } else {
                        assert task.inState(Task.State.DEAD);
                    }
                }
            }
            sharingLocalQueue.addAll(output);
        }

        @Override
        public void finish() {
        }
    }
}
