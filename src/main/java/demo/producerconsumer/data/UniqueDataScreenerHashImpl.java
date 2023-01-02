package demo.producerconsumer.data;

import java.util.HashSet;
import java.util.Set;

/**
 * Basic implementation of the UniqueDataScreener that stores marked strings in
 * a hash set. For large amounts of numbers, this may not be suitable due to the
 * growing memory footprint of the internal hashset as millions of messages are
 * processed. However it does provide the benefit of not limiting the input range and
 * will accept negative numbers. This would be useful for more complex implementations
 * of this app.
 *
 * Per the requirements of this application and for optimization purposes, this class
 * is not actually used and is provided only for illustration.
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
