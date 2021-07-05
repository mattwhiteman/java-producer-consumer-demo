package demo.newrelic.numberlogger.consumer;

import demo.newrelic.numberlogger.logging.NumberLogger;
import demo.newrelic.numberlogger.server.signalling.TerminateSignalReceiver;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class is responsible for managing a single thread that consumes numbers written
 * to the shared queue by client threads. Messages read from the queue are passed to
 * the logging module for decisioning about writing to a log file. The assumption is that
 * large amounts of data will be streamed through this queue, so the internal thread
 * uses a busy-wait strategy that keeps polling the queue rather than doing a blocking
 * read.
 */
public class NumberMessageConsumer implements TerminateSignalReceiver {
    private final Queue<Integer> sharedNumberQueue;
    private final NumberLogger numberLogger;
    private final AtomicBoolean serverActive;
    private final Thread consumerThread;

    public NumberMessageConsumer(Queue<Integer> sharedNumberQueue, NumberLogger logger) {
        this.sharedNumberQueue = sharedNumberQueue;
        this.numberLogger = logger;
        this.serverActive = new AtomicBoolean(true);
        this.consumerThread = new Thread(this::doConsume);
    }

    /**
     * Thread-safe function to start the thread that reads from the shared message
     * queue. Calling this function when the thread is already started will have
     * no effect.
     */
    public synchronized void startConsumer() {
        if (!consumerThread.isAlive()) {
            consumerThread.start();
        }
    }

    /**
     * This function causes the caller to block until the internal thread has
     * finished and exited. The intended use of this function is to be called after
     * the thread has received a termination signal and is shutting down.
     */
    public void waitForConsumerToFinish() {
        try {
            consumerThread.join();
        } catch (InterruptedException ignored) {
        }
    }

    /**
     * Tells the module to stop reading from the queue and shut down cleanly.
     * If there is still data in the queue, it will not continue to be processed
     * until the queue is empty.
     */
    @Override
    public void receiveTerminateSignal() {
        serverActive.set(false);
    }

    private void doConsume() {
        Integer readVal = null;
        while(serverActive.get()) {
            readVal = sharedNumberQueue.poll();
            if (readVal != null) {
                numberLogger.logNumber(readVal);
            }
        }
        // Make sure the last partial batch in the logs are written out
        numberLogger.flushLog();
        numberLogger.closeLog();
    }
}
