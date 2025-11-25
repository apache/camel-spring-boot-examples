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

/**
 * Route that demonstrates Spring AI vector store capabilities.
 * Stores documents from files and performs similarity search.
 */
@Component
public class VectorStoreRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        // Route to add documents from files to the vector store
        from("file:input?noop=true&include=.*\\.txt")
            .routeId("addDocuments")
            .log("Adding document ${header.CamelFileName} to vector store")
            .convertBodyTo(String.class)
            .to("spring-ai-vector-store:myStore?operation=ADD")
            .log("Successfully added ${header.CamelFileName} to vector store");

        // Route to query the vector store using similarity search
        from("timer:queryDocs?repeatCount=1&delay=10000")
            .routeId("queryDocuments")
            .setBody(constant("What is Apache Camel and what does it do?"))
            .log("Querying vector store with: ${body}")
            .to("spring-ai-vector-store:myStore?operation=SIMILARITY_SEARCH&topK=3&similarityThreshold=0.5")
            .log("Retrieved ${body.size()} documents from vector store")
            .split(body())
                .log("Document text: ${body.text}")
            .end();
    }
}
