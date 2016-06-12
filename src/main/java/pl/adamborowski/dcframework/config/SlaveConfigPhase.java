package pl.adamborowski.dcframework.config;

import pl.adamborowski.dcframework.api.ActiveMQReceiver;
import pl.adamborowski.dcframework.api.ActiveMQSender;
import pl.adamborowski.dcframework.comm.TaskQueueNameResolver;
import pl.adamborowski.dcframework.comm.data.SlaveConfigMessage;

import javax.jms.JMSException;
import javax.jms.Session;

public class SlaveConfigPhase {
    private final ActiveMQSender configResponseSender;
    private final ActiveMQReceiver configReceiver;

    public SlaveConfigPhase(Session session) throws JMSException {
        configResponseSender = ActiveMQSender.forQueue(session, TaskQueueNameResolver.getConfigResponseQueueName());
        configReceiver = ActiveMQReceiver.forTopic(session, TaskQueueNameResolver.getConfigTopicName());
    }

    /**
     * @param nodeId
     * @return collection of slave nodes id
     * @throws JMSException
     */
    public NodeConfig perform(Integer nodeId) throws JMSException {

        SlaveConfigMessage configMessage = (SlaveConfigMessage) configReceiver.receive();

        configResponseSender.send(new SlaveConfigMessage.Response(nodeId));
        return configMessage.getNodeConfig();
    }

}
