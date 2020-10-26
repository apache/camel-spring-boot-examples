package com.fg7;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sample.camel.Application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@CamelSpringBootTest
@SpringBootTest(classes = Application.class)
public class LoadBalancerEIPTest {

	@Autowired
	private CamelContext camelContext;

	private ProducerTemplate producerTemplate;

	@BeforeEach
	public void setUp() {
		producerTemplate = camelContext.createProducerTemplate();
	}

	@Test
	public void testLoadBalancer() throws InterruptedException {
		MockEndpoint mockA = camelContext.getEndpoint("mock:a", MockEndpoint.class);
		MockEndpoint mockB = camelContext.getEndpoint("mock:b", MockEndpoint.class);

		mockA.expectedBodiesReceived("A", "C");
		mockB.expectedBodiesReceived("B", "D");

		producerTemplate.sendBody("direct:loadbalancer-round-robin", "A");
		producerTemplate.sendBody("direct:loadbalancer-round-robin", "B");
		producerTemplate.sendBody("direct:loadbalancer-round-robin", "C");
		producerTemplate.sendBody("direct:loadbalancer-round-robin", "D");

		MockEndpoint.assertIsSatisfied(camelContext);
	}


	@Test
	public void testRandomLoadBalancer() {
		MockEndpoint mockC = camelContext.getEndpoint("mock:c", MockEndpoint.class);
		MockEndpoint mockD = camelContext.getEndpoint("mock:d", MockEndpoint.class);

		for (int i = 0 ; i < 10 ; i++){
			producerTemplate.sendBody("direct:loadbalancer-random", i);
		}

		assertEquals(10, mockC.getReceivedCounter() + mockD.getReceivedCounter());
	}

	@Test
	public void testStickyLoadBalancer() throws InterruptedException {
		MockEndpoint mockE = camelContext.getEndpoint("mock:e", MockEndpoint.class);
		MockEndpoint mockF = camelContext.getEndpoint("mock:f", MockEndpoint.class);

		mockE.expectedBodiesReceived("A", "E");
		mockF.expectedBodiesReceived("B", "C", "D");

		producerTemplate.sendBodyAndHeader("direct:loadbalancer-sticky", "A", "correlation-key",  "vowel");
		producerTemplate.sendBodyAndHeader("direct:loadbalancer-sticky", "B", "correlation-key",  "consonant");
		producerTemplate.sendBodyAndHeader("direct:loadbalancer-sticky", "C", "correlation-key",  "consonant");
		producerTemplate.sendBodyAndHeader("direct:loadbalancer-sticky", "D", "correlation-key",  "consonant");
		producerTemplate.sendBodyAndHeader("direct:loadbalancer-sticky", "E", "correlation-key",  "vowel");

		MockEndpoint.assertIsSatisfied(camelContext);
	}

	@Test
	public void testTopicLoadBalancer() throws Exception {
		MockEndpoint mockJ = camelContext.getEndpoint("mock:j", MockEndpoint.class);
		MockEndpoint mockK = camelContext.getEndpoint("mock:k", MockEndpoint.class);

		mockJ.expectedBodiesReceived("A", "B", "C", "D", "E");
		mockK.expectedBodiesReceived("A", "B", "C", "D", "E");

		producerTemplate.sendBody("direct:loadbalancer-topic", "A");
		producerTemplate.sendBody("direct:loadbalancer-topic", "B");
		producerTemplate.sendBody("direct:loadbalancer-topic", "C");
		producerTemplate.sendBody("direct:loadbalancer-topic", "D");
		producerTemplate.sendBody("direct:loadbalancer-topic", "E");

		MockEndpoint.assertIsSatisfied(camelContext);
	}

