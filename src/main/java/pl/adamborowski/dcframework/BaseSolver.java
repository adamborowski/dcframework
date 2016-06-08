package pl.adamborowski.dcframework;

import com.google.common.base.Throwables;
import lombok.Setter;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQObjectMessage;
import pl.adamborowski.dcframework.api.AddressingQueueSender;
import pl.adamborowski.dcframework.api.GlobalQueueReceiver;
import pl.adamborowski.dcframework.api.GlobalQueueSender;
import pl.adamborowski.dcframework.api.OwningQueueReceiver;
import pl.adamborowski.dcframework.comm.LocalQueueSupplier;
import pl.adamborowski.dcframework.comm.RemoteTransferManager;
import pl.adamborowski.dcframework.comm.SharingLocalQueue;
import pl.adamborowski.dcframework.comm.TaskCache;
import pl.adamborowski.dcframework.comm.data.ShutdownMessage;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;
import java.io.Serializable;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

public class BaseSolver<Params extends Serializable, Result extends Serializable> extends Solver<Params, Result> {

    private SharingLocalQueue<Params, Result> sharingLocalQueue;
    private TaskFactory<Params, Result> taskFactory;
    @Setter
    private float randomThreshold = 0.5f;
    @Setter
    private int maxThreshold = 700;
    @Setter
    private int minThreshold = 300;
    @Setter
    private long supplierInterval = 1000;
    @Setter
    private long supplierIntervalSubsequent = 50;
    @Setter
    private String connectionUrl;
    private LocalQueueSupplier supplier;
    private Connection connection;
    private Session syncSesssion;

    @Override
    protected AbstractWorker createWorker() {
        return new Worker();
    }

    AtomicInteger taskCounter = new AtomicInteger(0);

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

        final ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(connectionUrl);
        connection = factory.createConnection();
        connection.start();
        syncSesssion = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        final Session asyncSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);


        final TaskCache cache = new TaskCache();
        final GlobalQueueSender globalSender = new GlobalQueueSender(syncSesssion);
        final AddressingQueueSender addressingSender = new AddressingQueueSender(syncSesssion);
        final RemoteTransferManager transferManager = new RemoteTransferManager(nodeId, cache, localQueue, globalSender, addressingSender, taskFactory);
        final GlobalQueueReceiver globalReceiver = new GlobalQueueReceiver(asyncSession, transferManager);
        final OwningQueueReceiver owningQueueReceiver = new OwningQueueReceiver(syncSesssion, transferManager, nodeId);
        supplier = new LocalQueueSupplier(localQueue, globalReceiver, supplierInterval, supplierIntervalSubsequent, minThreshold);
        supplier.start();


        sharingLocalQueue = new SharingLocalQueue<>(localQueue, transferManager, maxThreshold, randomThreshold);

        if (nodeId != 0) {
            Topic finish = asyncSession.createTopic("finish");
            syncSesssion.createConsumer(finish).setMessageListener(message -> {
                try {
                    log.info("Got FINISH signal with result from the master");
                    ShutdownMessage<Result> message1 = (ShutdownMessage<Result>) ((ObjectMessage) message).getObject();
                    this.complete(message1.getResult());
                } catch (JMSException e) {
                    Throwables.propagate(e);
                }
            });
        }
    }

    private void performInitialTask() {
        if (nodeId == 0) {
            Task<Params, Result> rootTask = taskFactory.createRootTask(initialParams);
            log.info("Created root task:" + rootTask.toString());
            sharingLocalQueue.add(rootTask);
        } else {
            // slave will wait to queue become not empty
        }
    }

    @Override
    public void finish(Result result) {
        log.info("Node " + nodeId + " processed " + taskCounter + " tasks");

        supplier.stop();
        if (nodeId == 0) {
            try {
                Topic finish = syncSesssion.createTopic("finish");

                ActiveMQObjectMessage message = new ActiveMQObjectMessage();
                message.setObject(new ShutdownMessage<>(result));
                syncSesssion.createProducer(finish).send(message);

            } catch (JMSException e) {
                Throwables.propagate(e);
            }
        }
        try {
            connection.close();
            log.info("SharingLocalQueue ActiveMQ stopped");
        } catch (JMSException e) {
            Throwables.propagate(e);
        }
    }

    private class Worker extends AbstractWorker {

        Vector<Task<Params, Result>> output;
        Vector<Task<Params, Result>> input;

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
//            log.debug("Step");
            sharingLocalQueue.drainTo(input, batchSize);
//            log.debug("Got " + input.size() + " elements.");

            for (Task<Params, Result> task : input) {
                taskCounter.incrementAndGet();
                synchronized (task) {
                    log.debug("Processing task" + task);
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
                            log.debug("Computing " + task.toString());
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
                                log.debug(String.format("Task %s is not ready to merge", task));
                                sharingLocalQueue.putForLater(task);
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
