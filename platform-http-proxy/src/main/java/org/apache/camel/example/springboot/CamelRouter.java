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
 * Reverse proxy route that logs headers around the backend call.
 *
 * Requests are accepted under the {@code /reverse-proxy} path and forwarded to the fixed backend
 * configured via {@code reverse-proxy.target-base-uri}. The {@code /reverse-proxy} prefix is stripped
 * from the request path so that, for example, {@code /reverse-proxy/get} is forwarded to
 * {@code <target-base-uri>/get}. The remaining path and query string are then appended
 * automatically by the http producer's {@code bridgeEndpoint} mode.
 *
 * NOTE: the consumer path must not be the literal string "proxy" (e.g. "platform-http:proxy").
 * That exact path is a reserved marker in camel-platform-http: it turns the endpoint into a
 * catch-all consumer meant to build a Host-header-based forward proxy, but that mode is only
 * implemented by the Vert.x platform-http engine, not by camel-platform-http-starter (the
 * servlet/Spring-MVC engine used here). On this engine it silently degrades into an unguarded
 * catch-all with no forwarding logic, so a genuinely different path is used instead.
 */
@Component
public class CamelRouter extends RouteBuilder {

	private static final String PROXY_PATH = "/reverse-proxy";

	@Override
	public void configure() throws Exception {

		// @formatter:off
        from("platform-http:reverse-proxy?matchOnUriPrefix=true")
                .routeId("reverse-proxy")
                .wireTap("direct:request")
                .setHeader(Exchange.HTTP_PATH, simple("${header." + Exchange.HTTP_PATH + ".substring(" + PROXY_PATH.length() + ")}"))
                .log("calling ${properties:reverse-proxy.target-base-uri}${headers." + Exchange.HTTP_PATH + "}")
                .to("{{reverse-proxy.target-base-uri}}?bridgeEndpoint=true&throwExceptionOnFailure=false")
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
