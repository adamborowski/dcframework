package pl.adamborowski.dcframework;

import lombok.Getter;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class SimpleLocalQueue<Params, Result> implements LocalQueue<Params, Result> {

    private ReentrantLock writeLock = new ReentrantLock();
    private AtomicInteger currentSize = new AtomicInteger(0);

    @Getter
    private int maxCount = 0;

    //create manager thread for this queue to occasionally check if we should move some tasks out of or into the queue to activemq queue
    final BlockingQueue<Task<Params, Result>> queue = new LinkedBlockingQueue<>();

    @Override
    public void addAll(Collection<Task<Params, Result>> collection) {
        writeLock.lock();
        log.trace(String.format("adding tasks %s to collection of size %s", collection, size()));
        queue.addAll(collection);
        currentSize.addAndGet(collection.size());
        maxCount = Math.max(maxCount, size());
        writeLock.unlock();
    }

    @Override
    public void add(Task<Params, Result> task) {
        log.trace(String.format("adding task %s to collection of size %s", task, size()));
        queue.add(task);
        currentSize.incrementAndGet();
    }

    @Override
    public void drainTo(Collection<Task<Params, Result>> collection, int numTasks) throws InterruptedException {
        final int initialCollectionSize = collection.size();
        queue.drainTo(collection, numTasks);
        if (collection.isEmpty()) {
            collection.add(queue.take());
        }
        currentSize.addAndGet(-(collection.size() - initialCollectionSize));
        log.trace("Queue size after drain: " + size());
    }

    @Override
    public int size() {
        return currentSize.get();
    }
}
