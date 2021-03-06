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
import java.util.List;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.example.spring.boot.Application;
import org.apache.camel.example.spring.boot.InterceptorSimulatedErrorException;
import org.apache.camel.example.spring.boot.MockSimulatedErrorException;
import org.apache.camel.model.SplitDefinition;
import org.apache.camel.model.ToDefinition;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.apache.camel.builder.Builder.body;
import static org.apache.camel.builder.Builder.simple;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@CamelSpringBootTest
@SpringBootTest(classes = Application.class)
public class CamelSpringBootUnitTests {

	@Autowired
	private CamelContext camelContext;

	@Autowired
	private ProducerTemplate producerTemplate;

	// @formatter:off
	@Test
	public void testSimpleMock() throws InterruptedException {
		// Getting mock endpoint object from the context
		MockEndpoint mockA = camelContext.getEndpoint("mock:a", MockEndpoint.class);

		// setting expectation for a message body received by mock using one the expect* methods
		mockA.expectedBodiesReceived("Hello world");
		// setting expectation for a message body received by mock using one the expect* methods
		mockA.message(0).header("Counter").isEqualTo(1);

		// sending data to route consumer
		producerTemplate.sendBodyAndHeader("direct:simple-mock", "Hello world", "Counter", 1);

		// asserting expectations are satisfied
		mockA.assertIsSatisfied();
	}

	@Test
	public void testMockWithCustomExpect() throws Exception {
		// Getting mock endpoint object from the context
		MockEndpoint mockB = camelContext.getEndpoint("mock:b", MockEndpoint.class);

		// setting expectation for a number of messages that will arrive at mock
		mockB.expectedMessageCount(3);

		// expects method allow providing custom logic to verify expectations
		mockB.expects(() -> mockB.getExchanges().stream()
				.reduce(0, (previous, exchange) -> {
					int current = exchange.getIn().getHeader("Counter", Integer.class);
					if (current <= previous){
						// We fail explicitly if counter current counter is less than previous
						fail ("Counter is not greater than previous counter");
					} else if (current - previous != 1){
						// We fail explicitly if gap more than 1 detected between previous and current counter
						fail("Gap detected: previous: " + previous + " current: " + current);
					}
					return current;
					// Combiner
				}, (prev, current) -> current));

		// Change to non consequent counter number to see test fail
		// Sending test data to route consumer
		producerTemplate.sendBodyAndHeader("direct:mock-expects-expression", "A", "Counter", 1);
		producerTemplate.sendBodyAndHeader("direct:mock-expects-expression", "A", "Counter", 2);
		producerTemplate.sendBodyAndHeader("direct:mock-expects-expression", "A", "Counter", 3);

		// Asserting expectations are satisfied
		mockB.assertIsSatisfied();
	}

	@Test
	public void testMockExpectDescending() throws Exception {
		// Getting mock endpoint object from the context
		MockEndpoint mockC = camelContext.getEndpoint("mock:c", MockEndpoint.class);

		// setting expectation for a number of messages that will arrive at mock
		mockC.expectedMessageCount(3);

		// setting expectation for a attribute, in this case defined in header to be in a descending order
		mockC.expectsDescending().header("Counter");
		// setting expectation for a message body
		mockC.allMessages().body().isEqualTo("A");

		// Change to non consequent counter number to see test fail
		// Sending test data to route consumer
		// TODO 7 : Does not work exactly descending 1, 2, 1
		producerTemplate.sendBodyAndHeader("direct:mock-expects-descending", "A", "Counter", 3);
		producerTemplate.sendBodyAndHeader("direct:mock-expects-descending", "A", "Counter", 2);
		producerTemplate.sendBodyAndHeader("direct:mock-expects-descending", "A", "Counter", 1);

		// Asserting expectations are satisfied
		mockC.assertIsSatisfied();
	}

