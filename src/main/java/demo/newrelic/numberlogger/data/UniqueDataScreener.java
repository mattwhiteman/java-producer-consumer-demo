package demo.newrelic.numberlogger.data;

/**
 * Interface for a class that implements some method of determining whether
 * a particular number has been seen already in the data stream. It is
 * assumed that the call to isUnique also marks a number as being "seen".
 *
 * Ex: The first call to isUnique(123) should return true, any subsequent calls
 * should return false.
 */
public interface UniqueDataScreener {

    /**
     * Check whether a number has been seen by this module. This call will also
     * mark that number has having been seen, so subsequent calls with the same
     * number will return false.
     * @param data: Number to check
     * @return true if the number has not been seen, false otherwise
     */
    public boolean isUnique(int data);
}
