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
import java.util.Map;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.azure.storage.blob.BlobConstants;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.apache.camel.test.spring.junit6.CamelSpringBootTest;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@DirtiesContext
@CamelSpringBootTest
@SpringBootTest(classes = {
        CamelAutoConfiguration.class,
        BlobTagsTest.class,
        BlobTagsTest.AzuriteConfiguration.class,
        BlobRouteBuilder.class,
        BlobTagsRouteBuilder.class
})
public class BlobTagsTest {

    static final String ACCOUNT_NAME = "devstoreaccount1";
    static final String ACCOUNT_KEY =
            "Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==";
    static final String CONTAINER_NAME = "tagtest";
    static final String BLOB_NAME = "test-blob.txt";

    static GenericContainer<?> azurite = new GenericContainer<>("mcr.microsoft.com/azure-storage/azurite:3.35.0")
            .withExposedPorts(10000)
            .waitingFor(Wait.forListeningPort());

    static {
        azurite.start();
    }

    @DynamicPropertySource
    static void azuriteProperties(DynamicPropertyRegistry registry) {
        registry.add("azure.storage.account-name", () -> ACCOUNT_NAME);
        registry.add("azure.storage.container-name", () -> CONTAINER_NAME);
        registry.add("camel.component.azure-storage-blob.credential-type", () -> "SHARED_KEY_CREDENTIAL");
    }

    @Configuration
    static class AzuriteConfiguration {
        @Bean
        BlobServiceClient serviceClient() {
            String endpoint = String.format("http://%s:%d/%s",
                    azurite.getHost(), azurite.getMappedPort(10000), ACCOUNT_NAME);
            return new BlobServiceClientBuilder()
                    .endpoint(endpoint)
                    .credential(new StorageSharedKeyCredential(ACCOUNT_NAME, ACCOUNT_KEY))
                    .buildClient();
        }
    }

    @Autowired
    CamelContext camelContext;

    @Autowired
    ProducerTemplate template;

    void uploadTestBlob() {
        BlobServiceClient client = camelContext.getRegistry().lookupByNameAndType("serviceClient", BlobServiceClient.class);
        client.createBlobContainerIfNotExists(CONTAINER_NAME);

        byte[] content = "hello blob tags".getBytes(StandardCharsets.UTF_8);
        template.send("direct:uploadBlob", exchange -> {
            exchange.getIn().setHeader(BlobConstants.BLOB_NAME, BLOB_NAME);
            exchange.getIn().setBody(new ByteArrayInputStream(content));
        });
    }

    @Test
    @SuppressWarnings("unchecked")
    void testSetAndGetBlobTags() {
        uploadTestBlob();

        Map<String, String> tags = Map.of(
                "status", "quarantine",
                "category", "document",
                "priority", "high");

        Exchange setResult = template.send("direct:setBlobTags", exchange -> {
            exchange.getIn().setHeader(BlobConstants.BLOB_NAME, BLOB_NAME);
            exchange.getIn().setHeader(BlobConstants.BLOB_TAGS, tags);
        });
        assertNull(setResult.getException(), "setBlobTags should succeed: " + setResult.getException());

        Exchange result = template.send("direct:getBlobTags", exchange -> {
            exchange.getIn().setHeader(BlobConstants.BLOB_NAME, BLOB_NAME);
        });
        assertNull(result.getException(), "getBlobTags should succeed: " + result.getException());

        Map<String, String> retrieved = result.getMessage().getBody(Map.class);
        assertNotNull(retrieved, "Tags should not be null");
        assertEquals("quarantine", retrieved.get("status"));
        assertEquals("document", retrieved.get("category"));
        assertEquals("high", retrieved.get("priority"));
    }
}
