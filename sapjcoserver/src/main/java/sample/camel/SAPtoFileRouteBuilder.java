package sample.camel;

import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class SAPtoFileRouteBuilder extends RouteBuilder {
	
	private static final Logger LOG = LoggerFactory.getLogger(SAPtoFileRouteBuilder.class);
	public void configure() throws Exception {
		from("direct:saprfc").id("route: direct_saprfc")
	    .log("Processing ..... ${body}")
	    .to("file:sapoutput");
	}
}