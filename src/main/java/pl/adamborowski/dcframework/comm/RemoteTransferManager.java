package pl.adamborowski.dcframework.comm;

import lombok.RequiredArgsConstructor;
import pl.adamborowski.dcframework.LocalQueue;
import pl.adamborowski.dcframework.Task;
import pl.adamborowski.dcframework.TaskFactory;
import pl.adamborowski.dcframework.api.AddressingQueueSender;
import pl.adamborowski.dcframework.api.GlobalQueueSender;
import pl.adamborowski.dcframework.comm.data.TaskComputedTO;
import pl.adamborowski.dcframework.comm.data.TaskToComputeTO;

import javax.jms.JMSException;

/**
 * A class to send task data (without references to other tasks)
 */
@RequiredArgsConstructor()
public class RemoteTransferManager {
    private final int nodeId;
    private final TaskCache cache;
    private final LocalQueue localQueue;
    private final GlobalQueueSender globalSender;
    private final AddressingQueueSender addressingSender;
    private final TaskFactory taskFactory;

    /**
     * Called by GlobalQueueReceiver when a task to compute was received.
     *
     * @param transfer a task parameters to compute locally
     */
    public void remoteToLocal(TaskToComputeTO transfer) {
        final Task delegatedTask = taskFactory.createDelegatingTask(transfer.getParams(), transfer.getGlobalId());
        localQueue.add(delegatedTask);
    }

    /**
     * Called by OwningQueueReceiver when remote result received
     *
     * @param transfer a result received from external unit, computed and ready to process (merge phase) locally
     */
    @SuppressWarnings("unchecked")
    public void remoteToLocal(TaskComputedTO transfer) {
        final Task parkedTask = cache.retrieve(transfer.getGlobalId());
        parkedTask.setComputed(transfer.getResult(), transfer.getComputingNodeId());
        localQueue.add(parkedTask);

    }

    /**
     * Called by SharedQueue to return delegated task back to creator node.
     * @param task
     * @throws JMSException
     */

    public void localDelegateToOwningRemote(Task task) throws JMSException {
        assert task.isDelegate();
        assert task.inState(Task.State.COMPUTED);
        TaskComputedTO transfer = new TaskComputedTO(task.getGlobalId(), nodeId, task.getResult());
        addressingSender.send(transfer);
    }

    /**
     * Called by SharedQueue to put native task to global queue for others to compute
     * @param task
     * @throws JMSException
     */
    public void localNativeToDelegateRemote(Task task) throws JMSException {
        assert !task.isDelegate();
        assert task.inState(Task.State.AWAITING);
        cache.park(task);
        TaskToComputeTO transfer = new TaskToComputeTO(task.getGlobalId(), task.getParams());
        globalSender.send(transfer);
    }

    private String getQueueNameForNode(final int nodeId) {
        return "queue-" + nodeId;
    }

}
