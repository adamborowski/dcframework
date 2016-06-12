package pl.adamborowski.dcframework.api;

import org.apache.log4j.Logger;
import pl.adamborowski.dcframework.comm.data.TransferObject;

import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

public class ActiveMQSender {
    private final MessageProducer producer;
    private final Logger log;
    private Session session;

    public ActiveMQSender(Session session, Destination destination) throws JMSException {
        this.session = session;
        producer = session.createProducer(destination);
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        log = Logger.getLogger(ActiveMQSender.class);
    }

    public static ActiveMQSender forQueue(Session session, String name) throws JMSException {
        return new ActiveMQSender(session, session.createQueue(name));
    }

    public static ActiveMQSender forTopic(Session session, String name) throws JMSException {
        return new ActiveMQSender(session, session.createTopic(name));
    }

    public void send(TransferObject object) throws JMSException {
        ObjectMessage objectMessage = session.createObjectMessage();
        objectMessage.setObject(object);
        producer.send(objectMessage);
    }

}
