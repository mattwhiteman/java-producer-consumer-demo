# newrelic-number-logger
Demo application for newrelic interview

**Assumptions:**

- All input is interpreted as single-byte ascii.

- For inputs that should be treated as "correct" and processed, clients will provide any leading 0's as
  necessary to ensure input is 9 characters and meets validation requirements. IE, an input of "123" would be invalid, the
  server will not pad with leading 0's to make the input valid.

- When all 5 client threads are being utilized, new connections will sit in the executor queue and not have data read
  until a thread is available.

- No preference or priority is given to any client thread. The order of numbers received from individual clients is
  preserved when logging, but no guarantee of order is given across threads.

- The "terminate" command is case insensitive.

- When a terminate notice is received, any input still in the client sockets is not processed, nor are any
  numbers received by the clients but not yet logged. The buffer containing numbers already logged will be flushed to disk.

- The logfile is a static name "numbers.log" and will be created in the working directory the program is launched from.
  It is assumed that the program is launched with all necessary permissions to execute the jar, and create and write to a
  file.

- Numbers are written to the log file with leading zeroes preserved. Every entry in the log file will be exactly
  9 characters followed by a server-native newline.

- The server runs on TCP port 4000, and it is assumed this port is available to bind to on the local machine.

- Internal design decisions and implementations were based on knowledge about the data set scope, size, and range. This
  allowed for optimizations that might not have been possible for a generic producer-consumer application that handles
  String data of any type and length.

- Instructions for building and executing are given using commands and directories in linux/OS X style commands and paths.
  The project will work on Windows but it is up to the reader to use the appropriate command and path styles.


**Build:**

This project is built with gradle 6.8, which also downloads any necessary compile and test dependencies from the central
maven repository. The gradle wrapper is provided so that the project can be built on machines that do not have gradle
installed. An active network connection is required to download dependencies from the maven repository.

To build the project, run `./gradlew clean build` from the project root.

**Execute:**

The project uses Java 8 to run, and it is assumed that the machine already has Java installed and configured. Gradle will
build an executable fat jar named 'newrelic-number-logger.jar', which can be run from any desired directory.
To run the server from the project root after building with the above gradle command, run
`java -jar ./build/libs/newrelic-number-logger.jar`

**Test:**

Unit tests for several modules are run as part of the gradle build process. Gradle will also build a test jar that can
be used for simulating input data to a running server. This jar is named 'clientSimulator.jar' and will be in the
build/libs directory along with the main server jar. Note that the simulator does *not* start a server instance and this
must be done manually by the user.

The clientSimulator jar can run 3 different test scenarios and supports several parameter flags for each. To execute a
particular scenario, provide the name (basic, endsingle, endmulti) as a command line parameter when running the jar. By
default, the basic scenario will be run if no test scenario is specified on the command line. Note that parameter index
0 is always occupied by the scenario name mentioned above.

- basic:

Posts a small amount of valid and invalid data to the server, tests robustness by making several client
connect/disconnects, opens more than 5 connections simultaneously to verify expected behavior, and tests the terminate
command. Supported the following parameters:

1 - ip address to connect to for sending data, default is localhost

- endsingle:

Endurance test that sends 4 batches of 100k numbers every 1 second to the server. Only one connection is made to the
server. This is useful to run multiple instances of to simulate multiple clients. Supported parameters by index:

1 - Starting value to use when sending sequential data, default is 0

2 - boolean indicating whether to send the terminate command at the end, default is true

3 - ip address to connect to for sending data, default is localhost

4 - generate random numbers instead of sequential values based on the start value, default is false

Note that all parameters are optional, but all previous index parameters must be included when specifying a parameter.
For example, to specify the IP address to connect to, the starting value and terminate boolean value must also be
provided since they are in earlier indexes.

- multisingle:
Endurance test that creates 5 producers to concurrently send 4 batches of 100k sequential numbers each every 1 second to the
server, for a total of 2M numbers. These numbers by default are sequential and will not contain any duplicates. Because
this runs in a single JVM, it is not as fast as 5 separate producers in separate instances. This test is still useful
to ensure large interleaved data from multiple connections can be handled.
Supported parameters by index:

1 - ip address to connect to for sending data, default is localhost

2 - generate random numbers instead of sequential values based on the start value, default is false

**Examples:**

Runs the basic scenario for a server running a different machine with IP address 192.168.1.187
`java -jar ./build/libs/clientSimulator.jar basic 192.168.1.187`

Runs the endsingle scenario with sequential data starting at 0, with the terminate command, for a server running on IP address 192.168.1.187
`java -jar ./build/libs/clientSimulator.jar endsingle 0 true 192.168.1.187`

Runs the endmulti scenario for a server running on IP address 192.168.1.187 and uses random instead of sequential data.
`java -jar ./build/libs/clientSimulator.jar endmulti 192.168.1.187 true`
