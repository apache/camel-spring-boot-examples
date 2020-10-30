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

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelExchangeException;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.example.spring.boot.Application;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.apache.camel.test.junit5.TestSupport.assertIsInstanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@CamelSpringBootTest
@SpringBootTest(classes = {Application.class})
public class CamelSplitterEIPTests {

	@Autowired
	private CamelContext camelContext;

	@Autowired
	private ProducerTemplate producerTemplate;

	@Test
	public void testSplit() throws InterruptedException {
		MockEndpoint mockA = camelContext.getEndpoint("mock:a", MockEndpoint.class);

		mockA.expectedBodiesReceived("A", "B", "C");

		producerTemplate.sendBody("direct:splitter", new ArrayList<>(Arrays.asList("A", "B", "C")));

		mockA.assertIsSatisfied();
	}

	@Test
	public void testSplitAggregate() throws InterruptedException {
		MockEndpoint mockB = camelContext.getEndpoint("mock:b", MockEndpoint.class);
		MockEndpoint mockC = camelContext.getEndpoint("mock:c", MockEndpoint.class);

		mockB.expectedBodiesReceived("Alpha", "Beta", "Charlie");
		mockC.expectedBodiesReceived("Alpha+Beta+Charlie");

		producerTemplate.sendBody("direct:split-aggregate", "A,B,C");

		mockB.assertIsSatisfied();
		mockC.assertIsSatisfied();
	}

	@Test
	public void testSplitAggregatePojo() throws InterruptedException {
		MockEndpoint mockD = camelContext.getEndpoint("mock:d", MockEndpoint.class);
		MockEndpoint mockE = camelContext.getEndpoint("mock:e", MockEndpoint.class);

		mockD.expectedBodiesReceived("Alpha", "Beta", "Charlie");
		mockE.expectedBodiesReceived("Alpha+Beta+Charlie");

		producerTemplate.sendBody("direct:split-aggregate-bean", "A,B,C");

		mockD.assertIsSatisfied();
		mockE.assertIsSatisfied();
	}

	@Test
	public void testSplitAggregateStopOnException() throws InterruptedException {
		MockEndpoint mockF = camelContext.getEndpoint("mock:f", MockEndpoint.class);
		MockEndpoint mockG = camelContext.getEndpoint("mock:g", MockEndpoint.class);

		mockF.expectedBodiesReceived("Alpha");
		mockG.expectedMessageCount(0);

		final CamelExecutionException ex = assertThrows(CamelExecutionException.class,
				() -> producerTemplate.sendBody("direct:split-aggregate-stop-on-exception", "A,E,C"));

		CamelExchangeException cee = assertIsInstanceOf(CamelExchangeException.class, ex.getCause());
		IllegalArgumentException iae = assertIsInstanceOf(IllegalArgumentException.class, cee.getCause());
		assertEquals("Unknown key E", ex.getCause().getCause().getMessage());

		mockF.assertIsSatisfied();
		mockG.assertIsSatisfied();
	}

	@Test
	public void testSplitAggregateStopOnAggregationException() throws InterruptedException {
		MockEndpoint mockH = camelContext.getEndpoint("mock:h", MockEndpoint.class);
		MockEndpoint mockJ = camelContext.getEndpoint("mock:j", MockEndpoint.class);

		// without error aggregation is done as usual
		mockH.expectedBodiesReceived("A", "B", "C");
		mockJ.expectedBodiesReceived("A+B+C");

		producerTemplate.sendBody("direct:split-aggregate-stop-on-aggregation-exception", "A,B,C");

		mockH.assertIsSatisfied();
		mockJ.assertIsSatisfied();

		mockH.reset();
		mockJ.reset();

		// 'E' is poison message which will lead to an error during aggregation
		producerTemplate.sendBody("direct:split-aggregate-stop-on-aggregation-exception", "A,B,E,C,D");

		// TODO : THOSE TESTS ARE NOT CORRECT, ANY VALUE WOULD PASS
		// Received all messages that went into aggregation, including corrupted one
		mockH.expectedBodiesReceived("A", "B", "E");
		// Since aggregation stopped, receiving all original messages
		mockJ.expectedBodiesReceived("A,B,E,C,D");

		mockH.assertIsSatisfied();
		mockJ.assertIsSatisfied();
	}
}
