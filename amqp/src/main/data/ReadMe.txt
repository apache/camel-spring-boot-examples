Example project which connects to A-MQ 7, using the standard AMQP protocol.

There is the code, from that project, which instantiates component, and sends message

public class CamelRoute extends RouteBuilder {

	@Override
	public void configure() throws Exception {
        from("timer:bar")
            .id("timer-consumer-route")
            .setBody(constant("Hello from Camel"))
            .to("amqp:queue:SCIENCEQUEUE")
            .log("Message sent from route ${routeId} to SCIENCEQUEUE");
	}
}

Please also see the pom file, no need to specify pom versions, 
because they come from the imported BOM.

    <dependency>
        <groupId>org.apache.camel.springboot</groupId>
        <artifactId>camel-amqp-starter</artifactId>
    </dependency>



