package pl.adamborowski.zar;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.log4j.Logger;
import pl.adamborowski.dcframework.remote.TaskQueueNameResolver;

import javax.jms.JMSException;

public class ActiveMQUtil {
    public static void resetConnection(ProgramArgs options) {
        ActiveMQConnection conn = null;
        try {
            conn = (ActiveMQConnection) new ActiveMQConnectionFactory(options.getConnectionUrl()).createConnection();
            if (options.getNodeId() == 0) {
                conn.destroyDestination(new ActiveMQQueue(TaskQueueNameResolver.getGlobalQueueName()));
                conn.destroyDestination(new ActiveMQQueue(TaskQueueNameResolver.getConfigResponseQueueName()));
                conn.destroyDestination(new ActiveMQQueue(TaskQueueNameResolver.getConfigTopicName()));
            }
            conn.destroyDestination(new ActiveMQQueue("queue-" + options.getNodeId()));
        } catch (JMSException e) {
            Logger.getLogger(ActiveMQUtil.class).warn("Cannot clear queues");
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (JMSException e) {
                    System.out.println("Error closing connection" + e);
                }
            }
        }
    }
}
