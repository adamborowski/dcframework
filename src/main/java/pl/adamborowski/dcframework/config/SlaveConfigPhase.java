package pl.adamborowski.dcframework.config;

import org.apache.log4j.Logger;
import pl.adamborowski.dcframework.remote.TaskQueueNameResolver;
import pl.adamborowski.dcframework.remote.data.SlaveConfigMessage;
import pl.adamborowski.dcframework.util.ActiveMQReceiver;
import pl.adamborowski.dcframework.util.ActiveMQSender;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;

public class SlaveConfigPhase {
    private final ActiveMQSender configResponseSender;
    private final ActiveMQReceiver configReceiver;
    private final Logger log = Logger.getLogger(SlaveConfigPhase.class);
    private final Session session;

    public SlaveConfigPhase(Connection connection) throws JMSException {
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        configResponseSender = ActiveMQSender.forQueue(session, TaskQueueNameResolver.getConfigResponseQueueName());
        configReceiver = ActiveMQReceiver.forTopic(session, TaskQueueNameResolver.getConfigTopicName());
    }

    /**
     * @param nodeId
     * @return collection of slave nodes id
     * @throws JMSException
     */
    public NodeConfig perform(Integer nodeId) throws JMSException {
        log.info("Receiving configuration from config topic...");
        SlaveConfigMessage configMessage = (SlaveConfigMessage) configReceiver.receive();
        log.info("Sending response to master");
        configResponseSender.send(new SlaveConfigMessage.Response(nodeId));
        session.close();
        return configMessage.getNodeConfig();
    }

}
