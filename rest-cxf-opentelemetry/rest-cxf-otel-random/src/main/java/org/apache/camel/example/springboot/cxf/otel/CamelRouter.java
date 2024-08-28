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

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.bean.validator.BeanValidationException;
import org.apache.camel.component.cxf.common.message.CxfConstants;
import org.apache.camel.component.minio.MinioConstants;
import org.apache.camel.model.dataformat.JsonLibrary;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

import io.minio.Result;
import io.minio.messages.Contents;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Component
public class CamelRouter extends RouteBuilder {


    @Override
    public void configure() throws Exception {
        //very raw way, just to handle the validation responses
        onException(BeanValidationException.class)
                .handled(true)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(Response.Status.BAD_REQUEST.getStatusCode()))
                .setBody(simple("${exchangeProperty.CamelExceptionCaught.getMessage()}"));

        // @formatter:off
        from("cxfrs:/api?" +
                    "resourceClasses=org.apache.camel.example.springboot.cxf.otel.RandomService" +
                    "&bindingStyle=SimpleConsumer" +
                    "&providers=jaxrsProvider,openTelemetryProvider" +
                    "&loggingFeatureEnabled=true")
                .to("log:camel-cxf-log?showAll=true")
                .setHeader(Exchange.BEAN_METHOD_NAME, simple("${header.operationName}"))
                .bean(RandomServiceImpl.class);


        from("direct:play").routeId("play")
                .loop(header("attempts"))
                    .process(exchange -> exchange.getIn().getHeaders().clear())
                    .setHeader(CxfConstants.HTTP_METHOD, constant("GET"))
                    .toD("cxfrs:{{service.random.url}}/services/api/generate")
                    .process(exchange -> exchange.getIn().getHeaders().clear())
                    .setHeader(CxfConstants.HTTP_METHOD, constant("POST"))
                    .setHeader(CxfConstants.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON))
                    .toD("cxfrs:{{service.even.url}}/services/api/check")
                    .process(exchange -> exchange.getIn().getHeaders().clear())
                    .setHeader(CxfConstants.HTTP_METHOD, constant("POST"))
                    .setHeader(CxfConstants.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON))
                    .toD("cxfrs:{{service.odd.url}}/services/api/check")
                .end()
                .setBody(constant(null));

        from("direct:random").routeId("generate-random")
                .loadBalance().random()
                    .to("direct:generate-even")
                    .to("direct:generate-odd")
                .end()
                .convertBodyTo(RandomNumber.class);

        from("direct:generate-even")
                .routeId("generate-even")
                .process(exchange -> {
                    int num = random();
                    while (num % 2 != 0) {
                        num = random();
                    }
                    exchange.getIn().setBody(num);
                });

        from("direct:generate-odd").routeId("generate-odd")
                .process(exchange -> {
                    int num = random();
                    while (num % 2 == 0) {
                        num = random();
                    }
                    exchange.getIn().setBody(num);
                });

        from("direct:save-obj").routeId("save-obj")
                .setHeader(MinioConstants.OBJECT_NAME, header("objectName"))
                .marshal().json(JsonLibrary.Jackson)
                .toD("minio://{{bucket.name}}");

        from("direct:load-results").routeId("load-results")
                .setVariable("results", () -> new Results())
                .toD("minio://{{bucket.name}}?operation=listObjects")
                .split(body())
                    .process(exchange -> {
						try {
                            exchange.getIn().setHeader(MinioConstants.OBJECT_NAME
                                    , ((Contents) exchange.getIn().getBody(Result.class).get()).objectName());
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					})
                    .toD("minio://{{bucket.name}}?operation=getObject")
                    .unmarshal().json(JsonLibrary.Jackson, RandomNumber.class)
                    .process(exchange -> exchange.getVariable("results", Results.class)
							.addNumber(exchange.getIn().getBody(RandomNumber.class)))
                .end()
                .setBody(variable("results"));
        // @formatter:on

    }

    private static int random() {
        return new SecureRandom().nextInt(100, 1000);
    }

}
