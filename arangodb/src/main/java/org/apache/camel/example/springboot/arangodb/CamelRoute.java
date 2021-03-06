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
package org.apache.camel.example.springboot.arangodb;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.arangodb.ArangoDbConstants;
import org.springframework.stereotype.Component;

@Component
public class CamelRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

      from("timer://foo?period=10000&repeatCount=1")
                .bean(MyBeanService.class, "createDocument")
                .to("arangodb:myDb?operation=SAVE_DOCUMENT")
                .bean(MyBeanService.class, "readDocument")
                .setHeader(ArangoDbConstants.RESULT_CLASS_TYPE).constant(String.class)
                .to("arangodb:myDb?operation=FIND_DOCUMENT_BY_KEY")
                .log("Received body: ${body}");
    }
}
