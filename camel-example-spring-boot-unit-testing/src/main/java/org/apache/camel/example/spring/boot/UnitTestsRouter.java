/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one or more
 *  * contributor license agreements.  See the NOTICE file distributed with
 *  * this work for additional information regarding copyright ownership.
 *  * The ASF licenses this file to You under the Apache License, Version 2.0
 *  * (the "License"); you may not use this file except in compliance with
 *  * the License.  You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */
package org.apache.camel.example.spring.boot;

import java.util.ArrayList;

import org.apache.camel.builder.RouteBuilder;

import org.springframework.stereotype.Component;

import static org.apache.camel.builder.AggregationStrategies.flexible;

@Component
public class UnitTestsRouter extends RouteBuilder {
	@Override
	public void configure() throws Exception {

		// @formatter:off

		// Example 5 : Simulating error from mocked endpoint
		onException(MockSimulatedErrorException.class)
				.handled(true)
				.to("mock:g");

		// Example 6 : Simulating error from mocked endpoint
		onException(InterceptorSimulatedErrorException.class)
				.handled(true)
				.to("mock:j");

		// Example 1 : Simple mock testing
		from("direct:simple-mock")
				.to("mock:a");

		// Example 2 : Mock testing with expects expression
		from("direct:mock-expects-expression")
				.to("mock:b");

		// Example 3 : Mock testing with descending counter expression
		from("direct:mock-expects-descending")
				.to("mock:c");

		// Example 3 : Mock testing with ascending counter expression
		from("direct:mock-expects-ascending")
				.to("mock:d");

		// Example 4 : Providing canned response from mocked endpoint
		from("direct:mock-response")
				.to("mock:e");

		// Example 5 : Simulating error from mocked endpoint
		from("direct:mock-simulate-error")
				.to("mock:f");

		// Example 6 : Simulating error with interceptor
		from("direct:interceptor-simulate-error").routeId("example6")
				.to("mock:h");

		// Example 7 : Advising and mocking endpoints
		from("direct:advice-mock-endpoints").routeId("example7")
				.choice()
					.when(simple("${body} contains 'K'"))
						.to("seda:k")
					.otherwise()
						.to("seda:l");

		// Example 8 : Advising and mocking endpoints
		from("jms:abc").routeId("example8")
				.choice()
					.when(simple("${body} contains 'M'"))
				.to("seda:m")
					.otherwise()
				.to("seda:n");

		// Example 9 : Weaving processor by ID
		from("direct:advice-weave-by-id-replace").routeId("example9")
				.transform(simple("${body.toLowerCase()}")).id("node9");

		// Example 10 : Weaving producer by ID and replacing node
		from("direct:advice-producer-by-id-replace").routeId("example10")
				.split(body(), flexible().accumulateInCollection(ArrayList.class))
					.transform(simple("${body.toLowerCase()}"))
					.to("seda:p")
				.end()
				.to("mock:q");

		// Example 11 : Weaving producer by ID and removing node
		from("direct:advice-weave-by-id-remove").routeId("example11")
				.transform(simple("${body.toLowerCase()}")).id("node11");

		// Example 12 : Weaving node by type
		from("direct:advice-weave-by-type").routeId("example12")
				.split(body(), flexible().accumulateInCollection(ArrayList.class))
					.transform(simple("${body.toLowerCase()}"))
					.to("mock:t")
				.end()
				.to("mock:u");

		// Example 13 : Selecting specific node using select* method
		from("direct:advice-weave-by-type-and-select").routeId("example13")
				.split(body(), flexible().accumulateInCollection(ArrayList.class))
					.transform(simple("${body.toUpperCase()}"))
					.to("mock:v")
				.end()
				.to("mock:w");
		// @formatter:on

	}

}
