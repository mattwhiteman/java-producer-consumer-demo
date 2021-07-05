package demo.newrelic.numberlogger.scenarios;

import java.io.IOException;

public class NumberCreatorSimulator {

    public static final String DEFAULT_SERVERIP = "localhost";
    public static void main(String[] args) throws IOException, InterruptedException {
        String scenario = args.length > 0 ? args[0] : "default";

        if (scenario.equalsIgnoreCase("endmulti")) {
            System.out.println("Running endurance test with multiple producers");
            new EnduranceTestMultiProducerScenario().runScenario(args);
        }
        else if (scenario.equalsIgnoreCase("endsingle")) {
            System.out.println("Running endurance test with single producer");
            new EnduranceTestSingleProducerScenario().runScenario(args);
        }
        else {
            System.out.println("Running basic scenario test");
            new BasicTestScenario().runScenario(args);
        }
    }
}
