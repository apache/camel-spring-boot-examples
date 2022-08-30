package org.apache.camel.example.springboot.infinispan;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;

import java.util.function.Consumer;

@CamelSpringBootTest
@SpringBootTest(classes = Application.class)
final class ApplicationTest {

	private static final Logger LOG = LoggerFactory.getLogger(ApplicationTest.class);

	private static final String CONTAINER_IMAGE = "quay.io/infinispan/server:13.0.5.Final-1";

	private static GenericContainer<?> container;

	@Autowired
	private ProducerTemplate producerTemplate;

	@Autowired
	private CamelContext camelContext;

	private static String host = "localhost";

	private static Integer port = 11222;

	private static String name = "infinispan";

	private static String username = "admin";

	private static String password = "password";

	private ApplicationTest() {
	}

	@BeforeAll
	public static void initContainer() {
		LOG.info("start infinispan docker container");
		final Consumer<CreateContainerCmd> cmd = e -> {
			e.getHostConfig().withPortBindings(new PortBinding(Ports.Binding.bindPort(port),
					new ExposedPort(port)));

		};
		final Logger containerLog = LoggerFactory.getLogger("container." + name);
		final Consumer<OutputFrame> logConsumer = new Slf4jLogConsumer(containerLog);

		container = new GenericContainer<>(CONTAINER_IMAGE).withNetworkAliases(name)
				.withEnv("USER", username).withEnv("PASS", password)
				.withLogConsumer(logConsumer)
				.withClasspathResourceMapping("infinispan.xml", "/user-config/infinispan.xml",
						BindMode.READ_ONLY)
				.withCommand("-c", "/user-config/infinispan.xml").withExposedPorts(port)
				.withCreateContainerCmdModifier(cmd).waitingFor(Wait.forListeningPort())
				.waitingFor(Wait.forLogMessage(".*Infinispan.*Server.*started.*", 1));
		container.start();
	}

	@Test
	public void shouldPopulateCache() throws Exception {
		final MockEndpoint mock = camelContext.getEndpoint("mock:result", MockEndpoint.class);
		mock.expectedMessageCount(1);
		producerTemplate.sendBody("direct:test", null);
		mock.assertIsSatisfied();
		Assertions.assertEquals("test", mock.getExchanges().get(0).getIn().getBody(String.class));
	}
}
