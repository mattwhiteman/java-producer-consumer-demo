package demo.newrelic.numberlogger.logging;

import demo.newrelic.numberlogger.data.UniqueDataScreener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NumberLoggerTest {

    @Mock
    private UniqueDataScreener mockDataScreener;

    @Test
    public void testLogNumber() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        NumberLogger numberLogger = new NumberLogger(outputStream, mockDataScreener,0);
        when(mockDataScreener.isUnique(anyInt())).thenReturn(true);

        numberLogger.logNumber(123456789);

        assertEquals("123456789" + System.lineSeparator(), outputStream.toString());

        NumberReport report = numberLogger.getReport();
        assertEquals(1, report.getTotalUniques());
        assertEquals(1, report.getUniquesThisRun());
        assertEquals(0, report.getDupesThisRun());

        outputStream.close();
    }

    @Test
    public void testLogDuplicate() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        NumberLogger numberLogger = new NumberLogger(outputStream, mockDataScreener, 0);
        when(mockDataScreener.isUnique(anyInt())).thenReturn(true, false);

        numberLogger.logNumber(123456789);
        numberLogger.logNumber(123456789);

        assertEquals("123456789" + System.lineSeparator(), outputStream.toString());

        NumberReport report = numberLogger.getReport();
        assertEquals(1, report.getTotalUniques());
        assertEquals(1, report.getUniquesThisRun());
        assertEquals(1, report.getDupesThisRun());

        outputStream.close();
    }

    @Test
    public void testLogWithPaddingZeroes() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        NumberLogger numberLogger = new NumberLogger(outputStream, mockDataScreener,0);
        when(mockDataScreener.isUnique(anyInt())).thenReturn(true);

        numberLogger.logNumber(111222);

        assertEquals("000111222" + System.lineSeparator(), outputStream.toString());

        NumberReport report = numberLogger.getReport();
        assertEquals(1, report.getTotalUniques());
        assertEquals(1, report.getUniquesThisRun());
        assertEquals(0, report.getDupesThisRun());

        outputStream.close();
    }

    @Test
    public void testBatchLog() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        NumberLogger numberLogger = new NumberLogger(outputStream, mockDataScreener,5);
        when(mockDataScreener.isUnique(anyInt())).thenReturn(true);

        numberLogger.logNumber(123456789);
        numberLogger.logNumber(223456789);
        numberLogger.logNumber(323456789);
        numberLogger.logNumber(423456789);

        assertTrue(outputStream.toString().isEmpty());

        numberLogger.logNumber(523456789);

        assertFalse(outputStream.toString().isEmpty());

        NumberReport report = numberLogger.getReport();
        assertEquals(5, report.getTotalUniques());
        assertEquals(5, report.getUniquesThisRun());
        assertEquals(0, report.getDupesThisRun());

        outputStream.close();
    }

    @Test
    public void testMultipleReportCalls() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        NumberLogger numberLogger = new NumberLogger(outputStream, mockDataScreener,0);
        when(mockDataScreener.isUnique(anyInt())).thenReturn(true, false, true);

        numberLogger.logNumber(123456789);
        numberLogger.logNumber(123456789);

        NumberReport report = numberLogger.getReport();
        assertEquals(1, report.getTotalUniques());
        assertEquals(1, report.getUniquesThisRun());
        assertEquals(1, report.getDupesThisRun());

        report = numberLogger.getReport();
        assertEquals(1, report.getTotalUniques());
        assertEquals(0, report.getUniquesThisRun());
        assertEquals(0, report.getDupesThisRun());

        numberLogger.logNumber(223456789);
        numberLogger.logNumber(323456789);

        report = numberLogger.getReport();
        assertEquals(3, report.getTotalUniques());
        assertEquals(2, report.getUniquesThisRun());
        assertEquals(0, report.getDupesThisRun());

        outputStream.close();
    }
}
