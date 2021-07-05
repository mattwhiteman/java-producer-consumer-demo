package demo.newrelic.numberlogger.server.signalling;

/**
 * Classes that implement this interface are capable of receiving signals that the server is
 * to shutdown. Implementing classes are responsible for ensuring the thread safety of this
 * and other necessary functions.
 */
public interface TerminateSignalReceiver {
    public void receiveTerminateSignal();
}
