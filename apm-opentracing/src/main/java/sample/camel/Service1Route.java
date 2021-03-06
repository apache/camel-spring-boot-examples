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
import org.springframework.stereotype.Component;

@Component
public class Service1Route extends RouteBuilder {

    @Override
    public void configure() throws Exception {
         from("timer:trigger?exchangePattern=InOut&period=30000").streamCaching()
            .bean("counterBean")
            .log("Client request: ${body}")
            .to("http://localhost:9090/service1")
            .log("Client response: ${body}");

        from("jetty:http://0.0.0.0:9090/service1").routeId("service1").streamCaching()
            .removeHeaders("CamelHttp*")
            .log("Service1 request: ${body}")
            .delay(simple("${random(1000,2000)}"))
            .transform(simple("Service1-${body}"))
            .log("Service1 response: ${body}");
    }

}
