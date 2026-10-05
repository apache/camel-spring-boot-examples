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
        BlobSnapshotTest.class,
        BlobSnapshotTest.AzuriteConfiguration.class,
        BlobRouteBuilder.class,
        BlobSnapshotRouteBuilder.class
})
public class BlobSnapshotTest {

    static final String ACCOUNT_NAME = "devstoreaccount1";
    static final String ACCOUNT_KEY =
            "Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==";
    static final String CONTAINER_NAME = "snapshottest";
    static final String BLOB_NAME = "snapshot-blob.txt";
    static final String ORIGINAL_CONTENT = "original snapshot content";

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

        byte[] content = ORIGINAL_CONTENT.getBytes(StandardCharsets.UTF_8);
        template.send("direct:uploadBlob", exchange -> {
            exchange.getIn().setHeader(BlobConstants.BLOB_NAME, BLOB_NAME);
            exchange.getIn().setBody(new ByteArrayInputStream(content));
        });
    }

    @Test
    void testCreateSnapshotReturnsId() {
        uploadTestBlob();

        Exchange result = template.send("direct:createSnapshot", exchange -> {
            exchange.getIn().setHeader(BlobConstants.BLOB_NAME, BLOB_NAME);
        });
        assertNull(result.getException(), "createSnapshot should succeed: " + result.getException());

        String snapshotId = result.getMessage().getHeader(BlobConstants.BLOB_SNAPSHOT_ID, String.class);
        assertNotNull(snapshotId, "Snapshot ID should be returned in header");
    }

    @Test
    void testReadBlobViaSnapshotId() {
        uploadTestBlob();

        Exchange snapshotResult = template.send("direct:createSnapshot", exchange -> {
            exchange.getIn().setHeader(BlobConstants.BLOB_NAME, BLOB_NAME);
        });
        String snapshotId = snapshotResult.getMessage().getHeader(BlobConstants.BLOB_SNAPSHOT_ID, String.class);
        assertNotNull(snapshotId);

        String updatedContent = "modified after snapshot";
        template.send("direct:uploadBlob", exchange -> {
            exchange.getIn().setHeader(BlobConstants.BLOB_NAME, BLOB_NAME);
            exchange.getIn().setBody(new ByteArrayInputStream(updatedContent.getBytes(StandardCharsets.UTF_8)));
        });

        Exchange snapshotRead = template.send("direct:getBlob", exchange -> {
            exchange.getIn().setHeader(BlobConstants.BLOB_NAME, BLOB_NAME);
            exchange.getIn().setHeader(BlobConstants.BLOB_SNAPSHOT_ID, snapshotId);
        });
        assertNull(snapshotRead.getException(), "Snapshot read should succeed: " + snapshotRead.getException());
        String retrieved = snapshotRead.getMessage().getBody(String.class);
        assertEquals(ORIGINAL_CONTENT, retrieved, "Snapshot should return original content");

        Exchange liveRead = template.send("direct:getBlob", exchange -> {
            exchange.getIn().setHeader(BlobConstants.BLOB_NAME, BLOB_NAME);
        });
        assertNull(liveRead.getException(), "Live read should succeed: " + liveRead.getException());
        String liveRetrieved = liveRead.getMessage().getBody(String.class);
        assertEquals(updatedContent, liveRetrieved, "Live blob should return updated content");
    }
}
