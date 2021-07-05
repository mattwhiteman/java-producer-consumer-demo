package demo.newrelic.numberlogger.consumer;

import demo.newrelic.numberlogger.logging.NumberLogger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class NumberMessageConsumerTest {

    @Mock
    private NumberLogger mockLogger;

    private NumberMessageConsumer underTest;

    private Queue<Integer> sharedQueue;

    @Test
    public void testSimpleInput() throws InterruptedException {
        sharedQueue = new ConcurrentLinkedQueue<>();

        underTest = new NumberMessageConsumer(sharedQueue, mockLogger);
        underTest.startConsumer();

        Thread producer = createMockProducer("123456789", "123456789", "323456789",
                "423456789", "523456789", "223456789");
        producer.start();
        producer.join();

        Thread.sleep(1000);

        underTest.receiveTerminateSignal();
        underTest.waitForConsumerToFinish();

        verify(mockLogger, times(6)).logNumber(any());
    }

    @Test
    public void testMultipleInput() throws InterruptedException {
        sharedQueue = new ConcurrentLinkedQueue<>();

        underTest = new NumberMessageConsumer(sharedQueue, mockLogger);
        underTest.startConsumer();

        Thread producer1 = createMockProducer("123456789", "223456789", "323456789",
                "423456789", "523456789");
        Thread producer2 = createMockProducer("000000000", "111111111", "000000000");
        Thread producer3 = createMockProducer("222222222", "123456789", "333333333",
                "444444444");
        Thread producer4 = createMockProducer("987654321", "987654322", "987654323",
                "987654324", "987654325", "987654326", "987654326");
        Thread producer5 = createMockProducer("999999999");

        producer1.start();
        producer2.start();
        producer3.start();
        producer4.start();
        producer5.start();

        producer1.join();
        producer2.join();
        producer3.join();
        producer4.join();
        producer5.join();

        Thread.sleep(1000);

        underTest.receiveTerminateSignal();
        underTest.waitForConsumerToFinish();

        verify(mockLogger, times(20)).logNumber(any());
    }

    private Thread createMockProducer(String...numbers) {
        return new Thread(() -> {
            for(String number : numbers) {
                sharedQueue.offer(Integer.valueOf(number));
            }
        });
    }
}