	@Test
	public void testMockExpectAscending() throws Exception {
		// Getting mock endpoint object from the context
		MockEndpoint mockD = camelContext.getEndpoint("mock:d", MockEndpoint.class);

		// setting expectation for a number of messages that will arrive at mock
		mockD.expectedMessageCount(3);

		// setting expectation for a attribute, in this case defined in header to be in a ascending order
		mockD.expectsAscending().header("Counter");
		// setting expectation for a message body to contain no duplicates
		mockD.expectsNoDuplicates().body();

		// Change to non consequent counter number to see test fail
		// Sending test data to route consumer
		producerTemplate.sendBodyAndHeader("direct:mock-expects-ascending", "A", "Counter", 1);
		producerTemplate.sendBodyAndHeader("direct:mock-expects-ascending", "B", "Counter", 2);
		producerTemplate.sendBodyAndHeader("direct:mock-expects-ascending", "C", "Counter", 3);

		// asserting expectations are satisfied
		mockD.assertIsSatisfied();
	}

	@Test
	public void testMockComponent() throws Exception {
		// Getting mock endpoint object from the context
		MockEndpoint mockE = camelContext.getEndpoint("mock:e", MockEndpoint.class);

		// setting expectation for a number of messages that will arrive at mock
		mockE.expectedMessageCount(2);
		// setting expectation for the contents of messages that will arrive at mock
		mockE.expectedBodiesReceived("1", "2");

		// defining behavior of a mock by providing a canned stubbed response to any exchanges arrived
		mockE.whenAnyExchangeReceived(e -> e.getIn().setBody("B"));
		// defining behavior of a mock by providing a canned stubbed response for a particular exchange by index
		mockE.whenExchangeReceived(2, e -> e.getIn().setBody("C"));

		// Change to non consequent counter number to see test fail
		// Sending test data to route consumer and obtaining response messages for further validation
		final String mockResponse1 = producerTemplate.requestBody("direct:mock-response", "1", String.class);
		final String mockResponse2 = producerTemplate.requestBody("direct:mock-response", "2", String.class);

		// Asserting expectations are satisfied
		mockE.assertIsSatisfied();
		// Asserting expectations towards message body contents are satisfied
		assertEquals("B", mockResponse1);
		assertEquals("C", mockResponse2);
	}

	@Test
	public void testMockSimulateError() throws Exception {
		// Getting mock endpoint object from the context
		MockEndpoint mockF = camelContext.getEndpoint("mock:f", MockEndpoint.class);
		MockEndpoint mockG = camelContext.getEndpoint("mock:g", MockEndpoint.class);

		// defining behavior of a mock by providing a canned stubbed response (exception) to any exchanges arrived
		mockF.whenAnyExchangeReceived(e -> e.setException(new MockSimulatedErrorException("Simulated error")));

		// setting expectation for the contents of messages that will arrive at mock
		mockG.expectedBodiesReceived("A");

		// Sending test data to route consumer
		producerTemplate.sendBody("direct:mock-simulate-error", "A");

		// Asserting expectations are satisfied
		mockF.assertIsSatisfied();
		mockG.assertIsSatisfied();
	}

	@Test
	public void testMockSimulateErrorWithInterceptor() throws Exception {
		// Getting mock endpoint object from the context
		MockEndpoint mockH = camelContext.getEndpoint("mock:h", MockEndpoint.class);
		MockEndpoint mockJ = camelContext.getEndpoint("mock:j", MockEndpoint.class);

		// Advising endpoint in AOP style by providing routeId
		AdviceWith.adviceWith(camelContext, "example6",
						// intercepting an exchange on route to the endpoint
				a -> a.interceptSendToEndpoint("mock:h")
						// skipping sending exchange to the original endpoint
						.skipSendToOriginalEndpoint()
						// Throwing an error to simulate failure
						.process(exchange -> {
							throw new InterceptorSimulatedErrorException("Error");
						}));

		// setting expectation for the absence of messages that will arrive at mock
		mockH.expectedMessageCount(0);
		// setting expectation for the contents of messages that will arrive at mock
		mockJ.expectedBodiesReceived("A");

		// Sending test data to route consumer
		producerTemplate.sendBody("direct:interceptor-simulate-error", "A");

		// Asserting expectations are satisfied
		mockH.assertIsSatisfied();
		mockJ.assertIsSatisfied();
	}

