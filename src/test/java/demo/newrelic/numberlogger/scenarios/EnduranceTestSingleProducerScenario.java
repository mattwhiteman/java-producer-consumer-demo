package demo.newrelic.numberlogger.scenarios;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

// Generates data using a single producer
public class EnduranceTestSingleProducerScenario {

    private static final int DEFAULT_STARTVAL = 0;

    public void runScenario(String[] args) throws InterruptedException, IOException {
        // Process parameters
        int startVal = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_STARTVAL;
        boolean sendTerminate = args.length <= 2 || Boolean.parseBoolean(args[2]);
        String serverIP = args.length > 3 ? args[3] : NumberCreatorSimulator.DEFAULT_SERVERIP;
        boolean useRandomVals = args.length > 4 && Boolean.parseBoolean(args[4]);

        // Send 400k total numbers
        int endVal = startVal + 4000000;

        // Open the socket connection
        Socket clientSocket = new Socket(serverIP, 4000);
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

        System.out.println("Producer 1 is batching data");

        // Build the data in batches of 100k messages
        List<StringBuilder> numberBatches = ScenarioBatchUtils.buildBatches(startVal, endVal, useRandomVals);
        System.out.println("Producer 1 is sending batches");
        ScenarioBatchUtils.sendBatches(numberBatches, 1, out);

        // Wait 20 seconds then send the terminate command
        Thread.sleep(20000);
        if (sendTerminate) {
            System.out.println("Sending terminate command");
            out.println("terminate");
            out.flush();
        }

        out.close();
        clientSocket.close();
    }
}
