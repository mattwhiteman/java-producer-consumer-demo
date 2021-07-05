package demo.newrelic.numberlogger.server.signalling;

import java.util.LinkedList;
import java.util.List;

/**
 * This class starts a high-priority thread that sleeps until it receives a signal to
 * send the server shutdown signal to all registered clients.
 */
public class TerminateSignalSenderImpl implements TerminateSignalSender {

    // Monitor object used for locking and waking the signal thread
    private final Object serverTerminateSignal;

    // List of clients to receive the terminate signal.
    private final List<TerminateSignalReceiver> signalReceiverList;

    // Thread that handles the signaling to all registered clients
    private final Thread signalHandlerThread;

    public TerminateSignalSenderImpl() {
        serverTerminateSignal = new Object();
        signalReceiverList = new LinkedList<>();

        // Run this thread at highest priority to ensure all clients get
        // the termination notice as quickly as possible once the thread
        // has been woken
        signalHandlerThread = new Thread(this::doWait);
        signalHandlerThread.setPriority(Thread.MAX_PRIORITY);
        signalHandlerThread.start();
    }

    /**
     * Removes a client from the registration list. This should be called during the application
     * run when short-lived objects are going out of scope and will no longer need to receive the
     * signal.
     * Thread-safe call that will block if another thread is accessing the underlying collection.
     * @param receiver
     */
    public synchronized void unregisterSignalReceiver(TerminateSignalReceiver receiver) {
        // A list is used instead of another data structure for simplicity. There should be a very
        // small amount of these registered clients, so iterating over the list in this
        // manner should have no performance effect.
        signalReceiverList.remove(receiver);
    }

    /**
     * Add a client to the registration list to receive the termination signal if it occurs.
     * Thread-safe call that will block if another thread is accessing the underlying collection.
     * @param receiver
     */
    public synchronized void registerSignalReceiver(TerminateSignalReceiver receiver) {
        signalReceiverList.add(receiver);
    }

    public void signalServerStop() {
        synchronized(serverTerminateSignal) {
            serverTerminateSignal.notify();
        }
    }

    private void doWait() {
        synchronized (serverTerminateSignal) {
            try {
                serverTerminateSignal.wait();
                sendTerminationSignal();
            } catch (InterruptedException ignored) {
            }
        }
    }

    private synchronized void sendTerminationSignal() {
        signalReceiverList.forEach(TerminateSignalReceiver::receiveTerminateSignal);
    }

    /**
     * Cleanly shut down the signal thread. Registered clients will *not* receive the
     * termination signal when this is called, as this would only be called when the server
     * is already shutting down (and clients have already been notified).
     */
    public synchronized void shutdownSignalSender() {
        signalReceiverList.clear();
        signalServerStop();
        try {
            signalHandlerThread.join();
        } catch (InterruptedException ignored) {
        }
    }
}
