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

import org.apache.cxf.frontend.ClientProxyFactoryBean;

import org.junit.jupiter.api.Test;

import org.assertj.core.api.Assertions;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import sample.camel.service.Address;
import sample.camel.service.Contact;
import sample.camel.service.ContactService;
import sample.camel.service.ContactType;
import sample.camel.service.NoSuchContactException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PojoClientTest {

	@LocalServerPort
	private int port;

	protected ContactService createCXFClient() {
		String URL = "http://localhost:" + port + "/services/contact";

		ClientProxyFactoryBean factory = new ClientProxyFactoryBean();
		factory.setServiceClass(ContactService.class);
		factory.setAddress(URL);

		return (ContactService) factory.create();
	}

	protected static Contact createContact() {
		Contact contact = new Contact();
		contact.setName("Croway");
		contact.setType(ContactType.OTHER);
		Address address = new Address();
		address.setCity("Rome");
		address.setStreet("Test Street");
		contact.setAddress(address);

		return contact;
	}

	@Test
	public void testBasic() throws NoSuchContactException {
		ContactService cxfClient = createCXFClient();

		cxfClient.addContact(createContact());

		Assertions.assertThat(cxfClient.getContacts().getContacts()).hasSize(1);

		Assertions.assertThat(cxfClient.getContact("Croway")).isNotNull();

		Assertions.assertThatThrownBy(() -> cxfClient.getContact("Non existent")).isInstanceOf(NoSuchContactException.class);
	}
}