	@Test
	public void testFailoverLoadBalancer() throws Exception {
		MockEndpoint mockL = camelContext.getEndpoint("mock:l", MockEndpoint.class);
		MockEndpoint mockM = camelContext.getEndpoint("mock:m", MockEndpoint.class);

		mockL.expectedBodiesReceived("A", "B", "C", "D");
		mockM.expectedBodiesReceived("E");

		producerTemplate.sendBody("direct:loadbalancer-failover", "A");
		producerTemplate.sendBody("direct:loadbalancer-failover", "B");
		producerTemplate.sendBody("direct:loadbalancer-failover", "C");
		producerTemplate.sendBody("direct:loadbalancer-failover", "E");
		producerTemplate.sendBody("direct:loadbalancer-failover", "D");

		MockEndpoint.assertIsSatisfied(camelContext);
	}

	@Test
	public void testFailoverRoundRobinLoadBalancer() throws Exception {
		MockEndpoint mockN = camelContext.getEndpoint("mock:n", MockEndpoint.class);
		MockEndpoint mockO = camelContext.getEndpoint("mock:o", MockEndpoint.class);
		MockEndpoint mockP = camelContext.getEndpoint("mock:p", MockEndpoint.class);
		MockEndpoint mockQ = camelContext.getEndpoint("mock:q", MockEndpoint.class);

		mockN.expectedMessageCount(0);
		mockO.expectedMessageCount(0);
		mockP.expectedBodiesReceived("A", "C");
		mockQ.expectedBodiesReceived("B", "D", "E");

		producerTemplate.sendBody("direct:loadbalancer-failover-round-robin-no-error-handler", "A");
		producerTemplate.sendBody("direct:loadbalancer-failover-round-robin-no-error-handler", "B");
		producerTemplate.sendBody("direct:loadbalancer-failover-round-robin-no-error-handler", "C");
		producerTemplate.sendBody("direct:loadbalancer-failover-round-robin-no-error-handler", "D");
		producerTemplate.sendBody("direct:loadbalancer-failover-round-robin-no-error-handler", "E");

		MockEndpoint.assertIsSatisfied(camelContext);
	}

	@Test
	public void testWeightedRoundRobinLoadBalancer() throws Exception {
		MockEndpoint mockW = camelContext.getEndpoint("mock:w", MockEndpoint.class);
		MockEndpoint mockX  = camelContext.getEndpoint("mock:x", MockEndpoint.class);

		mockW.expectedBodiesReceived("A", "C", "D", "F", "G", "J", "K");
		mockX.expectedBodiesReceived("B", "E", "H");

		producerTemplate.sendBody("direct:loadbalancer-weighted-round-robin", "A"); // W
		producerTemplate.sendBody("direct:loadbalancer-weighted-round-robin", "B"); // X
		producerTemplate.sendBody("direct:loadbalancer-weighted-round-robin", "C"); // W
		producerTemplate.sendBody("direct:loadbalancer-weighted-round-robin", "D"); // W
		producerTemplate.sendBody("direct:loadbalancer-weighted-round-robin", "E"); // X
		producerTemplate.sendBody("direct:loadbalancer-weighted-round-robin", "F"); // W
		producerTemplate.sendBody("direct:loadbalancer-weighted-round-robin", "G"); // W
		producerTemplate.sendBody("direct:loadbalancer-weighted-round-robin", "H"); // X
		producerTemplate.sendBody("direct:loadbalancer-weighted-round-robin", "J"); // W
		producerTemplate.sendBody("direct:loadbalancer-weighted-round-robin", "K"); // W

		MockEndpoint.assertIsSatisfied(camelContext);
	}

	@Test
	public void testCustomLoadBalancer() throws InterruptedException {
		MockEndpoint mockG = camelContext.getEndpoint("mock:g", MockEndpoint.class);
		MockEndpoint mockH = camelContext.getEndpoint("mock:h", MockEndpoint.class);

		mockG.expectedBodiesReceived("A", "E");
		mockH.expectedBodiesReceived("B", "C", "D");

		producerTemplate.sendBody("direct:loadbalancer-custom", "A");
		producerTemplate.sendBody("direct:loadbalancer-custom", "B");
		producerTemplate.sendBody("direct:loadbalancer-custom", "C");
		producerTemplate.sendBody("direct:loadbalancer-custom", "D");
		producerTemplate.sendBody("direct:loadbalancer-custom", "E");

		MockEndpoint.assertIsSatisfied(camelContext);
	}

}
