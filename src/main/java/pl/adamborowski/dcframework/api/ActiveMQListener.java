package pl.adamborowski.dcframework.api;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import pl.adamborowski.dcframework.comm.data.TransferObject;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;

public class ActiveMQListener {
    private final Session session;
    private final String name;
    private final Queue queue;
    private final MessageConsumer consumer;
    private final Logger log;

    public ActiveMQListener(Session session, String name) throws JMSException {

        this.session = session;
        this.name = name;
        queue = session.createQueue(name);
        consumer = session.createConsumer(queue);
        log = Logger.getLogger(ActiveMQListener.class);
    }

    public void setTransferListener(TransferListener listener) {
        log.warn("set transfer listener for name: " + name);

        try {
            consumer.setMessageListener(message -> {
                try {
                    listener.onTransfer(getTransferObject(message));
                } catch (JMSException e) {
                    log.error("Exception during asynchronous receive: ", e);
                }
            });
        } catch (JMSException e) {
            log.error("Exception during setting transfer listener, ", e);
        }
    }

    @SuppressWarnings("Duplicates")
    private TransferObject getTransferObject(Message msg) throws JMSException {
        if (msg instanceof ObjectMessage) {
            ObjectMessage objectMessage = (ObjectMessage) msg;
            if (objectMessage.getObject() instanceof TransferObject) {
                if (log.isEnabledFor(Level.DEBUG)) {
                    log.debug(String.format("Received from %s: %s", queue.getQueueName(), objectMessage.getObject().toString()));
                }
                return (TransferObject) objectMessage.getObject();
            }
            throw new IllegalStateException("Got an object which is not TransferObject");
        }
        throw new IllegalStateException("Got not an ObjectMessage");
    }


    public interface TransferListener {
        void onTransfer(TransferObject transfer);
    }
}
