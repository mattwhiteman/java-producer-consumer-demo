package demo.newrelic.numberlogger.producer;

import demo.newrelic.numberlogger.server.signalling.TerminateSignalSender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientNumberProducerTest {
    @Mock
    private Socket mockSocket1;

    @Mock
    private Socket mockSocket2;

    @Mock
    private Socket mockSocket3;

    @Mock
    private Socket mockSocket4;

    @Mock
    private Socket mockSocket5;

    @Mock
    private TerminateSignalSender mockTerminateSignalSender;

    private ClientNumberProducer underTest;

    @Test
    public void testReadInput() throws InterruptedException, IOException {
        ByteArrayInputStream inputStream = buildInputSequenceForStream("123456789");
        when(mockSocket1.getInputStream()).thenReturn(inputStream);

        Queue<Integer> sharedNumberQueue = new ConcurrentLinkedQueue<>();
        underTest = new ClientNumberProducer(mockSocket1, sharedNumberQueue, mockTerminateSignalSender);

        Thread readerThread = new Thread(underTest::doRun);
        readerThread.start();

        readerThread.join();
        inputStream.close();

        assertEquals(1, sharedNumberQueue.size());
        assertEquals(123456789, sharedNumberQueue.poll());
        verify(mockTerminateSignalSender, times(0)).signalServerStop();
        verify(mockSocket1, times(1)).close();
    }

    @Test
    public void testReadInvalidInputLength() throws InterruptedException, IOException {
        ByteArrayInputStream inputStream = buildInputSequenceForStream("12345678");
        when(mockSocket1.getInputStream()).thenReturn(inputStream);

        Queue<Integer> sharedNumberQueue = new ConcurrentLinkedQueue<>();
        underTest = new ClientNumberProducer(mockSocket1, sharedNumberQueue, mockTerminateSignalSender);

        Thread readerThread = new Thread(underTest::doRun);
        readerThread.start();

        readerThread.join();
        inputStream.close();

        assertTrue(sharedNumberQueue.isEmpty());
        verify(mockTerminateSignalSender, times(0)).signalServerStop();
        verify(mockSocket1, times(1)).close();
    }

    @Test
    public void testReadInvalidInputChars() throws InterruptedException, IOException {
        ByteArrayInputStream inputStream = buildInputSequenceForStream("12345678x");
        when(mockSocket1.getInputStream()).thenReturn(inputStream);

        Queue<Integer> sharedNumberQueue = new ConcurrentLinkedQueue<>();
        underTest = new ClientNumberProducer(mockSocket1, sharedNumberQueue, mockTerminateSignalSender);

        Thread readerThread = new Thread(underTest::doRun);
        readerThread.start();

        readerThread.join();
        inputStream.close();

        assertTrue(sharedNumberQueue.isEmpty());
        verify(mockTerminateSignalSender, times(0)).signalServerStop();
        verify(mockSocket1, times(1)).close();
    }

    @Test
    public void testTerminateInput() throws InterruptedException, IOException {
        ByteArrayInputStream inputStream = buildInputSequenceForStream("terminate");
        when(mockSocket1.getInputStream()).thenReturn(inputStream);

        Queue<Integer> sharedNumberQueue = new ConcurrentLinkedQueue<>();
        underTest = new ClientNumberProducer(mockSocket1, sharedNumberQueue, mockTerminateSignalSender);

        Thread readerThread = new Thread(underTest::doRun);
        readerThread.start();

        readerThread.join();
        inputStream.close();

        assertTrue(sharedNumberQueue.isEmpty());
        verify(mockTerminateSignalSender, times(1)).signalServerStop();
        verify(mockSocket1, times(1)).close();
    }

    @Test
    public void testMixedInput() throws InterruptedException, IOException {
        ByteArrayInputStream inputStream = buildInputSequenceForStream("123456789", "000111222", "123x", "333333333");
        when(mockSocket1.getInputStream()).thenReturn(inputStream);

        Queue<Integer> sharedNumberQueue = new ConcurrentLinkedQueue<>();
        underTest = new ClientNumberProducer(mockSocket1, sharedNumberQueue, mockTerminateSignalSender);

        Thread readerThread = new Thread(underTest::doRun);
        readerThread.start();

        readerThread.join();
        inputStream.close();

        assertEquals(2, sharedNumberQueue.size());
        assertEquals(123456789, sharedNumberQueue.poll());
        assertEquals(111222, sharedNumberQueue.poll());
        verify(mockTerminateSignalSender, times(0)).signalServerStop();
        verify(mockSocket1, times(1)).close();
    }

    @Test
    public void testMultiThreadedInput() throws InterruptedException, IOException {
        // 3 valid numbers
        ByteArrayInputStream inputStream1 = buildInputSequenceForStream("123456789", "000111222", "333333333");
        when(mockSocket1.getInputStream()).thenReturn(inputStream1);
        // 1 valid numbers
        ByteArrayInputStream inputStream2 = buildInputSequenceForStream("222222222", "abx34");
        when(mockSocket2.getInputStream()).thenReturn(inputStream2);
        // 4 valid numbers
        ByteArrayInputStream inputStream3 = buildInputSequenceForStream("000000000", "000333444", "987654321", "545454545");
        when(mockSocket3.getInputStream()).thenReturn(inputStream3);
        // 1 valid number
        ByteArrayInputStream inputStream4 = buildInputSequenceForStream("999999999", "00011122", "444444444");
        when(mockSocket4.getInputStream()).thenReturn(inputStream4);
        // 1 valid number and terminate
        ByteArrayInputStream inputStream5 = buildInputSequenceForStream("800800800");
        when(mockSocket5.getInputStream()).thenReturn(inputStream5);

        Queue<Integer> sharedNumberQueue = new ConcurrentLinkedQueue<>();

        ClientNumberProducer inputReader1 = new ClientNumberProducer(mockSocket1, sharedNumberQueue, mockTerminateSignalSender);
        ClientNumberProducer inputReader2 = new ClientNumberProducer(mockSocket2, sharedNumberQueue, mockTerminateSignalSender);
        ClientNumberProducer inputReader3 = new ClientNumberProducer(mockSocket3, sharedNumberQueue, mockTerminateSignalSender);
        ClientNumberProducer inputReader4 = new ClientNumberProducer(mockSocket4, sharedNumberQueue, mockTerminateSignalSender);
        ClientNumberProducer inputReader5 = new ClientNumberProducer(mockSocket5, sharedNumberQueue, mockTerminateSignalSender);

        Thread inputThread1 = new Thread(inputReader1::doRun);
        Thread inputThread2 = new Thread(inputReader2::doRun);
        Thread inputThread3 = new Thread(inputReader3::doRun);
        Thread inputThread4 = new Thread(inputReader4::doRun);
        Thread inputThread5 = new Thread(inputReader5::doRun);

        inputThread1.start();
        inputThread2.start();
        inputThread3.start();
        inputThread4.start();
        inputThread5.start();

        inputThread1.join();
        inputThread2.join();
        inputThread3.join();
        inputThread4.join();
        inputThread5.join();

        inputStream1.close();
        inputStream2.close();
        inputStream3.close();
        inputStream4.close();
        inputStream5.close();

        assertEquals(10, sharedNumberQueue.size());
        verify(mockSocket1, times(1)).close();
        verify(mockSocket2, times(1)).close();
        verify(mockSocket3, times(1)).close();
        verify(mockSocket4, times(1)).close();
        verify(mockSocket5, times(1)).close();
        verify(mockTerminateSignalSender, times(0)).signalServerStop();
    }

    private ByteArrayInputStream buildInputSequenceForStream(String...inputs) {
        StringBuilder builder = new StringBuilder();
        for(String input : inputs) {
            builder.append(input);
            builder.append(System.lineSeparator());
        }
        return new ByteArrayInputStream(builder.toString().getBytes(StandardCharsets.US_ASCII));
    }
}
