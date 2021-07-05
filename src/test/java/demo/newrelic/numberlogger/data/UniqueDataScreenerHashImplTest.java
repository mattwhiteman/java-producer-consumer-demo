package demo.newrelic.numberlogger.data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UniqueDataScreenerHashImplTest {
    @Test
    public void testUnique() {
        UniqueDataScreenerHashImpl underTest = new UniqueDataScreenerHashImpl();

        assertTrue(underTest.isUnique(111224));
        assertFalse(underTest.isUnique(111224));

        assertTrue(underTest.isUnique(111225));
        assertFalse(underTest.isUnique(111225));

        assertTrue(underTest.isUnique(111226));
        assertFalse(underTest.isUnique(111226));

        assertTrue(underTest.isUnique(111227));
        assertFalse(underTest.isUnique(111227));

        assertTrue(underTest.isUnique(111228));
        assertFalse(underTest.isUnique(111228));

        assertTrue(underTest.isUnique(111229));
        assertFalse(underTest.isUnique(111229));

        assertTrue(underTest.isUnique(111230));
        assertFalse(underTest.isUnique(111230));

        assertTrue(underTest.isUnique(111231));
        assertFalse(underTest.isUnique(111231));
    }
}
