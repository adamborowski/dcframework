package pl.adamborowski.dcframework.api;

import org.apache.log4j.Logger;
import pl.adamborowski.dcframework.comm.data.TransferObject;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;

public class ActiveMQSender {
    private final MessageProducer producer;
    private final Logger log;
    private Session session;
    private final String queueName;
    private final Queue queue;

    public ActiveMQSender(Session session, String name) throws JMSException {
        this.session = session;
        this.queueName = name;
        queue = session.createQueue(name);
        producer = session.createProducer(queue);
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        log = Logger.getLogger(ActiveMQSender.class);
    }

    public void send(TransferObject object) throws JMSException {
        ObjectMessage objectMessage = session.createObjectMessage();
        objectMessage.setObject(object);
        producer.send(queue, objectMessage);
    }

}
