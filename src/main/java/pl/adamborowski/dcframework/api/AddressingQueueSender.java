package pl.adamborowski.dcframework.api;

import lombok.RequiredArgsConstructor;
import pl.adamborowski.dcframework.comm.TaskQueueNameResolver;
import pl.adamborowski.dcframework.comm.data.TaskComputedTO;

import javax.jms.JMSException;
import javax.jms.Session;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class AddressingQueueSender {

    private final Map<String, ActiveMQSender> sendersByName = new HashMap<>();
    private final Session session;

    private ActiveMQSender getSender(final String name) throws JMSException {
        if (!sendersByName.containsKey(name)) {
            sendersByName.put(name, new ActiveMQSender(session, name));
        }
        return sendersByName.get(name);
    }

    public void send(final TaskComputedTO transfer) throws JMSException {
        getSender(TaskQueueNameResolver.getQueueNameFor(transfer.getGlobalId().getNodeId())).send(transfer);
    }

}
