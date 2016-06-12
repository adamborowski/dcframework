package pl.adamborowski.dcframework.config;

import pl.adamborowski.dcframework.api.ActiveMQReceiver;
import pl.adamborowski.dcframework.api.ActiveMQSender;
import pl.adamborowski.dcframework.comm.TaskQueueNameResolver;
import pl.adamborowski.dcframework.comm.data.SlaveConfigMessage;

import javax.jms.JMSException;
import javax.jms.Session;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class MasterConfigPhase {
    private final ActiveMQSender configTopicSender;
    private final ActiveMQReceiver configTopicReceiver;

    public MasterConfigPhase(Session session) throws JMSException {
        configTopicSender = ActiveMQSender.forTopic(session, TaskQueueNameResolver.getConfigTopicName());
        configTopicReceiver = ActiveMQReceiver.forQueue(session, TaskQueueNameResolver.getConfigTopicName());
    }

    /**
     * @param nodeConfig
     * @return collection of slave nodes id
     * @throws JMSException
     */
    public Collection<Integer> perform(NodeConfig nodeConfig) throws JMSException {
        configTopicSender.send(new SlaveConfigMessage(nodeConfig));
        final List<Integer> slaveIds = new LinkedList<>();
        while (true) {
            SlaveConfigMessage.Response slaveResponse = (SlaveConfigMessage.Response) configTopicReceiver.receive(300);
            if (slaveResponse == null) {
                break;
            }
        }
        return slaveIds;
    }
}
