package pl.adamborowski.dcframework.remote.comm;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.adamborowski.dcframework.remote.TaskQueueNameResolver;
import pl.adamborowski.dcframework.remote.data.TaskComputedTO;
import pl.adamborowski.dcframework.remote.data.TaskToComputeTO;
import pl.adamborowski.dcframework.util.ActiveMQSender;

import javax.jms.JMSException;
import javax.jms.Session;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class AddressingQueueSender {

    @Getter
    private int counter = 0;
    private final Map<String, ActiveMQSender> sendersByName = new HashMap<>();
    private final Session session;

    private ActiveMQSender getSender(final String name) throws JMSException {
        if (!sendersByName.containsKey(name)) {
            sendersByName.put(name, ActiveMQSender.forQueue(session, name));
        }
        return sendersByName.get(name);
    }

    public void send(final TaskComputedTO transfer) throws JMSException {
        counter++;
        getSender(TaskQueueNameResolver.getQueueNameFor(transfer.getGlobalId().getNodeId())).send(transfer);
    }

    public void initialSend(final TaskToComputeTO transfer, int nodeId) throws JMSException {
        counter++;
        getSender(TaskQueueNameResolver.getQueueNameFor(nodeId)).send(transfer);
    }

}
