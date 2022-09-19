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

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.cxf.jaxws.CxfEndpoint;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import sample.camel.service.ContactService;

/**
 * This class demonstrate how to expose a SOAP endpoint starting from java classes
 */
@Component
public class MyPojoRouteBuilder extends RouteBuilder {

	@Bean
	CxfEndpoint contact() {
		CxfEndpoint contactEndpoint = new CxfEndpoint();
		contactEndpoint.setServiceClass(ContactService.class);
		contactEndpoint.setAddress("/contact");

		return contactEndpoint;
	}

	@Override
	public void configure() throws Exception {
		from("cxf:bean:contact")
				.recipientList(simple("bean:inMemoryContactService?method=${header.operationName}"));
	}
}
