package pl.adamborowski.dcframework.remote.comm;

import lombok.Getter;
import pl.adamborowski.dcframework.remote.RemoteTransferManager;
import pl.adamborowski.dcframework.remote.TaskQueueNameResolver;
import pl.adamborowski.dcframework.remote.data.TaskComputedTO;
import pl.adamborowski.dcframework.remote.data.TaskToComputeTO;
import pl.adamborowski.dcframework.util.ActiveMQListener;

import javax.jms.JMSException;
import javax.jms.Session;

/**
 * This receiver asynchronously receives every task which is completed and computed by others
 */
public class OwningQueueReceiver {

    private final RemoteTransferManager transferManager;
    private final Session session;
    private final ActiveMQListener receiver;
    private final int nodeId;
    @Getter
    private int counter = 0;

    public OwningQueueReceiver(Session session, RemoteTransferManager transferManager, int nodeId) throws JMSException {
        this.session = session;
        this.transferManager = transferManager;
        this.nodeId = nodeId;
        receiver = new ActiveMQListener(session, TaskQueueNameResolver.getQueueNameFor(nodeId));
        receiver.setTransferListener(transfer -> {
            counter++;
            if (transfer instanceof TaskComputedTO) {
                // this is for computed tasks by others, returned to the author
                transferManager.remoteToLocal((TaskComputedTO) transfer);
            } else {
                // this case is for initial distribution only, received from master
                transferManager.remoteToLocal((TaskToComputeTO) transfer);
            }

        });
    }
}