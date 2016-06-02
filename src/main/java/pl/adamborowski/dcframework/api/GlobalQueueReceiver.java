package pl.adamborowski.dcframework.api;

import pl.adamborowski.dcframework.comm.RemoteTransferManager;
import pl.adamborowski.dcframework.comm.TaskQueueNameResolver;
import pl.adamborowski.dcframework.comm.data.TaskToComputeTO;

import javax.jms.JMSException;
import javax.jms.Session;

public class GlobalQueueReceiver {

    private final RemoteTransferManager transferManager;
    private final Session session;
    private final ActiveMQReceiver receiver;

    public GlobalQueueReceiver(Session session, RemoteTransferManager transferManager) throws JMSException {
        this.session = session;
        this.transferManager = transferManager;
        receiver = new ActiveMQReceiver(session, TaskQueueNameResolver.getGlobalQueueName());
    }

    public void receiveTask(long timeout) throws JMSException {
        TaskToComputeTO transfer = (TaskToComputeTO) receiver.receive(timeout);
        transferManager.remoteToLocal(transfer);
    }
}
