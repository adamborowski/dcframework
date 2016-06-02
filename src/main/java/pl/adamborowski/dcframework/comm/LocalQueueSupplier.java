package pl.adamborowski.dcframework.comm;

import lombok.RequiredArgsConstructor;
import org.apache.log4j.Logger;
import pl.adamborowski.dcframework.LocalQueue;
import pl.adamborowski.dcframework.api.GlobalQueueReceiver;

import javax.jms.JMSException;

@RequiredArgsConstructor
public class LocalQueueSupplier {
    private final Logger log = Logger.getLogger(LocalQueueSupplier.class);
    private final LocalQueue localQueue;
    private final GlobalQueueReceiver receiver;
    private final long checkInterval;
    private Thread thread;
    private boolean running;
    private final int minThreshold;

    public void start() {
        assert thread == null;
        running = true;
        this.thread = new Thread(new Supplier());
        this.thread.start();

    }

    public void stop() {
        running = false;
    }

    private boolean shouldQueueBeBigger() {
        return localQueue.size() < minThreshold;
    }

    private class Supplier implements Runnable {

        @Override
        public void run() {
            log.info("supplier started");
            while (running) {
                if (shouldQueueBeBigger())
                    try {
                        receiver.receiveTask(checkInterval);
                    } catch (JMSException e) {
                        log.error("Supplier can't receive global task: ", e);
                    }
                try {
                    Thread.sleep(checkInterval);
                } catch (InterruptedException e) {
                    log.error("Supplier interrupted during working", e);
                }
            }
        }
    }
}
