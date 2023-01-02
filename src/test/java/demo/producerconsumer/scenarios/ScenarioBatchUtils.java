package demo.producerconsumer.scenarios;

import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ScenarioBatchUtils {

    public static List<StringBuilder> buildBatches(int startVal, int endVal, boolean useRandomVals) {
        List<StringBuilder> numberBatches = new LinkedList<>();
        StringBuilder buff = new StringBuilder();
        for (int i = startVal; i < endVal; i++) {
            // Generate either random or sequential values
            if (useRandomVals) {
                buff.append(String.format("%09d",
                        ThreadLocalRandom.current().nextInt(1, 999999999)));
            }
            else {
                buff.append(String.format("%09d", i));
            }

            buff.append(System.lineSeparator());
            // Batch every 400k values
            if (i != startVal && ((i - startVal) % 400000 == 0)) {
                numberBatches.add(buff);
                buff = new StringBuilder();
            }
        }
        // Put any remaining in a batch
        if (buff.length() > 0) {
            numberBatches.add(buff);
        }

        return numberBatches;
    }

    public static void sendBatches(List<StringBuilder> numberBatches, int id, PrintWriter out) {
        // Send each batch in 1 second intervals and record the time spend writing the batches
        int batchNumber = 0;
        long batchWriteTime = 0;
        long batchStartTime = System.currentTimeMillis();
        for(StringBuilder batch : numberBatches) {
            batchNumber++;
            long startWriteTime = System.currentTimeMillis();
            out.print(batch);
            out.flush();
            long endWriteTime = System.currentTimeMillis();
            long sleepTime = (startWriteTime + 1000) - endWriteTime;
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException ignored) {
                }
            }
            batchWriteTime += (endWriteTime - startWriteTime);
        }

        System.out.println("Producer " + id + " sent " + batchNumber + " batches in " +
                (System.currentTimeMillis() - batchStartTime) + ", write time: " + batchWriteTime);
    }
}
