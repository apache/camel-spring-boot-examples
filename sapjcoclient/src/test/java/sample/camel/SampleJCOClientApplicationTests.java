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


import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.MockEndpoints;
import org.apache.camel.test.spring.junit5.UseAdviceWith;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.MethodMode;

@CamelSpringBootTest
@SpringBootTest
@UseAdviceWith
@MockEndpoints("file:sapoutput")
public class SampleJCOClientApplicationTests {
	
    @Autowired
    ProducerTemplate producerTemplate;
    

    @Autowired
    CamelContext camelContext;
    
    @EndpointInject("mock:file:sapoutput")
    MockEndpoint mockfile;

    @Test
    @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
    public void testSendMsg() throws Exception {
    	  
        String msg="ABAP RFC TEST";
    	
    	AdviceWith.adviceWith(camelContext, "route_direct_saprfc", routeBuilder -> {
            routeBuilder.weaveById("callSapProcessor").replace().process(new Processor() {

				@Override
				public void process(Exchange exchange) throws Exception {
					exchange.getIn().setBody(msg);
					
				}}).id("mockcallSapProcessor");
           
         });
         camelContext.start();
    	
         //MockEndpoint mockfile = camelContext.getEndpoint("mock:file:target/sapoutput", MockEndpoint.class);
      
    	  mockfile.expectedBodiesReceived(msg);
          mockfile.assertIsSatisfied();
    }
}
