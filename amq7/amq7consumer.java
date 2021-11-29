// camel-k: language=java
// camel-k: dependency=mvn:org.amqphub.quarkus:quarkus-qpid-jms
/*
 * The above statements provide information required for running the example. This includes
 * the metadata informing the language used by this code and the dependencies used by
 * Camel K to run this example.
 * As for the dependencies, these are:
 * - camel-quarkus-jms and camel-quarkus-timer, which are from Camel, thus resolved
 * automatically (hence the prefix notation "camel:")
 * - The fully qualified Maven name of JavaFaker dependency, used to generate fake data
 */

import org.apache.camel.builder.RouteBuilder;

public class amq7consumer extends RouteBuilder {

  @Override
  public void configure() throws Exception {
    /*
     * Explanation, method by method:
     *
     * - from("timer:{{period}}") Generate time-based events are a regular interval
     * defined by "period". The default period is 1 second - configured above - but
     * can be overriden using the --property flag (i.e.: --property
     * period=newPeriodValue)
     *
     * - bean(this, "generateFakePerson()") The code call the method
     * generateFakerPerson on this' object instance in order to generate a fake
     * person and a fake address.
     *
     * - to("log:info") Log the generated fake person name and address to the logger
     * using the info level
     *
     * This generates a log message that looks like this: (redacted...)
     * ExchangePattern: InOnly, BodyType: String, Body: So Effertz I lives on 967
     * Richie Ports
     *
     * - to("jms:{{jms.destinationType}}:{{jms.destinationName}}") This sends the
     * fake person data to the destination configured on the configuration file
     */
    from("jms:{{jms.destinationType}}:{{jms.destinationName}}").to("log:info");
  }
}