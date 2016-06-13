package pl.adamborowski.dcframework.remote.comm;

import pl.adamborowski.dcframework.remote.RemoteTransferManager;
import pl.adamborowski.dcframework.remote.TaskQueueNameResolver;
import pl.adamborowski.dcframework.remote.data.TaskComputedTO;
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

    public OwningQueueReceiver(Session session, RemoteTransferManager transferManager, int nodeId) throws JMSException {
        this.session = session;
        this.transferManager = transferManager;
        this.nodeId = nodeId;
        receiver = new ActiveMQListener(session, TaskQueueNameResolver.getQueueNameFor(nodeId));
        receiver.setTransferListener(transfer -> transferManager.remoteToLocal((TaskComputedTO) transfer));
    }
}