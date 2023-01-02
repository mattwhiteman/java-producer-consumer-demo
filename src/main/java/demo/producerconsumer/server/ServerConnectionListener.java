package demo.producerconsumer.server;

import demo.producerconsumer.constants.ApplicationConstants;
import demo.producerconsumer.producer.ClientNumberProducer;
import demo.producerconsumer.server.signalling.TerminateSignalReceiver;
import demo.producerconsumer.server.signalling.TerminateSignalSender;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Listener module responsible for handling incoming connections and creating a new client thread
 * to process data from the connected socket. Uses a thread pool with a fixed size of 5 allowed
 * connections. Additional connections will wait in the executor queue until a thread is available.
 */
public class ServerConnectionListener implements TerminateSignalReceiver {
    private final ServerSocket serverSocket;
    private final ExecutorService threadPoolExecutor;
    private final Queue<Integer> numberMessageQueue;
    private final TerminateSignalSender terminateSignalSender;

    public ServerConnectionListener(Queue<Integer> numberMessageQueue, TerminateSignalSender terminateSignalSender) throws IOException {
        this.serverSocket = new ServerSocket(ApplicationConstants.SERVER_PORT);
        this.threadPoolExecutor = Executors.newFixedThreadPool(ApplicationConstants.MAX_CLIENT_THREADS);
        this.numberMessageQueue = numberMessageQueue;
        this.terminateSignalSender = terminateSignalSender;
    }

    public void listenForClients() {
        try {
            while (!threadPoolExecutor.isShutdown()) {
                Socket clientSocket = serverSocket.accept();
                try {
                    // Create a new client handler, register it with the signal receiver, and
                    // send to the execution service.
                    ClientNumberProducer clientReader = new ClientNumberProducer(
                            clientSocket, numberMessageQueue, terminateSignalSender);
                    terminateSignalSender.registerSignalReceiver(clientReader);
                    submitClientForExecution(clientReader);
                } catch (RejectedExecutionException | IOException e) {
                    IOUtils.closeQuietly(clientSocket, null);
                }
            }
        }
        catch (IOException ignored) {
            // If an error occurs, close the socket gracefully. This can also occur when
            // the termination signal is received while blocking on the accept() call, in
            // which case the socket has already been cleaned up but another call here will
            // not harm anything.
            IOUtils.closeQuietly(serverSocket, null);
        }
        finally {
            try {
                // Wait for the thread pool to finish for 1 second.
                threadPoolExecutor.awaitTermination(ApplicationConstants.THREADPOOL_EXPECTED_SHUTDOWN_TIME_MS, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ignored) {
            }
        }
    }

    private synchronized void submitClientForExecution(ClientNumberProducer client) {
        threadPoolExecutor.execute(() -> {
            client.doRun();
            terminateSignalSender.unregisterSignalReceiver(client);
        });
    }

    @Override
    public synchronized void receiveTerminateSignal() {
        threadPoolExecutor.shutdown();
        IOUtils.closeQuietly(serverSocket, null);
    }
}
