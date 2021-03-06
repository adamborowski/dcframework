package pl.adamborowski.dcframework.remote;

import com.google.common.base.Throwables;
import lombok.RequiredArgsConstructor;
import org.apache.log4j.Logger;
import pl.adamborowski.dcframework.node.LocalQueue;
import pl.adamborowski.dcframework.remote.comm.GlobalQueueReceiver;
import pl.adamborowski.dcframework.remote.data.TaskToComputeTO;

import javax.jms.JMSException;

@RequiredArgsConstructor
public class LocalQueueSupplier {
    private final Logger log = Logger.getLogger(LocalQueueSupplier.class);
    private final LocalQueue localQueue;
    private final GlobalQueueReceiver receiver;
    private final long checkInterval;
    private final long checkIntervalSubsequent;

    private Thread thread;
    private boolean running;
    private final int minThreshold;

    public void start() {
        assert thread == null;
        running = true;
        this.thread = new Thread(new Supplier(), "Supplier");
        this.thread.start();

    }

    public void stop() {
        running = false;
        this.thread.interrupt();
    }

    private boolean shouldQueueBeBigger() {
//        log.debug(String.format("Checking local queue if size < minThreshold: %s < %s", localQueue.size(), minThreshold));
        return localQueue.size() < minThreshold;
    }

    private class Supplier implements Runnable {

        @SuppressWarnings("ConstantConditions")
        @Override
        public void run() {
            log.info("supplier started");
            try {
                while (running) {
                    if (shouldQueueBeBigger()) {
                        TaskToComputeTO receivedTask = receiver.receiveTask(checkInterval);
                        if (receivedTask != null) {
                            log.trace("received task to supply from global queue: " + receivedTask.toString());
                            while (running && shouldQueueBeBigger()) {
                                // stary, jak już się dorwę do biorę ile wlezie (dopóki potrzebuję), ale pod warunkiem, że nie będę czekał
                                receivedTask = receiver.receiveTaskNoWait();
                                if (receivedTask == null) {
                                    Thread.sleep(checkIntervalSubsequent);
                                } else {
                                    log.trace("received task to supply from global queue: " + receivedTask.toString());
                                }
                            }
                        }
                    }
                    Thread.sleep(checkInterval);
                }
            } catch (InterruptedException | JMSException e) {
                if (Throwables.getRootCause(e) instanceof InterruptedException) {
                    log.info("Supplied interrupted");
                } else {
                    log.error("Supplier can't receive global task: ", e);
                    System.exit(123);
                }
            }
            log.info("supplier stopped");
        }
    }
}
