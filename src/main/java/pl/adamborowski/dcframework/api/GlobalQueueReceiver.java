package pl.adamborowski.dcframework.api;

import org.apache.log4j.Logger;
import pl.adamborowski.dcframework.comm.RemoteTransferManager;
import pl.adamborowski.dcframework.comm.TaskQueueNameResolver;
import pl.adamborowski.dcframework.comm.data.TaskToComputeTO;

import javax.jms.JMSException;
import javax.jms.Session;

public class GlobalQueueReceiver {

    private final RemoteTransferManager transferManager;
    private final Session session;
    private final ActiveMQReceiver receiver;
    private final Logger log = Logger.getLogger(GlobalQueueReceiver.class);

    public GlobalQueueReceiver(Session session, RemoteTransferManager transferManager) throws JMSException {
        this.session = session;
        this.transferManager = transferManager;
        receiver = new ActiveMQReceiver(session, TaskQueueNameResolver.getGlobalQueueName());
    }

    public TaskToComputeTO receiveTask(long timeout) throws JMSException {
        TaskToComputeTO transfer = (TaskToComputeTO) receiver.receive(timeout);
        return getTaskToComputeTO(transfer);
    }

    public TaskToComputeTO receiveTaskNoWait() throws JMSException {
        TaskToComputeTO transfer = (TaskToComputeTO) receiver.receiveNoWait();
        return getTaskToComputeTO(transfer);
    }

    public TaskToComputeTO getTaskToComputeTO(TaskToComputeTO transfer) {
        if (transfer == null) {
            log.trace("Could not get any task from global queue - propably is empty");
            return null;
        } else {
            transferManager.remoteToLocal(transfer);
            return transfer;
        }
    }
}
