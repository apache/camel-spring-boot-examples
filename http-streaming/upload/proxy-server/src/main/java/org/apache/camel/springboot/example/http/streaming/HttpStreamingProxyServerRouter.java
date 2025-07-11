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
package org.apache.camel.springboot.example.http.streaming;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class HttpStreamingProxyServerRouter extends RouteBuilder {
	@Override
	public void configure() {
		restConfiguration()
				.component("platform-http");
		rest()
				.put("/test").to("direct:putProxy");

	from("direct:putProxy")
			.removeHeader("CamelHttpPath")
			.setHeader(Exchange.CONTENT_TYPE, constant("multipart/form-data"))
			.process(exchange -> {
				MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
				HttpEntity resultEntity = multipartEntityBuilder
						.addBinaryBody("data", exchange.getMessage().getBody(InputStream.class), ContentType.DEFAULT_BINARY, "input")
						.build();
				exchange.getIn().setBody(resultEntity);
			})
			.to("http://localhost:8080/test?bridgeEndpoint=true")
			.log("done streaming")
			.setBody(simple("done"));
	}
}
