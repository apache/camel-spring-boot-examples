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
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@CamelSpringBootTest
@SpringBootTest(classes = Application.class)
public class CustomConverterGeneratedTest {

	@Autowired
	private CamelContext context;

	private ProducerTemplate producerTemplate;

	@BeforeEach
	public void setup() {
		producerTemplate = context.createProducerTemplate();
	}

	@Test
	public void testCustomConverter() {
// 		Camel 2.x option : adding TypeConverter at runtime
// 		Requires  TypeConverter file in /META-INF/services/org/apache/camel/
		context.getTypeConverterRegistry().addTypeConverters(new CustomGeneratedConverter());

		byte[] data = "John Doe 22".getBytes();
		final Person abc = producerTemplate.requestBody("direct:convert1", data, Person.class);

		assertNotNull(abc);

		assertEquals("John", abc.getFirstName());
		assertEquals("Doe", abc.getLastName());
		assertEquals(22, abc.getAge());
	}

}
