package sample.camel;

import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.ConnectionFactory;

@Component
public class ServicebusJmsRouteBuilder extends EndpointRouteBuilder {
    @Autowired
    private MessageReceiver receiver;

    @Autowired
    private ConnectionFactory connectionFactory;

    @Override
    public void configure() {
        from(timer("tick").period(1000))
            .setBody(constant("Event Test"))
            .to(jms("q1").connectionFactory(connectionFactory));

        from(jms("q1").connectionFactory(connectionFactory)).
            bean(receiver);
    }
}
