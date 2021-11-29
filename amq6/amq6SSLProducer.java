// camel-k: language=java
// camel-k: dependency=camel:camel-quarkus-activemq
// camel-k: dependency=camel:camel-quarkus-timer
// camel-k: property=period=5000
// camel-k: dependency=mvn:com.github.javafaker:javafaker:1.0.2
// camel-k: dependency=mvn:org.slf4j:slf4j-simple:1.7.32
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

import com.github.javafaker.Faker;

import org.apache.activemq.ActiveMQSslConnectionFactory;
import org.apache.camel.component.activemq.ActiveMQComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.activemq.pool.PooledConnectionFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.camel.BindToRegistry;

import org.apache.camel.PropertyInject;

public class amq6SSLProducer extends RouteBuilder {
  @Inject
  PooledConnectionFactory pooledConnectionFactory;

  @PropertyInject("activemq.destination.brokerURL") 
  String destinationBrokerURL;

  @PropertyInject("activemq.destination.username") 
  String destinationUserName;

  @PropertyInject("activemq.destination.password") 
  String destinationPassword;

  @PropertyInject("activemq.destination.ssl.keyStorePassword") 
  String destinationKeystorePassword;

  @PropertyInject("activemq.destination.ssl.trustStorePassword") 
  String destinationTruststorePassword;

  @PropertyInject("activemq.destination.ssl.trustStoreLocation") 
  String destinationTrustStoreLocation;

  @PropertyInject("activemq.destination.ssl.keyStoreLocation") 
  String destinationKeyStoreLocation;

  Logger logger = LoggerFactory.getLogger(amq6SSLProducer.class.getName());
  
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
    from("timer:foo?fixedRate=true&period={{period}}").bean(this, "generateFakePerson()").to("log:info")
        .to("activemq:{{activemq.destination.type}}:{{activemq.destination.name}}?connectionFactory=#pooledConnectionFactory");
  }

  public String generateFakePerson() {
    Faker faker = new Faker();
    return faker.name().fullName() + " lives on " + faker.address().streetAddress();
  }

  @ApplicationScoped
  public ActiveMQComponent activeMq(PooledConnectionFactory pooledConnectionFactory) {
    ActiveMQComponent activeMqComponent = new ActiveMQComponent();
    activeMqComponent.setConnectionFactory(pooledConnectionFactory);
    activeMqComponent.setCacheLevelName("CACHE_CONSUMER");

    return activeMqComponent;
  }

  @BindToRegistry
  public PooledConnectionFactory pooledConnectionFactory() throws Exception {
    return new PooledConnectionFactory(sslConnectionFactory());
  }

  private ActiveMQSslConnectionFactory sslConnectionFactory() throws Exception {
    ActiveMQSslConnectionFactory connectionFactory = new ActiveMQSslConnectionFactory();
    logger.info("BrokerURL: " + destinationBrokerURL);
    connectionFactory.setBrokerURL(destinationBrokerURL);
    connectionFactory.setUserName(destinationUserName);
    connectionFactory.setPassword(destinationPassword);
    connectionFactory.setTrustStore(destinationTrustStoreLocation);
    connectionFactory.setTrustStorePassword(destinationTruststorePassword);
    connectionFactory.setKeyStore(destinationKeyStoreLocation);
    connectionFactory.setKeyStorePassword(destinationKeystorePassword);

    return connectionFactory;
  }
}
