package demo.newrelic.numberlogger.scenarios;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class BasicTestScenario {

    public void runScenario(String[] args) throws IOException, InterruptedException {
        String serverIP = args.length > 1 ? args[1] : NumberCreatorSimulator.DEFAULT_SERVERIP;

        System.out.println("Sending 5 uniques and 1 invalid input");
        Socket clientSocket = new Socket(serverIP, 4000);
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        out.println("000000000");
        out.println("111111111");
        out.println("222222222");
        out.println("333333333");
        out.println("444444444");
        out.println("abc1");

        Thread.sleep(500);
        out.close();
        clientSocket.close();

        Thread.sleep(1000);

        System.out.println("Sending 2 uniques, 3 duplicates, and 1 invalid input");
        clientSocket = new Socket(serverIP, 4000);
        out = new PrintWriter(clientSocket.getOutputStream(), true);

        out.println("000000000");
        out.println("555555555");
        out.println("222222222");
        out.println("666666666");
        out.println("444444444");
        out.println("77777777");
        Thread.sleep(500);
        out.close();
        clientSocket.close();

        Thread.sleep(1000);

        System.out.println("Simulating several connect/disconnects with invalid inputs");
        clientSocket = new Socket(serverIP, 4000);
        out = new PrintWriter(clientSocket.getOutputStream(), true);

        out.println("123");
        Thread.sleep(100);
        out.println("123");
        Thread.sleep(100);
        out.println("123");
        Thread.sleep(100);
        out.println("123");
        Thread.sleep(100);
        out.println("123");
        Thread.sleep(100);
        out.println("123");

        Thread.sleep(500);
        out.close();
        clientSocket.close();

        Thread.sleep(1000);

        System.out.println("Opening 6 sockets, sending 4 unique, 1 duplicate, terminate, and 2 inputs that should not be read");
        Socket clientSocket1 = new Socket(serverIP, 4000);
        Socket clientSocket2 = new Socket(serverIP, 4000);
        Socket clientSocket3 = new Socket(serverIP, 4000);
        Socket clientSocket4 = new Socket(serverIP, 4000);
        Socket clientSocket5 = new Socket(serverIP, 4000);
        Socket clientSocket6 = new Socket(serverIP, 4000);
        PrintWriter out1 = new PrintWriter(clientSocket1.getOutputStream(), true);
        PrintWriter out2 = new PrintWriter(clientSocket2.getOutputStream(), true);
        PrintWriter out3 = new PrintWriter(clientSocket3.getOutputStream(), true);
        PrintWriter out4 = new PrintWriter(clientSocket4.getOutputStream(), true);
        PrintWriter out5 = new PrintWriter(clientSocket5.getOutputStream(), true);
        PrintWriter out6 = new PrintWriter(clientSocket6.getOutputStream(), true);

        out1.println("123456789");
        out2.println("987654321");
        out3.println("123451234");
        out4.println("000000000");
        out5.println("543215432");
        out6.println("888888888"); // This should not end up being processed

        Thread.sleep(1000);

        out1.println("terminate");
        out1.println("005550055"); // This should not end up being processed
        Thread.sleep(1000);

        out1.close();
        clientSocket1.close();
        out2.close();
        clientSocket2.close();
        out3.close();
        clientSocket3.close();
        out4.close();
        clientSocket4.close();
        out5.close();
        clientSocket5.close();
        out6.close();
        clientSocket6.close();

        System.out.println("Scenario finished. Expected 11 Total Uniques, 4 Total Duplicates");
    }
}
