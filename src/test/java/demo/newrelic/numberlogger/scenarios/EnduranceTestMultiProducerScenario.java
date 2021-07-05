package demo.newrelic.numberlogger.scenarios;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

// Generates data using 5 concurrent producers
public class EnduranceTestMultiProducerScenario {

    public void runScenario(String[] args) throws InterruptedException, IOException {
        String serverIP = args.length > 1 ? args[1] : NumberCreatorSimulator.DEFAULT_SERVERIP;
        boolean useRandomVals = args.length > 2 && Boolean.parseBoolean(args[2]);

        Thread p1 = new Thread(() -> sendNumberBatches(serverIP, 0, 4000000, 1, useRandomVals));
        Thread p2 = new Thread(() -> sendNumberBatches(serverIP, 4000000, 8000000, 2, useRandomVals));
        Thread p3 = new Thread(() -> sendNumberBatches(serverIP, 8000000, 12000000, 3, useRandomVals));
        Thread p4 = new Thread(() -> sendNumberBatches(serverIP, 12000000, 16000000, 4, useRandomVals));
        Thread p5 = new Thread(() -> sendNumberBatches(serverIP, 16000000, 20000000, 5, useRandomVals));

        p1.start();
        p2.start();
        p3.start();
        p4.start();
        p5.start();

        p1.join();
        p2.join();
        p3.join();
        p4.join();
        p5.join();

        System.out.println("Producers are finished, sleeping for 20s then sending terminate");
        Thread.sleep(20000);
        Socket clientSocket = new Socket(serverIP, 4000);
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        System.out.println("Sending terminate command");
        out.println("terminate");

        out.close();
        clientSocket.close();
    }

    private void sendNumberBatches(String serverIP, int startVal, int endVal, int id, boolean useRandomVals) {
        System.out.println("Producer " + id + " is creating data");
        try {
            Socket clientSocket = new Socket(serverIP, 4000);
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            // Build batches of 2 million
            List<StringBuilder> numberBatches = ScenarioBatchUtils.buildBatches(startVal, endVal, useRandomVals);
            System.out.println("Producer " + id + " is sending batches");
            ScenarioBatchUtils.sendBatches(numberBatches, id, out);

            out.close();
            clientSocket.close();
        } catch (IOException ignored) {
        }
    }
}
