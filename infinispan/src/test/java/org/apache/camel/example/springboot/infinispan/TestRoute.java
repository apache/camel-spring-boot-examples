package org.apache.camel.example.springboot.infinispan;

import org.apache.camel.builder.RouteBuilder;

import org.springframework.stereotype.Component;

@Component
public class TestRoute extends RouteBuilder {
	@Override
	public void configure() throws Exception {
		from("direct:test")
				.to("direct:put-cache")
				.to("mock:result");
	}
}
