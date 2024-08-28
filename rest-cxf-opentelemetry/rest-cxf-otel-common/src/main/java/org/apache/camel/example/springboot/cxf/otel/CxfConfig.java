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

import org.apache.cxf.tracing.opentelemetry.jaxrs.OpenTelemetryFeature;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;

@Configuration
public class CxfConfig {

	@Bean
	public JacksonJsonProvider jaxrsProvider() {
		return new JacksonJsonProvider().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
	}

	@Bean
	public OpenTelemetryFeature openTelemetryProvider() {
		return new OpenTelemetryFeature();
	}
}
