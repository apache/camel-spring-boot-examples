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
package org.apache.camel.example.kafka;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class KafkaRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        String insert = "insert into foo(name) values (:#word)";

        from("platform-http:/send/{word}")
            .log("http word: ${header.word}")
            .to("sql:" + insert)
            .setBody(simple("{\"foo\": \"${header.word}\"}"))
            .to("kafka:foo");

        from("platform-http:/send2/{word}")
            .log("http word: ${header.word}")
            .setBody(simple("{\"foo\": \"${header.word}\"}"))
            .to("kafka:foo")
            .to("sql:" + insert);

        from("platform-http:/sendtx/{word}")
            .onException(Exception.class)
                .handled(true)
                .rollback("Expected error when trying to insert duplicate values in the unique column.")
            .end()
            .log("http word: ${header.word}")
            .to("sql:" + insert)
            .setBody(simple("{\"foo\": \"${header.word}\"}"))
            .to("kafka:foo?transacted=true");

        from("platform-http:/sendtx2/{word}")
            .onException(Exception.class)
                .handled(true)
                .rollback("Expected error when trying to insert duplicate values in the unique column.")
            .end()
            .log("http word: ${header.word}")
            .setBody(simple("{\"foo\": \"${header.word}\"}"))
            .to("kafka:foo?transacted=true")
            .to("sql:" + insert);
    }
}
