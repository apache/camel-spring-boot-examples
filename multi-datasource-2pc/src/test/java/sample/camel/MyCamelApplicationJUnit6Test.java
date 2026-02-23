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

import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.NotifyBuilder;
import org.apache.camel.test.spring.junit6.CamelSpringBootTest;
import org.apache.camel.test.spring.junit6.EnableRouteCoverage;
import org.apache.camel.util.FileUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@CamelSpringBootTest
@SpringBootTest(classes = MyCamelApplication.class)
@EnableRouteCoverage
@ActiveProfiles("test")
public class MyCamelApplicationJUnit6Test {

    @Container
    static PostgreSQLContainer<?> db1 = new PostgreSQLContainer<>("postgres:16")
        .withInitScript("schema-ds1.sql")
        .withDatabaseName("postgres")
        .withUsername("postgres")
        .withPassword("password")
        .withCommand("postgres -c max_prepared_transactions=10");

    @Container
    static PostgreSQLContainer<?> db2 = new PostgreSQLContainer<>("postgres:16")
        .withInitScript("schema-ds2.sql")
        .withDatabaseName("postgres")
        .withUsername("postgres")
        .withPassword("password")
        .withCommand("postgres -c max_prepared_transactions=10");

    @Autowired
    private CamelContext camelContext;

    @Autowired
    @Qualifier("ds1jdbc")
    private JdbcTemplate jdbcTemplate1;

    @Autowired
    @Qualifier("ds2jdbc")
    private JdbcTemplate jdbcTemplate2;

    @DynamicPropertySource
    static void dbProperties(DynamicPropertyRegistry registry) {
        registry.add("app.datasource.ds1.url",
            () -> "jdbc:postgresql://" + db1.getHost() + ":" + db1.getFirstMappedPort() + "/" + db1.getDatabaseName());
        registry.add("app.datasource.ds2.url",
            () -> "jdbc:postgresql://" + db2.getHost() + ":" + db2.getFirstMappedPort() + "/" + db2.getDatabaseName());
    }

    @AfterAll
    static void tearDownAll(@Autowired Environment environment) {
        String transactionLogs = environment.getProperty("narayana.log-dir");
        FileUtil.removeDir(Paths.get(transactionLogs).toFile());
    }

    @Test
    public void shouldProduceMessages() {
        int numberOfNames = MyCamelRouter.NAMES.size();
        int numberOfDistinctNames = new HashSet<>(MyCamelRouter.NAMES).size();

        NotifyBuilder notifyProcessed = new NotifyBuilder(camelContext).wereSentTo("direct:processName").whenDone(numberOfNames).create();
        assertTrue(notifyProcessed.matches(20, TimeUnit.SECONDS));

        List<Map<String, Object>> count1 = jdbcTemplate1.queryForList("select COUNT(name) as C from names");
        List<Map<String, Object>> count2 = jdbcTemplate2.queryForList("select COUNT(name) as C from names");

        assertEquals(numberOfDistinctNames, ((Long) count1.get(0).get("C")).intValue());
        assertEquals(numberOfDistinctNames, ((Long) count2.get(0).get("C")).intValue());
    }

}
