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
import java.net.HttpURLConnection;
import java.net.URI;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

@DirtiesContext
@CamelSpringBootTest
@SpringBootTest(classes = {
        CamelAutoConfiguration.class,
        BlobSmokeTest.class,
        BlobSmokeTest.TestConfiguration.class,
        BlobRouteBuilder.class
})
public class BlobSmokeTest {

    static final String ACCOUNT_NAME = "devstoreaccount1";
    static final String ACCOUNT_KEY =
            "Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==";
    static final String CONTAINER_NAME = "smoketest";

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

    @Autowired
    CamelContext context;

    @Autowired
    ProducerTemplate template;

    @Test
    void testAzuriteReachable() throws Exception {
        String url = String.format("http://%s:%d/devstoreaccount1?comp=list",
                azurite.getHost(), azurite.getMappedPort(10000));
        HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
        conn.setRequestMethod("GET");
        int status = conn.getResponseCode();
        assertTrue(status == 200 || status == 403, "Azurite should be reachable, got " + status);
    }

    @Test
    void testServiceClientInRegistry() {
        BlobServiceClient client = context.getRegistry().lookupByNameAndType("serviceClient", BlobServiceClient.class);
        assertNotNull(client, "serviceClient should be in the Camel registry");
    }

    @Test
    void testUploadAndDownloadBlob() {
        BlobServiceClient client = context.getRegistry().lookupByNameAndType("serviceClient", BlobServiceClient.class);
        client.createBlobContainerIfNotExists(CONTAINER_NAME);

        String expected = "hello azurite";
        template.send("direct:uploadBlob", exchange -> {
            exchange.getIn().setHeader(BlobConstants.BLOB_NAME, "smoke.txt");
            exchange.getIn().setBody(new ByteArrayInputStream(expected.getBytes(StandardCharsets.UTF_8)));
        });

        Exchange result = template.send("direct:getBlob", exchange -> {
            exchange.getIn().setHeader(BlobConstants.BLOB_NAME, "smoke.txt");
        });
        assertNull(result.getException(), "Download should succeed: " + result.getException());
        assertEquals(expected, result.getMessage().getBody(String.class));
    }

    @Configuration
    public static class TestConfiguration {
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
}
