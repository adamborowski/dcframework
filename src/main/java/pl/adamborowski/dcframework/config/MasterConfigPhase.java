package pl.adamborowski.dcframework.config;

import org.apache.log4j.Logger;
import pl.adamborowski.dcframework.remote.TaskQueueNameResolver;
import pl.adamborowski.dcframework.remote.data.SlaveConfigMessage;
import pl.adamborowski.dcframework.util.ActiveMQReceiver;
import pl.adamborowski.dcframework.util.ActiveMQSender;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class MasterConfigPhase {
    private final ActiveMQSender configTopicSender;
    private final ActiveMQReceiver configTopicReceiver;
    private final Logger log = Logger.getLogger(MasterConfigPhase.class);
    private final Session session;

    public MasterConfigPhase(Connection connection) throws JMSException {
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        configTopicSender = ActiveMQSender.forTopic(session, TaskQueueNameResolver.getConfigTopicName());
        configTopicReceiver = ActiveMQReceiver.forQueue(session, TaskQueueNameResolver.getConfigResponseQueueName());
    }

    /**
     * @param nodeConfig
     * @return collection of slave nodes id
     * @throws JMSException
     */
    public Collection<Integer> perform(NodeConfig nodeConfig) throws JMSException {
        log.info("Send configuration to config topic");
        configTopicSender.send(new SlaveConfigMessage(nodeConfig));
        final List<Integer> slaveIds = new LinkedList<>();
        while (true) {
            SlaveConfigMessage.Response slaveResponse = (SlaveConfigMessage.Response) configTopicReceiver.receive(1000);
            if (slaveResponse == null) {
                break;
            }
            slaveIds.add(slaveResponse.getNodeId());
            log.info("Got slave response from node " + slaveResponse.getNodeId());
        }
        log.info("Summary: got slave responses from " + slaveIds.size() + " nodes");
        session.close();
        return slaveIds;
    }
}
