// camel-k: language=java
// camel-k: dependency=camel:camel-quarkus-activemq
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

import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;

import org.apache.activemq.ActiveMQSslConnectionFactory;
import org.apache.camel.component.activemq.ActiveMQComponent;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.apache.qpid.jms.JmsConnectionFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.camel.BeanInject;
import org.apache.camel.BindToRegistry;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.apache.camel.PropertyInject;

public class sixToSevenBridge extends RouteBuilder {

  @Inject
  PooledConnectionFactory pooledConnectionFactory;

  @PropertyInject("activemq.source.brokerURL") 
  String sourceBrokerURL;

  @PropertyInject("activemq.source.username") 
  String sourceUserName;

  @PropertyInject("activemq.source.password") 
  String sourcePassword;

  @PropertyInject("activemq.source.ssl.keyStorePassword") 
  String sourceKeystorePassword;

  @PropertyInject("activemq.source.ssl.trustStorePassword") 
  String sourceTruststorePassword;

  @PropertyInject("activemq.source.ssl.trustStoreLocation") 
  String sourceTrustStoreLocation;

  @PropertyInject("activemq.source.ssl.keyStoreLocation") 
  String sourceKeyStoreLocation;

  @PropertyInject("quarkus.qpid-jms.url")
  String destinationBrokerURL;

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
     * - to("jms:{{jms.source
     Type}}:{{jms.source
     Name}}") This sends the
     * fake person data to the source
      configured on the configuration file
     */
    from("activeMQSource:{{activemq.source.type}}:{{activemq.source.name}}").to("log:info")
    .to("jms:{{jms.destinationType}}:{{jms.destinationName}}?connectionFactory=artemisConnectionFactory");
  }

  @BindToRegistry("artemisConnectionFactory")
  public JmsConnectionFactory connectionFactory() throws Exception {
    return new JmsConnectionFactory(destinationBrokerURL);
  }

  @BindToRegistry("activeMQSource")
  public ActiveMQComponent activeMQSource() throws Exception{
    ActiveMQComponent activeMqComponent = new ActiveMQComponent();
    activeMqComponent.setConnectionFactory(pooledConnectionFactorySource());
    activeMqComponent.setCacheLevelName("CACHE_CONSUMER");

    return activeMqComponent;
  }

  @BindToRegistry("pooledConnectionFactorySource")
  public PooledConnectionFactory pooledConnectionFactorySource() throws Exception {
    return new PooledConnectionFactory(sslConnectionFactorySource());
  }


  private ActiveMQSslConnectionFactory sslConnectionFactorySource() throws Exception {
    ActiveMQSslConnectionFactory connectionFactory = new ActiveMQSslConnectionFactory();
    System.out.println("BrokerURL: " + sourceBrokerURL);
    connectionFactory.setBrokerURL(sourceBrokerURL);
    connectionFactory.setUserName(sourceUserName);
    connectionFactory.setPassword(sourcePassword);
    connectionFactory.setTrustStore(sourceTrustStoreLocation);
    connectionFactory.setTrustStorePassword(sourceTruststorePassword);
    connectionFactory.setKeyStore(sourceKeyStoreLocation);
    connectionFactory.setKeyStorePassword(sourceKeystorePassword);

    return connectionFactory;
  }
}
