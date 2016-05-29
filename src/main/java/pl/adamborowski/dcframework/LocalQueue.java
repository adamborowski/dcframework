package pl.adamborowski.dcframework;

import java.util.Collection;

/**
 * LocalQueue - manage all tasks available for one (for all threads in the JVM)
 * if there are too many (ex. >100) tasks - send to GlobalQueue (activemq)
 * if there is too low (ex. <20) tasks - try to receive from GlobalQueue(activemq)
 */
public interface LocalQueue<Params, Result> {
    /**
     * Blocking,
     * if there is too many tasks, send async to global queue
     *
     * @param tasks
     */
    void addAll(Collection<Task<Params, Result>> tasks);

    /**
     * Blocking, if there is any tasks in local queue - pick tasks
     * if there is no tasks in local queue - wait for some (put by other threads)
     * if there is too low tasks, run async global queue read
     *
     * @return
     */
    void drainTo(Collection<Task<Params, Result>> collection, int numTasks) throws InterruptedException;
}
