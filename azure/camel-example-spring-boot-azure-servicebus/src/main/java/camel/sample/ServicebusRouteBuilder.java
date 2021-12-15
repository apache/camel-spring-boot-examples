package camel.sample;

import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ServicebusRouteBuilder extends EndpointRouteBuilder {

    @Autowired
    private MessageReceiver receiver;

    //update queueName for servicebus instance
    private static final String queueName = "";
    @Override
    public void configure() {
        from(timer("tick").period(1000))
            .setBody(constant("Event Test"))
            .to(azureServicebus(queueName));


        from(azureServicebus(queueName))
            .bean(receiver)
            .log("The content is ${body}");
    }
}
