package demo.newrelic.numberlogger.server;

import demo.newrelic.numberlogger.logging.NumberLogger;
import demo.newrelic.numberlogger.logging.NumberReport;

import java.util.TimerTask;

/**
 * Responsible for outputting the server metrics every 10 seconds. Extends from a TimerTask so
 * that it can be run in a timer.
 */
public class ServerReporter extends TimerTask {
    private final NumberLogger logger;

    public ServerReporter(NumberLogger logger) {
        this.logger = logger;
    }

    @Override
    public void run() {
        NumberReport report = logger.getReport();
        System.out.println("Received " + report.getUniquesThisRun() + " unique numbers, "
                + report.getDupesThisRun() + " duplicates. Total uniques: "
                + report.getTotalUniques());
    }
}
