package pl.adamborowski.dcframework.remote.comm;

import lombok.Getter;
import pl.adamborowski.dcframework.remote.TaskQueueNameResolver;
import pl.adamborowski.dcframework.remote.data.TaskToComputeTO;
import pl.adamborowski.dcframework.util.ActiveMQSender;

import javax.jms.JMSException;
import javax.jms.Session;

public class GlobalQueueSender {
    @Getter
    private int counter = 0;
    public GlobalQueueSender(Session session) throws JMSException {

        this.session = session;
        this.sender = ActiveMQSender.forQueue(session, TaskQueueNameResolver.getGlobalQueueName());
    }

    private final Session session;
    private final ActiveMQSender sender;

    public void send(final TaskToComputeTO transfer) throws JMSException {
        counter++;
        sender.send(transfer);
    }
}
