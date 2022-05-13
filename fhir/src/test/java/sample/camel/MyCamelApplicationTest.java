/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sample.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.commons.io.FileUtils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.hl7.fhir.dstu3.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@CamelSpringBootTest
@SpringBootTest(classes = MyCamelApplication.class,
		properties = "input = target/work/fhir/testinput")
public class MyCamelApplicationTest {

	private static final int CONTAINER_PORT = 8080;
	private static final int EXPOSED_PORT = 8081;
	private static final String CONTAINER_IMAGE = "hapiproject/hapi:v4.2.0";

	private static GenericContainer container;

	@Autowired
	private CamelContext camelContext;

	@Autowired
	private ProducerTemplate producerTemplate;

	@BeforeAll
	public static void startServer() throws Exception {
		Consumer<CreateContainerCmd> cmd = e -> {
			e.withPortBindings(new PortBinding(Ports.Binding.bindPort(EXPOSED_PORT),
					new ExposedPort(CONTAINER_PORT)));
		};

		container = new GenericContainer(CONTAINER_IMAGE)
				.withNetworkAliases("fhir")
				.withExposedPorts(CONTAINER_PORT)
				.withCreateContainerCmdModifier(cmd)
				.withEnv("HAPI_FHIR_VERSION", "DSTU3")
				.withEnv("HAPI_REUSE_CACHED_SEARCH_RESULTS_MILLIS", "-1")
				.waitingFor(Wait.forListeningPort())
				.waitingFor(Wait.forHttp("/hapi-fhir-jpaserver/fhir/metadata"));
		;
		container.start();
	}

	@Test
	public void shouldPushConvertedHl7toFhir() throws Exception {
		MockEndpoint mock = camelContext.getEndpoint("mock:result", MockEndpoint.class);
		mock.expectedMessageCount(1);

		FileUtils.copyDirectory(new File("src/main/data"), new File("target/work/fhir/testinput"));
		Thread.sleep(TimeUnit.SECONDS.toMillis(5));
		producerTemplate.sendBody("direct:start", null);

		mock.assertIsSatisfied();
		Assertions.assertEquals("Freeman", mock.getExchanges().get(0).getIn().getBody(Patient.class).getName().get(0).getFamily());
	}
}
