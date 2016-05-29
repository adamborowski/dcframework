package pl.adamborowski.dcframework;

import com.google.common.base.Throwables;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public abstract class AbstractSolver<Params, Result> {

    protected Problem<Params, Result> problem;
    private int numThreads;
    protected int nodeId;
    protected int batchSize = 1;

    public void setup(Problem<Params, Result> problem, int numThreads, int nodeId, int batchSize) {
        this.problem = problem;
        this.numThreads = numThreads;
        this.nodeId = nodeId;
        this.batchSize = batchSize;
    }

    @Getter
    private boolean working = true;
    private Result result;
    protected Params initialParams;

    private List<Thread> workers = new LinkedList<>();

    protected void complete(Result result) {
        this.result = result;
        working = false;
        for (Thread worker : workers) {
            worker.interrupt();
        }
    }


    protected abstract AbstractWorker createWorker();

    public Result process(Params params) {
        initialParams = params;
        init();
        CountDownLatch latch = new CountDownLatch(numThreads);
        for (int i = 0; i < numThreads; i++) {
            AbstractWorker worker = createWorker();
            worker.setCountDownLatch(latch);
            Thread thread = new Thread(worker, "Worker " + i);
            thread.start();
            workers.add(thread);
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            Throwables.propagate(e);
            //todo check if his will be fired if worker will be interrupped because another worker will set the result
            //todo here will be infinity blocked at queue.pick...
        }
        return result;

    }

    protected abstract void init();

    abstract class AbstractWorker implements Runnable {
        @Setter
        private CountDownLatch countDownLatch;


        @Override
        public void run() {
            init();
            while (working) {
                try {
                    step();
                } catch (InterruptedException e) {
//                    Throwables.propagate(e);
                    // TODO: 29.05.2016 check if this case always means that another thread finished the job and there is no point to wait for a task
                }
            }
            finish();
            countDownLatch.countDown();
        }

        public abstract void init();

        public abstract void step() throws InterruptedException;

        public abstract void finish();
    }
}