	@Test
	public void testAdvisingRouteAndMockingEndpoints() throws Exception {
		// Advising endpoint in AOP style by providing routeId
		AdviceWith.adviceWith(camelContext, "example7",
				// mocking all endpoint in advised route
				AdviceWithRouteBuilder::mockEndpoints);

		// Getting mock endpoint object from the context
		MockEndpoint mockK = camelContext.getEndpoint("mock:seda:k", MockEndpoint.class);
		MockEndpoint mockL = camelContext.getEndpoint("mock:seda:l", MockEndpoint.class);

		// setting expectation for the contents of messages that will arrive at mock
		mockK.expectedBodiesReceived("K");
		mockL.expectedBodiesReceived("L");

		// Sending test data to route consumer
		producerTemplate.sendBody("direct:advice-mock-endpoints", "K");
		producerTemplate.sendBody("direct:advice-mock-endpoints", "L");

		// Asserting expectations are satisfied
		mockK.assertIsSatisfied();
		mockL.assertIsSatisfied();
	}


	@Test
	public void testAdvisedRouteAndWeaveConsumerFromWith() throws Exception {
		// Getting mock endpoint object from the context
		MockEndpoint mockM = camelContext.getEndpoint("mock:seda:m", MockEndpoint.class);
		MockEndpoint mockN = camelContext.getEndpoint("mock:seda:n", MockEndpoint.class);

		// Advising endpoint in AOP style by providing routeId
		AdviceWith.adviceWith(camelContext, "example8",
				a -> {
						// Replacing a consumer (from) with a another consumer
						a.replaceFromWith("direct:advice-replace-consumer");
						// Mocking particular endpoint by providing a pattern match
						a.mockEndpoints("seda:*");
				});

		// setting expectation for the contents of messages that will arrive at mock
		mockM.expectedBodiesReceived("M");
		mockN.expectedBodiesReceived("N");

		// Sending test data to route consumer
		producerTemplate.sendBody("direct:advice-replace-consumer", "M");
		producerTemplate.sendBody("direct:advice-replace-consumer", "N");

		// Asserting expectations are satisfied
		mockM.assertIsSatisfied();
		mockN.assertIsSatisfied();
	}

	@Test
	public void testAdvisingRouteAndWeaveByIdAndReplacingNode() throws Exception {
		// Advising endpoint in AOP style by providing routeId
		AdviceWith.adviceWith(camelContext, "example9",
				a -> { 		// weaving particular node in the route by id
					a.weaveById("node9")
							// providing advised (weaved) node replacement
							.replace().transform(simple("${body.toUpperCase()}"));
					// adding new (mock) node to the route definition
					a.weaveAddLast().to("mock:o"); });

		// Getting mock endpoint object from the context
		MockEndpoint mockO = camelContext.getEndpoint("mock:o", MockEndpoint.class);



		// setting expectation for the contents of messages that will arrive at mock
		mockO.expectedBodiesReceived("HELLO WORLD");

		// Sending test data to route consumer
		producerTemplate.sendBody("direct:advice-weave-by-id-replace", "Hello World");

		// Asserting expectations are satisfied
		mockO.assertIsSatisfied();
	}


	@Test
	public void testAdvisingRouteAndProducerWeaveByToUriAndReplaceNode() throws Exception {
		// Getting mock endpoint object from the context
		MockEndpoint mockP = camelContext.getEndpoint("mock:p", MockEndpoint.class);
		MockEndpoint mockQ = camelContext.getEndpoint("mock:q", MockEndpoint.class);

		// Advising endpoint in AOP style by providing routeId
		AdviceWith.adviceWith(camelContext, "example10",
				a -> { 		// weaving particular node in the route by URI
					 	 	a.weaveByToUri("seda:p")
							// providing advised (weaved) node replacement
							.replace().to("mock:p"); });

		// setting expectation for the contents of messages that will arrive at mock
		mockP.expectedBodiesReceived("a", "b", "c");
		mockQ.message(0).body().isInstanceOf(List.class);

		// Sending test data to route consumer
		producerTemplate.sendBody("direct:advice-producer-by-id-replace", "a,b,c");

		// Asserting expectations are satisfied
		mockQ.assertIsSatisfied();
		mockP.assertIsSatisfied();
	}

