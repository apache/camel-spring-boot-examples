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
package org.apache.camel.example.springboot;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Reverse proxy route with routes that logs heders around the backend call
 */
@Component
public class CamelRouter extends RouteBuilder {

	@Override
	public void configure() throws Exception {

		// @formatter:off
        from("platform-http:proxy/*?matchOnUriPrefix=true")
                .routeId("reverse-proxy")
                .wireTap("direct:request")
                .removeHeader(Exchange.HTTP_PATH)
                .log("calling ${headers." + Exchange.HTTP_URL + "}")
                .toD("${headers." + Exchange.HTTP_URL + "}"
                        + "?throwExceptionOnFailure=false&bridgeEndpoint=true")
                .wireTap("direct:response");

        from("direct:request")
                .routeId("header-request")
                .log("${headers}");
        from("direct:response")
                .routeId("header-response")
                .log("${headers}");
        // @formatter:on
	}
}
