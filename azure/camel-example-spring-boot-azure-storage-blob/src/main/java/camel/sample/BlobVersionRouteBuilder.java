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
package camel.sample;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import com.azure.storage.blob.BlobServiceClient;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.azure.storage.blob.BlobConstants;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BlobVersionRouteBuilder extends RouteBuilder {

    @Autowired
    private BlobServiceClient serviceClient;

    @Value("${azure.storage.container-name}")
    private String containerName;

    @Override
    public void configure() {
        from("timer:versionDemo?repeatCount=1&delay=0")
            .routeId("version-demo")
            .process(ex -> serviceClient.createBlobContainerIfNotExists(containerName))
            .log("=== Blob Version ID Demo ===")
            .setHeader(BlobConstants.BLOB_NAME, constant("version-demo.txt"))
            .process(ex -> ex.getIn().setBody(
                new ByteArrayInputStream("Hello Version 1".getBytes(StandardCharsets.UTF_8))))
            .to("azure-storage-blob://{{azure.storage.account-name}}/{{azure.storage.container-name}}"
                + "?operation=uploadBlockBlob&serviceClient=#serviceClient")
            .log("Uploaded version 1")
            .removeHeaders("CamelAzureStorageBlob*")
            .setHeader(BlobConstants.BLOB_NAME, constant("version-demo.txt"))
            .process(ex -> ex.getIn().setBody(
                new ByteArrayInputStream("Hello Version 2".getBytes(StandardCharsets.UTF_8))))
            .to("azure-storage-blob://{{azure.storage.account-name}}/{{azure.storage.container-name}}"
                + "?operation=uploadBlockBlob&serviceClient=#serviceClient")
            .log("Uploaded version 2")
            .to("azure-storage-blob://{{azure.storage.account-name}}/{{azure.storage.container-name}}"
                + "?operation=listBlobVersions&serviceClient=#serviceClient")
            .split(body())
                .log("  ${body.name} -- versionId=${body.versionId}")
            .end()
            .log("=== Demo complete ===");
    }
}
