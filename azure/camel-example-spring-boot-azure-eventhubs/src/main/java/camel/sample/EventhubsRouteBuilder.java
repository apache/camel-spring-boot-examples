package camel.sample;

import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EventhubsRouteBuilder extends EndpointRouteBuilder {

    @Value("${namespaceName}")
    private String namespaceName;

    @Value("${eventhubsName}")
    private String eventhubsName;

    @Autowired
    private MessageReceiver receiver;

    @Override
    public void configure() {
        from(timer("tick")
            .period(1000))
            .setBody(constant("Event Test"))
            .to(azureEventhubs(namespaceName + "/" + eventhubsName));

        from(azureEventhubs(namespaceName + "/" + eventhubsName))
            .bean(receiver)
            .log("The content is ${body}");

    }
}
