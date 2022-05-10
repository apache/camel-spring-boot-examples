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

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestParamType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CamelRoutes extends RouteBuilder {
    @Value("${camel.servlet.mapping.context-path}")
    public String contextPath;

    @Override
    public void configure() {
        restConfiguration()
            .contextPath(contextPath);

        rest("/messages")
            .produces("text/plain")
            .get()
                .to("direct:messages")
            .post("/{message}")
                .param().name("message").type(RestParamType.path).dataType("string").endParam()
                .to("direct:trans");

        from("direct:messages")
            .to("jpa:sample.camel.AuditLog?namedQuery=getAuditLog")
            .convertBodyTo(String.class);

        from("direct:trans")
            .transacted()
            .setBody(simple("${headers.message}"))
            .to("bean:auditLog?method=createAuditLog(${body})")
            .to("jpa:sample.camel.AuditLog")
            .setBody(simple("${headers.message}"))
            .to("jms:outbound?disableReplyTo=true")
            .choice()
                .when(body().startsWith("fail"))
                    .log("Forced exception")
                        .process(x -> {throw new RuntimeException("fail");})
                .otherwise()
                    .log("Message added: ${body}")
            .endChoice();

        from("jms:outbound")
            .log("Message out: ${body}")
            .to("bean:auditLog?method=createAuditLog(${body}-ok)")
            .to("jpa:sample.camel.AuditLog");
    }
}
