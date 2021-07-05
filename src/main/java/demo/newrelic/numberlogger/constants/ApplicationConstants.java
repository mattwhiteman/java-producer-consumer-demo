package demo.newrelic.numberlogger.constants;

public final class ApplicationConstants {
    private ApplicationConstants() {}

    // In a production app, these would all be configurable properties
    public static final int SERVER_PORT = 4000;
    public static final int MAX_CLIENT_THREADS = 5;
    public static final int SERVER_REPORT_INTERVAL_MS = 10000;
    public static final int DEFAULT_BATCH_WRITE_NUM = 100000;
    public static final String LOGFILE_NAME = "numbers.log";
}
