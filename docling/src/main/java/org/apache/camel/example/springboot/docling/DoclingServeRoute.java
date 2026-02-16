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
package org.apache.camel.example.springboot.docling;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class DoclingServeRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        from("file:{{documents.directory}}?delete=true&include=.*\\.(pdf|docx|pptx|html|md)$")
            .routeId("document-to-markdown-converter")
            .log("Processing document: ${header.CamelFileName}")
            .to("docling:CONVERT_TO_MARKDOWN?useDoclingServe=true&doclingServeUrl={{docling.serve.url}}&contentInBody=true")
            .log("Document converted to Markdown: ${header.CamelFileName}")
            .setHeader("CamelFileName", simple("${file:name.noext}.md"))
            .to("file:{{output.directory}}")
            .log("Saved converted document to: {{output.directory}}/${header.CamelFileName}");

        from("file:{{documents.directory}}/extract?delete=true&include=.*\\.(pdf|docx|pptx)$")
            .routeId("document-metadata-extractor")
            .log("Extracting metadata from: ${header.CamelFileName}")
            .to("docling:EXTRACT_STRUCTURED_DATA?useDoclingServe=true&doclingServeUrl={{docling.serve.url}}&outputFormat=json&contentInBody=true")
            .log("Metadata extracted from: ${header.CamelFileName}")
            .setHeader("CamelFileName", simple("${file:name.noext}.json"))
            .to("file:{{output.directory}}/metadata")
            .log("Saved metadata to: {{output.directory}}/metadata/${header.CamelFileName}");
    }
}
