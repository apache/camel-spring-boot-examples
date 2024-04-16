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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.xml.ws.WebServiceException;

import org.apache.cxf.ext.logging.LoggingFeature;
import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.example.customerservice.Customer;
import com.example.customerservice.CustomerService;
import com.example.customerservice.NoSuchCustomerException;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WsdlClientTest {

	@LocalServerPort
	private int port;

	CustomerService cxfClient;

	protected CustomerService createCustomerClient() {
		String URL = "http://localhost:" + port + "/services/customers";

		ClientProxyFactoryBean factory = new JaxWsProxyFactoryBean();
		factory.setServiceClass(CustomerService.class);
		factory.setAddress(URL);
		factory.getFeatures().add(new LoggingFeature());
		return (CustomerService) factory.create();
	}

	@BeforeEach
	public void before() {
		cxfClient = createCustomerClient();
	}
	
	@Test
        public void testRequestLimiting() throws Exception {
            CountDownLatch latch = new CountDownLatch(50);

            ExecutorService executor = Executors.newFixedThreadPool(200);

            for (int i = 0; i < 50; i++) {
                executor.execute(new SendRequest(latch));
            }
            latch.await();
        }

        class SendRequest implements Runnable {

            CountDownLatch latch;

            SendRequest(CountDownLatch latch) {
                this.latch = latch;
            }

            @Override
            public void run() {
                try {
                    List<Customer> customers = cxfClient.getCustomersByName("test");
                    assertEquals(customers.get(0).getName(), "test");
                    assertEquals(customers.get(0).getCustomerId(), 1);

                } catch (Exception ex) {
                    // some requests are expected to fail and receive 503 error
                    // cause Server side limit the concurrent request
                    assertTrue(ex.getCause().getMessage().contains("503: Service Unavailable"));
                } finally {
                    latch.countDown();
                }
            }
        }

	@Test
	public void testGetCustomer() throws Exception {
		List<Customer> customers =
				cxfClient.getCustomersByName("test");
		assertEquals(customers.get(0).getName(), "test");
		assertEquals(customers.get(0).getCustomerId(), 1);
	}

	@Test
	public void testNonExistentCustomer() throws Exception {
		Assertions.assertThatThrownBy(() -> cxfClient.getCustomersByName("Non existent"))
				.isInstanceOf(NoSuchCustomerException.class);
	}

	@Test
	public void testInvalidRequest() {
		Assertions.assertThatThrownBy(() -> cxfClient.getCustomersByName("a"))
				.isInstanceOf(WebServiceException.class);
	}

	@Test
	public void testUpdateCustomer() throws Exception {
		double revenue = 9999;
		LocalDate birthDate = LocalDate.parse("1990-03-13");

		List<Customer> customers =
				cxfClient.getCustomersByName("test");

		assertNotEquals(customers.get(0).getRevenue(), revenue);
		assertNotEquals(customers.get(0).getBirthDate(), birthDate);

		Customer customer = customers.get(0);
		customer.setRevenue(revenue);
		customer.setBirthDate(birthDate);

		// void method are async by default
		cxfClient.updateCustomer(customer);

		Awaitility.await().atMost(Duration.ofSeconds(5))
				.until(() -> {
					List<Customer> updatedCustomers =
							cxfClient.getCustomersByName("test");

					return updatedCustomers.get(0).getName().equals("test") &&
							updatedCustomers.get(0).getCustomerId() == 1 &&
							updatedCustomers.get(0).getRevenue() == revenue &&
							updatedCustomers.get(0).getBirthDate().equals(birthDate);
				});
	}
}
