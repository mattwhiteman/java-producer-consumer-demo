package demo.producerconsumer.producer;

import demo.producerconsumer.server.signalling.TerminateSignalReceiver;
import demo.producerconsumer.server.signalling.TerminateSignalSender;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class reads data from a connected client socket and queues it for processing by the log consumer.
 * Data is validated to ensure it meets these conditions:
 *   - Must be exactly 9 characters long and comprised of digit characters 0-9
 *   - or, must be a 'terminate' command
 *
 *   Any data that does not meet those requirements is discarded and the client is disconnected
 *   without comment.
 *
 *   Valid data will be sent to the queue for logging, terminate command will cause the application
 *   to signal the server to disconnect all clients and gracefully stop processing data and shutdown.
 */
public class ClientNumberProducer implements TerminateSignalReceiver {
    private final BufferedReader inputReader;
    private final Queue<Integer> sharedNumberQueue;
    private final AtomicBoolean serverActive;
    private final Socket clientSocket;
    private final TerminateSignalSender terminateSignalSender;

    private static final String VALID_INPUT_REGEX = "[0-9]{9}";
    private static final int VALID_INPUT_LENGTH = 9;
    private static final String VALID_INPUT_TERMINATE = "terminate";

    public ClientNumberProducer(Socket clientSocket, Queue<Integer> sharedNumberQueue,
                                TerminateSignalSender terminateSignalSender) throws IOException {
        this.sharedNumberQueue = sharedNumberQueue;
        this.serverActive = new AtomicBoolean(true);
        this.inputReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        this.clientSocket = clientSocket;
        this.terminateSignalSender = terminateSignalSender;
    }

    public void doRun() {
        boolean keepReading = true;
        while(keepReading && serverActive.get()) {
            try {
                String readVal = inputReader.readLine();
                if (serverActive.get()) {
                    keepReading = processInput(readVal);
                }
            } catch (IOException e) {
                keepReading = false;
            }
        }
        IOUtils.closeQuietly(clientSocket, null);
        IOUtils.closeQuietly(inputReader, null);
    }

    private boolean processInput(String input) {
        if (input == null || input.length() != VALID_INPUT_LENGTH) {
            return false;
        }
        else if (input.matches(VALID_INPUT_REGEX)) {
            sharedNumberQueue.offer(Integer.valueOf(input));
            return true;
        }
        else if (VALID_INPUT_TERMINATE.equalsIgnoreCase(input)) {
            terminateSignalSender.signalServerStop();
            return false;
        }
        else {
            return false;
        }
    }

    @Override
    public void receiveTerminateSignal() {
        serverActive.set(false);
        IOUtils.closeQuietly(clientSocket, null);
        IOUtils.closeQuietly(inputReader, null);
    }
}
