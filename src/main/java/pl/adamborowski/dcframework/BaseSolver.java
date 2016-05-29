package pl.adamborowski.dcframework;

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
        Task<Params, Result> rootTask = taskFactory.createTask();
        rootTask.setup(null, null, initialParams);
        rootTask.setRootTask(true);
        localQueue.add(rootTask);
    }


    private class Worker extends AbstractWorker {

        Vector<Task<Params, Result>> output;
        Vector<Task<Params, Result>> input;

        @Override
        public void init() {
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
                if (task.inState(Task.State.DEAD)) {
                    // the second node from merge
                    task.setState(Task.State.DEAD);
                } else if (task.rootTask && task.inState(Task.State.COMPUTED)) {

                    //this->output(common + "root task = " + to_string(task->result));
                    task.setState(Task.State.DEAD);
                    complete(task.getResult());
                    //this is the root node = we just have the final result
                } else if (task.inState(Task.State.AWAITING)) {
                    //this node need's calculation or has to be divided
                    if (problem.testDivide(task.params)) {
                        //this->output(common + "divide");
                        // divide task into smaller tasks and push to queue
                        final Problem.DividedParams<Params> dividedParams = problem.divide(task.params);
                        final Task<Params, Result> leftTask = taskFactory.createTask();
                        final Task<Params, Result> rightTask = taskFactory.createTask();
                        leftTask.setup(task, rightTask, dividedParams.leftParams);
                        rightTask.setup(task, leftTask, dividedParams.rightParams);

                        output.add(leftTask);
                        output.add(rightTask);
                    } else {
                        task.setComputed(problem.compute(task.params));
                        output.add(task);
                    }
                } else if (task.readyToMerge()) {
                    final Task<Params, Result> parent = task.getParent();
                    parent.setComputed(problem.merge(task.getResult(), task.getBrother().getResult()));
                    task.markAsDead();
                    task.brother.markAsDead();
                    output.add(parent);
                } else {
                    // task have been not processed (i.e. brother is remote) - should be returned
                    output.add(task);
                    assert !task.inState(Task.State.DEAD);
                }
            }
            localQueue.addAll(output);
        }

        @Override
        public void finish() {

        }
    }
}
