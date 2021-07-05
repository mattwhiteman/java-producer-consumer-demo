package demo.newrelic.numberlogger.data;

import java.util.HashSet;
import java.util.Set;

/**
 * Basic implementation of the UniqueDataScreener that stores marked strings in
 * a hash set. For large amounts of numbers, this may not be suitable due to the
 * growing memory footprint of the internal hashset as millions of messages are
 * processed.
 */
public class UniqueDataScreenerHashImpl implements UniqueDataScreener {
    private final Set<Integer> uniqueMessages;

    public UniqueDataScreenerHashImpl() {
        uniqueMessages = new HashSet<>();
    }

    public boolean isUnique(int data) {
        return uniqueMessages.add(data);
    }
}
