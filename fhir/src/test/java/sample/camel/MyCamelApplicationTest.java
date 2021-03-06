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

import java.io.File;

import org.apache.camel.CamelContext;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.MockEndpointsAndSkip;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@CamelSpringBootTest
@SpringBootTest(classes = MyCamelApplication.class,
    properties = "input = target/work/fhir/testinput")
@MockEndpointsAndSkip("fhir*")
public class MyCamelApplicationTest {

    @Autowired
    private CamelContext camelContext;

    @Test
    public void shouldPushConvertedHl7toFhir() throws Exception {
        MockEndpoint mock = camelContext.getEndpoint("mock:fhir:create/resource", MockEndpoint.class);
        mock.expectedBodiesReceived("{\"resourceType\":\"Patient\",\"id\":\"100005056\",\"name\":[{\"family\":\"Freeman\",\"given\":[\"Vincent\"]}]}");

        FileUtils.copyDirectory(new File("src/main/data"), new File("target/work/fhir/testinput"));

        mock.assertIsSatisfied();
    }

}
