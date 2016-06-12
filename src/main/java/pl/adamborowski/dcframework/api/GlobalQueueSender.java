package pl.adamborowski.dcframework.api;

import pl.adamborowski.dcframework.comm.TaskQueueNameResolver;
import pl.adamborowski.dcframework.comm.data.TaskToComputeTO;

import javax.jms.JMSException;
import javax.jms.Session;

public class GlobalQueueSender {
    public GlobalQueueSender(Session session) throws JMSException {

        this.session = session;
        this.sender = ActiveMQSender.forQueue(session, TaskQueueNameResolver.getGlobalQueueName());
    }

    private final Session session;
    private final ActiveMQSender sender;

    public void send(final TaskToComputeTO transfer) throws JMSException {
        sender.send(transfer);
    }
}
