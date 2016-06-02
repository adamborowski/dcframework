package pl.adamborowski.dcframework.comm;

import com.google.common.base.Throwables;
import lombok.RequiredArgsConstructor;
import pl.adamborowski.dcframework.LocalQueue;
import pl.adamborowski.dcframework.Task;

import javax.jms.JMSException;
import java.util.Collection;

/**
 * Advanced local queue which
 * on add it resends a task which nativeId!=nodeId and is computed with result
 * on add it sends a task to global queue if local queue size is over a push threshold and given task awaiting and is divideable
 * has a maintenance thread which fetches tasks from global queue occasionally when number of tasks is below a pull threshold
 * has a maintenance thread which fetches tasks from node queue to get all sended tasks back to merge with parent
 *
 * @param <Params>
 * @param <Result>
 */
@RequiredArgsConstructor
public class SharingLocalQueue<Params, Result> implements LocalQueue<Params, Result> {
    private final LocalQueue<Params, Result> localQueue;
    private final RemoteTransferManager transferManager;
    private final int maxThreshold;
    private final float randomThreshold;

    @Override
    public void addAll(Collection<Task<Params, Result>> tasks) {
        for (Task<Params, Result> task : tasks) {
            add(task);
        }
    }

    @Override
    public void add(Task<Params, Result> task) {
        try {
            if (task.isDelegate()) {
                assert task.inState(Task.State.COMPUTED);
                transferManager.localDelegateToOwningRemote(task);
            } else {
                if (task.inState(Task.State.COMPUTED)) {
                    localQueue.add(task);
                } else {
                    if (shouldBeSmaller()) {
                        if (randomYes()) {
                            transferManager.localNativeToDelegateRemote(task);
                        } else {
                            localQueue.add(task);
                        }
                    } else {
                        localQueue.add(task);
                    }
                }
            }
        } catch (JMSException e) {
            Throwables.propagate(e);
        }
    }

    private boolean randomYes() {
        return false;
//        return Math.random() > randomThreshold;
    }


    private boolean shouldBeSmaller() {
        return true;
//        return size() > maxThreshold;
    }

    @Override
    public void drainTo(Collection<Task<Params, Result>> collection, int numTasks) throws InterruptedException {
        localQueue.drainTo(collection, numTasks);
    }

    @Override
    public int getMaxCount() {
        return localQueue.getMaxCount();
    }

    @Override
    public int size() {
        return localQueue.size();
    }
}
