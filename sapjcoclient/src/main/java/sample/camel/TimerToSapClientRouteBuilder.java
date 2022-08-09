package sample.camel;

import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class TimerToSapClientRouteBuilder extends RouteBuilder {
	
	@Autowired
	CallSapProcessor callSapProcessor;
	
	private static final Logger LOG = LoggerFactory.getLogger(TimerToSapClientRouteBuilder.class);
	public void configure() throws Exception {
		from("timer://foo?repeatCount=1").routeId("route_direct_saprfc")
		.process(callSapProcessor).id("callSapProcessor")
	    .to("file:sapoutput")
	    .log("received ABAP call's result ..... ${body}");
	}
}