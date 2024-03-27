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
package org.apache.camel.example.springboot.cxf.otel;

import static org.apache.camel.example.springboot.cxf.otel.Constants.SERVICE_HEADER_NAME;

import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.cxf.tracing.TracerContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class RandomServiceImpl implements RandomService {

	private static final Logger LOGGER = LoggerFactory.getLogger(RandomServiceImpl.class);

	@Autowired
	ProducerTemplate producerTemplate;

	@Override
	public Results play(Integer attempts) {
		producerTemplate.requestBodyAndHeader("direct:play", null, "attempts", attempts, Void.class);
		return producerTemplate.requestBody("direct:load-results", null, Results.class);
	}

	@Override
	public RandomNumber generate(final TracerContext tracer) {
		tracer.timeline("generate number");
		tracer.annotate("custom-op", "generate");
		return producerTemplate.requestBody("direct:random", null, RandomNumber.class);
	}

	@Override
	public void register(RandomNumber number, Exchange exchange) {
		final String serviceName = (String) exchange.getIn().getHeader(SERVICE_HEADER_NAME);
		assert serviceName != null;
		assert number != null;
		assert number.getType() != null;
		LOGGER.info("register {}", number);
		final String objName = String.format("%s-%s", serviceName, number.getNumber());
		producerTemplate.sendBodyAndHeader("direct:save-obj", number, "objectName", objName);
	}
}
