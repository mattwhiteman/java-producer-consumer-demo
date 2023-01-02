package demo.producerconsumer.data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UniqueDataScreenerMarkerImplTest {

    @Test
    public void testUnique() {
        UniqueDataScreenerMarkerImpl underTest = new UniqueDataScreenerMarkerImpl();

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

    @Test
    public void testEdgeCases() {
        UniqueDataScreenerMarkerImpl underTest = new UniqueDataScreenerMarkerImpl();
        assertTrue(underTest.isUnique(0));
        assertFalse(underTest.isUnique(0));

        assertTrue(underTest.isUnique(999999999));
        assertFalse(underTest.isUnique(999999999));
    }

    @Test
    public void testIllegalConstructorArgument() {
        try {
            new UniqueDataScreenerMarkerImpl(-1);
            fail();
        } catch (IllegalArgumentException e) {
            // Passes by default
        }
    }

    @Test
    public void testIllegalInput() {
        UniqueDataScreenerMarkerImpl underTest = new UniqueDataScreenerMarkerImpl();

        assertFalse(underTest.isUnique(1000000000));
        assertFalse(underTest.isUnique(-1));
    }
}
