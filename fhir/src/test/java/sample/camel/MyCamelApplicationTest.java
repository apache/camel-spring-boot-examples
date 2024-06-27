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

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.apache.camel.test.infra.fhir.services.FhirService;
import org.apache.camel.test.infra.fhir.services.FhirServiceFactory;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.commons.io.FileUtils;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;

@CamelSpringBootTest
@SpringBootTest(classes = {MyCamelApplication.class, MyCamelApplicationTest.class},
		properties = "input = target/work/fhir/testinput")
public class MyCamelApplicationTest {

	@RegisterExtension
	public static FhirService service = FhirServiceFactory.createSingletonService();

	@Autowired
	private CamelContext camelContext;

	@Autowired
	private ProducerTemplate producerTemplate;

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

	@Bean
	CamelContextConfiguration contextConfiguration() {
		return new CamelContextConfiguration() {
			@Override
			public void beforeApplicationStart(CamelContext context) {
				context.getPropertiesComponent().addInitialProperty("serverUrl", service.getServiceBaseURL());
			}

			@Override
			public void afterApplicationStart(CamelContext camelContext) {

			}
		};
	}
}
