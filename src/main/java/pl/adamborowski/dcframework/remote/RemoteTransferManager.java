package pl.adamborowski.dcframework.remote;

import lombok.RequiredArgsConstructor;
import org.apache.log4j.Logger;
import pl.adamborowski.dcframework.node.LocalQueue;
import pl.adamborowski.dcframework.node.Task;
import pl.adamborowski.dcframework.node.TaskFactory;
import pl.adamborowski.dcframework.remote.comm.AddressingQueueSender;
import pl.adamborowski.dcframework.remote.comm.GlobalQueueSender;
import pl.adamborowski.dcframework.remote.data.TaskComputedTO;
import pl.adamborowski.dcframework.remote.data.TaskToComputeTO;

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
    private final boolean optimizeShortReturn;
    private final Logger log = Logger.getLogger(RemoteTransferManager.class);

    /**
     * Called by GlobalQueueReceiver when a task to compute was received.
     *
     * @param transfer a task parameters to compute locally
     */
    public void remoteToLocal(TaskToComputeTO transfer) {
        log.debug(String.format("Received remote to local %s", transfer));
        if (transfer.getGlobalId().getNodeId() == nodeId && optimizeShortReturn) {
            // this is the same node, treat as own, (nobody helped me)
            localQueue.add(cache.retrieve(transfer.getGlobalId()));
        } else {
            localQueue.add(taskFactory.createDelegatingTask(transfer.getParams(), transfer.getGlobalId()));
        }
    }

    /**
     * Called by OwningQueueReceiver when remote result received
     *
     * @param transfer a result received from external unit, computed and ready to process (merge phase) locally
     */
    @SuppressWarnings("unchecked")
    public void remoteToLocal(TaskComputedTO transfer) {
        log.debug(String.format("Received remote to local %s", transfer));
        final Task parkedTask = cache.retrieve(transfer.getGlobalId());
        if (parkedTask == null) {
            log.error("Cannot find parked task for transfer " + transfer.toString());
            System.exit(432);
        }
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
        log.debug(String.format("Send local delegate %s to owning remote %s", task, transfer));
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
        log.debug(String.format("Send local native %s to delegate remote %s", task, transfer));
        globalSender.send(transfer);
    }

    /**
     * Called by SharedQueue to put native task to addressing queue for others to compute at initial state
     *
     * @param task
     * @param slaveId the id of slave to send
     * @throws JMSException
     */
    public void initialLocalNativeToDelegateRemote(Task task, int slaveId) throws JMSException {
        assert !task.isDelegate();
        assert task.inState(Task.State.AWAITING);
        cache.park(task);
        TaskToComputeTO transfer = new TaskToComputeTO(task.getGlobalId(), task.getParams());
        log.debug(String.format("Send initial local native %s to delegate remote %s for slave %d", task, transfer, slaveId));
        addressingSender.initialSend(transfer, slaveId);
    }


}