	@Test
	public void testAdvisingRouteAndWeaveByIdAndRemoveNode() throws Exception {
		// Advising endpoint in AOP style by providing routeId
		AdviceWith.adviceWith(camelContext, "example11",
				a -> { 	// adding a node at the end of the route definition
					a.weaveAddLast().to("mock:r");
					// weaving particular node in the route by URI
					a.weaveById("node11")
							// and removing advised node
							.remove();
				});

		// Getting mock endpoint object from the context
		MockEndpoint mockR = camelContext.getEndpoint("mock:r", MockEndpoint.class);

		// setting expectation for the contents of messages that will arrive at mock
		mockR.expectedBodiesReceived("Hello World");

		// Sending test data to route consumer
		producerTemplate.sendBody("direct:advice-weave-by-id-remove", "Hello World");

		// Asserting expectations are satisfied
		mockR.assertIsSatisfied();
	}

	@Test
	public void testAdvisingRouteAndWeaveByIdAndAddNode() throws Exception {
		// Advising endpoint in AOP style by providing routeId
		AdviceWith.adviceWith(camelContext, "example12",
						// find the splitter node
				a -> a.weaveByType(SplitDefinition.class)
						// and insert the route snippet before it
						.before()
							// definition of the newly added node
							.filter(body().contains("c"))
							.transform(simple("${body} advised")));

		// Getting mock endpoint object from the context
		MockEndpoint mockT = camelContext.getEndpoint("mock:t", MockEndpoint.class);
		MockEndpoint mockU = camelContext.getEndpoint("mock:u", MockEndpoint.class);

		// setting expectation for the contents of messages that will arrive at mock
		mockT.expectedBodiesReceived("a", "b", "c advised");
		mockU.message(0).body().isInstanceOf(List.class);

		// Sending test data to route consumer
		producerTemplate.sendBody("direct:advice-weave-by-type", "a,b,c");

		// Asserting expectations are satisfied
		mockT.assertIsSatisfied();
		mockU.assertIsSatisfied();

		// Resetting mock endpoints and removing weaved new node
		MockEndpoint.resetMocks(camelContext);

		// Sending test data to route consumer - this time without
		producerTemplate.sendBody("direct:advice-weave-by-type", "a,b,c");
		mockT.expectedBodiesReceived("a", "b", "c");
		mockU.message(0).body().isInstanceOf(List.class);

		// Asserting expectations are satisfied
		mockT.assertIsSatisfied();
		mockU.assertIsSatisfied();
	}

	@Test
	public void testAdvisingRouteAndWeaveByTypeAndReplacingNodeWithSelect() throws Exception {
		// Advising endpoint in AOP style by providing routeId
		AdviceWith.adviceWith(camelContext, "example13",
				// find by type the 'to' (consumer)
				a -> a.weaveByType(ToDefinition.class)
						// select first matching type using on of select* methods
						.selectFirst()
						// and replace it with the another consumer
						.replace().to("mock:x"));

		// Getting mock endpoint object from the context
		MockEndpoint mockV = camelContext.getEndpoint("mock:v", MockEndpoint.class);
		MockEndpoint mockX = camelContext.getEndpoint("mock:x", MockEndpoint.class);

		// we expect zero messages for 'mock:v' as it is weaved and replaced by 'mock:x'
		mockV.expectedMessageCount(0);
		// 'mock:x' will receive all messages destined to 'mock:v'
		mockX.expectedBodiesReceived("A", "B", "C");

		// Sending test data to route consumer - this time without
		producerTemplate.sendBody("direct:advice-weave-by-type-and-select", "a,b,c");

		// Asserting expectations are satisfied
		mockV.assertIsSatisfied();
		mockX.assertIsSatisfied();
	}
	// @formatter:on
}
