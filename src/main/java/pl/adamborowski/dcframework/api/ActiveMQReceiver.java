package pl.adamborowski.dcframework.api;

import org.apache.activemq.command.ActiveMQDestination;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import pl.adamborowski.dcframework.comm.data.TransferObject;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

public class ActiveMQReceiver {
    private final MessageConsumer consumer;
    private final ActiveMQDestination destination;
    private final Logger log = Logger.getLogger(ActiveMQReceiver.class);

    public ActiveMQReceiver(Session session, ActiveMQDestination destination) throws JMSException {
        this.destination = destination;
        consumer = session.createConsumer(destination);
    }

    public static ActiveMQReceiver forQueue(Session session, String name) throws JMSException {
        return new ActiveMQReceiver(session, (ActiveMQDestination) session.createQueue(name));
    }

    public static ActiveMQReceiver forTopic(Session session, String name) throws JMSException {
        return new ActiveMQReceiver(session, (ActiveMQDestination) session.createTopic(name));
    }




    public TransferObject receive(long timeout) throws JMSException {
        final Message msg = consumer.receive(timeout);
        return getTransferObject(msg);
    }

    public TransferObject receive() throws JMSException {
        final Message msg = consumer.receive();
        return getTransferObject(msg);

    }

    public TransferObject receiveNoWait() throws JMSException {
        final Message msg = consumer.receiveNoWait();
        return getTransferObject(msg);
    }

    @SuppressWarnings("Duplicates")
    private TransferObject getTransferObject(Message msg) throws JMSException {
        if (msg == null) {
            return null;
        }
        if (msg instanceof ObjectMessage) {
            ObjectMessage objectMessage = (ObjectMessage) msg;
            if (objectMessage.getObject() instanceof TransferObject) {
                if (log.isEnabledFor(Level.TRACE)) {
                    log.trace(String.format("Received from %s: %s", destination.getPhysicalName(), objectMessage.getObject().toString()));
                }
                return (TransferObject) objectMessage.getObject();
            }
            throw new IllegalStateException("Got an object which is not TransferObject");
        }
        throw new IllegalStateException("Got not an ObjectMessage: " + msg);
    }

}
