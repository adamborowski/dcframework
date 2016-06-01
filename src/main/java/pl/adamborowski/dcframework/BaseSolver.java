package pl.adamborowski.dcframework;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

public class BaseSolver<Params, Result> extends Solver<Params, Result> {

    private LocalQueue<Params, Result> localQueue;
    private TaskFactory<Params, Result> taskFactory;

    @Override
    protected AbstractWorker createWorker() {
        return new Worker();
    }


    @Override
    protected void init() {
        taskFactory = new SimpleTaskFactory<>(nodeId);
        localQueue = new SimpleLocalQueue<>();

        if (nodeId == 0) {
            Task<Params, Result> rootTask = taskFactory.createRootTask(initialParams);
            localQueue.add(rootTask);
        } else {
            // slave will wait to queue become not empty
        }
    }

    @Override
    public void finish() {
        log.info("Max local queue count = " + localQueue.getMaxCount());
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
            localQueue.drainTo(input, batchSize);
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
            localQueue.addAll(output);
        }

        @Override
        public void finish() {
        }
    }
}
