package pl.adamborowski.dcframework;

import lombok.Getter;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

public class SimpleLocalQueue<Params, Result> implements LocalQueue<Params, Result> {


    private ReentrantLock writeLock = new ReentrantLock();

    public static final int INITIAL_QUEUE_CAPACITY = 1000;

    @Getter
    private int maxCount = 0;

    //create manager thread for this queue to occasionally check if we should move some tasks out of or into the queue to activemq queue
    final BlockingQueue<Task<Params, Result>> queue = new LinkedBlockingQueue<>();

    @Override
    public void addAll(Collection<Task<Params, Result>> collection) {
        writeLock.lock();
        queue.addAll(collection);
        maxCount = Math.max(maxCount, queue.size());
        writeLock.unlock();
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
        log.trace("Queue size after drain: " + queue.size());
    }
}
