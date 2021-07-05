package demo.newrelic.numberlogger.data;

/**
 * Implementation of the UniqueDataScreener that optimizes memory usage. The data
 * is assumed to be any non-negative integer between 0 and 999999999. This means that there
 * are 1 billion possible data points. The only information that needs to be stored is a marker
 * to indicate whether it has been seen, which requires only 1 bit of information. Because the
 * data falls in a fixed continuous range, markers for all 1 billion values can be visualized as
 * a continuous array of 1 billion bits, or 125,000,000 bytes. Unlike a hashset which grows as more
 * values are added, all the memory is allocated up front and remains the same throughout the
 * application lifecycle.
 *
 * This implementation takes up approximately 119 MB of memory to hold all the markers.
 * (1000000000 possible values at 1 bit each = 125000000 bytes = 119.2 MB). A traditional hashmap
 * implementation would surpass this at approximately 7.8 million stored Integers. (Java Integer
 * object takes up 16 bytes according to documentation, 119.2MB / 16 bytes = 7812500 values).
 * Since this application is intended to stream large amounts of data, it is likely the number of
 * unique values will surpass this threshold, so this marker implementation will be used internally.
 */
public class UniqueDataScreenerMarkerImpl implements UniqueDataScreener {

    private static final int NUM_DATA_MARKER_BUCKETS = 1000000000 / 8;
    private final byte[] dataMarkers;

    public UniqueDataScreenerMarkerImpl() {
        dataMarkers = new byte[NUM_DATA_MARKER_BUCKETS];
    }

    @Override
    public boolean isUnique(int data) {

        // The byte index is the data's value divided by 8 (8 bits per byte), discarding remainder.
        // Shift right 3 is equivalent and is slightly more efficient than division
        int bucket = data >> 3;

        // The marker within the byte is determined by the data's value mod 8 (8 bites per byte).
        // Mask the last byte by 0x07 and shift left 1 that number of times to mask off the appropriate bit.
        byte marker = (byte)((0x01 << (data & 0x07)) & 0xFF);

        // Ex: 111222 would be in index 13902 (111222 / 8 = 13902) with specific bit 6
        // (111222 % 8 = 6)

        // If the bit isn't set, the value hasn't been seen. Set the marker and return true.
        if ((dataMarkers[bucket] & marker) == 0) {
            dataMarkers[bucket] |= marker;
            return true;
        }
        return false;
    }
}
