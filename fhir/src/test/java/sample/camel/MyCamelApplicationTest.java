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
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.apache.camel.test.infra.fhir.services.FhirService;
import org.apache.camel.test.infra.fhir.services.FhirServiceFactory;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.UseAdviceWith;
import org.apache.commons.io.FileUtils;
import org.awaitility.Awaitility;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;

@CamelSpringBootTest
@SpringBootTest(classes = {MyCamelApplication.class, MyCamelApplicationTest.class},
		properties = {"input = target/work/fhir/testinput", "camel.springboot.java-routes-include-pattern=**/MyCamelRouter"})
@UseAdviceWith
public class MyCamelApplicationTest {

	@RegisterExtension
	public static FhirService service = FhirServiceFactory.createSingletonService();

	@Autowired
	private CamelContext camelContext;

	@Autowired
	private ProducerTemplate producerTemplate;

	@Test
	public void shouldPushConvertedHl7toFhir() throws Exception {
		// Clean up test input directory if it exists
		File testInputDir = new File("target/work/fhir/testinput");
		if (testInputDir.exists()) {
			FileUtils.deleteDirectory(testInputDir);
		}

		//  Add mock endpoint to route
		AdviceWith.adviceWith(camelContext, "fhir-example", a -> {
			a.weaveAddLast().to("mock:result");
		});

		// Set server URL and start Camel after FHIR server is ready
		Awaitility.await()
			.atMost(90, TimeUnit.SECONDS)
			.pollInterval(2, TimeUnit.SECONDS)
			.until(() -> {
				try {
					java.net.HttpURLConnection conn = (java.net.HttpURLConnection)
						new java.net.URL(service.getServiceBaseURL() + "/metadata").openConnection();
					conn.setConnectTimeout(3000);
					conn.setReadTimeout(3000);
					int code = conn.getResponseCode();
					conn.disconnect();
					return code == 200;
				} catch (Exception e) {
					return false;
				}
			});

		// Configure server URL and start
		camelContext.getPropertiesComponent().addInitialProperty("serverUrl", service.getServiceBaseURL());
		camelContext.start();

		MockEndpoint mock = camelContext.getEndpoint("mock:result", MockEndpoint.class);
		mock.expectedMessageCount(1);

		// Copy test file
		FileUtils.copyDirectory(new File("src/main/data"), testInputDir);

		// Wait for processing
		mock.assertIsSatisfied(30000);

		// Verify - the FHIR create operation returns a MethodOutcome
		ca.uhn.fhir.rest.api.MethodOutcome outcome = mock.getExchanges().get(0).getIn().getBody(ca.uhn.fhir.rest.api.MethodOutcome.class);
		Assertions.assertNotNull(outcome, "MethodOutcome should not be null - route should have completed successfully");
		Assertions.assertNotNull(outcome.getId(), "Patient should have been created with an ID in the FHIR server");
	}

	@Bean
	CamelContextConfiguration contextConfiguration() {
		return new CamelContextConfiguration() {
			@Override
			public void beforeApplicationStart(CamelContext context) {
				// Server URL will be set in test method
			}

			@Override
			public void afterApplicationStart(CamelContext camelContext) {
			}
		};
	}
}
