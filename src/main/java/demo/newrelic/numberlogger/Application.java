package demo.newrelic.numberlogger;

import demo.newrelic.numberlogger.constants.ApplicationConstants;
import demo.newrelic.numberlogger.consumer.NumberMessageConsumer;
import demo.newrelic.numberlogger.data.UniqueDataScreenerMarkerImpl;
import demo.newrelic.numberlogger.logging.NumberLogger;
import demo.newrelic.numberlogger.server.ServerConnectionListener;
import demo.newrelic.numberlogger.server.ServerReporter;
import demo.newrelic.numberlogger.server.signalling.TerminateSignalSenderImpl;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Queue;
import java.util.Timer;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Application {
    public static void main(String[] args) throws IOException {
        // In a production app, most of this logic would be delegated to a builder or framework that
        // wires everything together.

        Queue<Integer> sharedNumberQueue = new ConcurrentLinkedQueue<>();
        NumberLogger logger = new NumberLogger(new FileOutputStream(ApplicationConstants.LOGFILE_NAME),
                new UniqueDataScreenerMarkerImpl(), ApplicationConstants.DEFAULT_BATCH_WRITE_NUM);

        TerminateSignalSenderImpl terminateServerSignaller = new TerminateSignalSenderImpl();
        NumberMessageConsumer consumer = new NumberMessageConsumer(sharedNumberQueue, logger);
        ServerConnectionListener connectionListener = new ServerConnectionListener(sharedNumberQueue, terminateServerSignaller);
        terminateServerSignaller.registerSignalReceiver(consumer);
        terminateServerSignaller.registerSignalReceiver(connectionListener);

        // Add the metrics reporter to the timer on a 10-second schedule
        Timer reportTimer = new Timer();
        ServerReporter reporter = new ServerReporter(logger);
        reportTimer.schedule(reporter, ApplicationConstants.SERVER_REPORT_INTERVAL_MS,
                ApplicationConstants.SERVER_REPORT_INTERVAL_MS);

        // Start the consumer thread
        consumer.startConsumer();
        // Start the listener and block until the server is ready to terminate
        connectionListener.listenForClients();

        // Cleanly close out resources and wait for associated threads to finish
        reportTimer.cancel();
        consumer.waitForConsumerToFinish();
        terminateServerSignaller.shutdownSignalSender();
        // Run the reporter to output the last of the metrics
        reporter.run();

        // This should not be necessary as everything has already been verified as cleaned up and
        // shut down by this point, but making an explicit exit call just in case.
        System.exit(0);
    }
}
