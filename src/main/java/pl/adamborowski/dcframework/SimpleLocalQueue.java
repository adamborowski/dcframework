package pl.adamborowski.dcframework;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SimpleLocalQueue<Params, Result> implements LocalQueue<Params, Result> {

    public static final int INITIAL_QUEUE_CAPACITY = 1000;


    //create manager thread for this queue to occasionally check if we should move some tasks out of or into the queue to activemq queue
    final BlockingQueue<Task<Params, Result>> queue = new LinkedBlockingQueue<>(INITIAL_QUEUE_CAPACITY);


    @Override
    public void addAll(Collection<Task<Params, Result>> collection) {
        queue.addAll(collection);
    }

    @Override
    public void add(Task<Params, Result> task) {
        queue.add(task);
    }

    @Override
    public void drainTo(Collection<Task<Params, Result>> collection, int numTasks) throws InterruptedException {
        queue.drainTo(collection, numTasks);
        if (collection.isEmpty()) {
            collection.add(queue.take());
        }
    }
}
