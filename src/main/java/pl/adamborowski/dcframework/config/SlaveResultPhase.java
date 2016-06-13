package pl.adamborowski.dcframework.config;

import pl.adamborowski.dcframework.remote.TaskQueueNameResolver;
import pl.adamborowski.dcframework.remote.data.StatisticsReportMessage;
import pl.adamborowski.dcframework.util.ActiveMQSender;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;

public class SlaveResultPhase {

    private final Session session;
    private final ActiveMQSender statisticsSender;

    public SlaveResultPhase(Connection connection) throws JMSException {
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        statisticsSender = ActiveMQSender.forQueue(session, TaskQueueNameResolver.getConfigResponseQueueName());
    }

    public void perform(int nodeId, Statistics statistics) throws JMSException {
        statisticsSender.send(new StatisticsReportMessage(nodeId, statistics));
    }
}
