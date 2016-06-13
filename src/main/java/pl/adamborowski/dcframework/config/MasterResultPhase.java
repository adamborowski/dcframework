package pl.adamborowski.dcframework.config;

import com.google.common.collect.Sets;
import pl.adamborowski.dcframework.remote.TaskQueueNameResolver;
import pl.adamborowski.dcframework.remote.data.StatisticsReportMessage;
import pl.adamborowski.dcframework.util.ActiveMQReceiver;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MasterResultPhase {

    private final Session session;
    private final ActiveMQReceiver statisticsReceiver;

    public MasterResultPhase(Connection connection) throws JMSException {
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        statisticsReceiver = ActiveMQReceiver.forQueue(session, TaskQueueNameResolver.getConfigResponseQueueName());
    }

    public Map<Integer, Statistics> perform(Collection<Integer> slaveIds, Statistics statistics) throws JMSException {
        final Set<Integer> slaveIdSet = Sets.newHashSet(slaveIds);
        final Map<Integer, Statistics> slaveStatistics = new HashMap<>();
        slaveStatistics.put(0, statistics);
        while (!slaveIdSet.isEmpty()) {
            StatisticsReportMessage report = (StatisticsReportMessage) statisticsReceiver.receive();
            slaveIdSet.remove(report.getNodeId());
            slaveStatistics.put(report.getNodeId(), report.getStatistics());
        }
        return slaveStatistics;
    }
}
