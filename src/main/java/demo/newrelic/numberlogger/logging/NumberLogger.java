package demo.newrelic.numberlogger.logging;

import demo.newrelic.numberlogger.data.UniqueDataScreener;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * This class is responsible for logging unique numbers to the logfile. A UniqueDataScreener instance
 * is used to determine if the value is unique across all input seen so far, and then makes a
 * determination about whether to log the value and update the metrics. To minimize disk io, logged
 * values are held in a buffer in memory and written out only when the buffer reaches a threshold size.
 * The application is responsible for closing the log, and explicitly flushing a partial batch on
 * shutdown or any other desired situation. Batching is necessary as disk io is slow and can hamper
 * performance when the application is streaming large amounts of data.
 *
 * A report of metrics can be retrieved at any time detailing the number of unique values seen,
 * and the number of unique and duplicates seen since the last report was retrieved.
 *
 * All operations on this class are thread-safe.
 *
 * All numbers written to the log file will be 9-characters long with appropriate leading 0's
 * added to meet this 9-character length. Each entry will be on a separate line in the log file,
 * separated by a server-native newline sequence.
 */
public class NumberLogger {

    private static final int EXPECTED_NUMBER_LENGTH = 9;
    private final OutputStreamWriter logWriter;
    private final int logWriteThreshold;
    private final UniqueDataScreener dataScreener;

    private volatile int totalNumbersLogged;
    private volatile int loggedSinceLastReport;
    private volatile int dupesSinceLastReport;

    // Calls accessing the builder are already synchronized so no need to use threadsafe StringBuffer
    private final StringBuilder outputBuffer;

    public NumberLogger(OutputStream outputLogStream, UniqueDataScreener dataScreener, int logBatchSize) {
        this.logWriter = new OutputStreamWriter(outputLogStream);
        this.outputBuffer = new StringBuilder();
        this.logWriteThreshold = logBatchSize * (EXPECTED_NUMBER_LENGTH + System.lineSeparator().length());
        this.dataScreener = dataScreener;
    }

    /**
     * If the parameter is a unique number, adds it to the log batch to be written to disk. The
     * number will be padded with leading 0's to ensure it is 9 characters long and written
     * on a separate line using a server-native newline sequence. Metrics are updated on each
     * call to record whether the number is a unique or duplicate.
     *
     * Thread-safe and will block if multiple threads attempt to call simultaneously.
     */
    public synchronized void logNumber(Integer number) {
        if (dataScreener.isUnique(number)) {
            outputBuffer.append(String.format("%09d", number));
            outputBuffer.append(System.lineSeparator());
            totalNumbersLogged++;
            loggedSinceLastReport++;

            if (outputBuffer.length() >= logWriteThreshold) {
                flushLog();
            }
        }
        else {
            dupesSinceLastReport++;
        }
    }

    /**
     * Flush all unique numbers held in memory that have not yet been written to disk.
     * Thread-safe and will block if multiple threads attempt to call simultaneously.
     */
    public synchronized void flushLog() {
        String outputVal = outputBuffer.toString();
        outputBuffer.setLength(0);
        try {
            logWriter.write(outputVal);
            logWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Close the log file.
     * Thread-safe and will block if multiple threads attempt to call simultaneously.
     */
    public synchronized void closeLog() {
        IOUtils.closeQuietly(logWriter, null);
    }

    /**
     * Retrieve a report of the data metrics. Calling this function will reset the metrics
     * that record since the last report was retrieved.
     * @return
     */
    public synchronized NumberReport getReport() {
        NumberReport retVal = new NumberReport(dupesSinceLastReport, loggedSinceLastReport, totalNumbersLogged);
        dupesSinceLastReport = 0;
        loggedSinceLastReport = 0;
        return retVal;
    }
}
