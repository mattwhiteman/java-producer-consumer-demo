package demo.newrelic.numberlogger.server.signalling;

/**
 * Classes that implement this interface are capable of sending a server shutdown signal
 * and holding references to receivers that need to receive this signal.
 */
public interface TerminateSignalSender {
    public void signalServerStop();
    public void registerSignalReceiver(TerminateSignalReceiver receiver);
    public void unregisterSignalReceiver(TerminateSignalReceiver receiver);
}
