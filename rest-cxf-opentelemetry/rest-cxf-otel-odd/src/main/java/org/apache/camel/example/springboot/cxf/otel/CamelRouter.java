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
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.bean.validator.BeanValidationException;
import org.apache.camel.component.cxf.common.message.CxfConstants;
import org.apache.camel.model.dataformat.JsonLibrary;

import org.springframework.stereotype.Component;

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
                    "resourceClasses=org.apache.camel.example.springboot.cxf.otel.OddService" +
                    "&bindingStyle=SimpleConsumer" +
                    "&providers=jaxrsProvider,openTelemetryProvider" +
                    "&loggingFeatureEnabled=true")
                .to("log:camel-cxf-log?showAll=true")
                .setHeader(Exchange.BEAN_METHOD_NAME, simple("${header.operationName}"))
                .bean(OddServiceImpl.class);


        from("direct:register").routeId("odd-register")
                .marshal().json(JsonLibrary.Jackson)
                .setHeader(CxfConstants.HTTP_METHOD, constant("PUT"))
                .setHeader(CxfConstants.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON))
                .setHeader(SERVICE_HEADER_NAME, constant("odd"))
                .toD("cxfrs:{{service.random.url}}/services/api/register");
        // @formatter:on
    }

}
