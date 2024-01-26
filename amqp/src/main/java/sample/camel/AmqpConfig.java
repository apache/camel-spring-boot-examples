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

import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AmqpConfig {

    /* tmielke: None of this code is really needed.  If we rather want to configure
       camel-amqp using camel.component.amqp.* properties, then these aren't
       needed. I struggle to have a strong opinion on whether we want to configure
       username and password directly on the ConnectionFactory below or via
       camel.component.amqp.password and camel.component.amqp.username. 
       I guess there are arguments either way.

    */
    // @Value("${AMQP_HOST}")
    // private String amqpHost;
    // @Value("${AMQP_SERVICE_PORT}")
    // private String amqpPort;
    // @Value("${AMQP_SERVICE_USERNAME}")
    // private String userName;
    // @Value("${AMQP_SERVICE_PASSWORD}")
    // private String pass;
    @Value("${AMQP_REMOTE_URI}")
    private String remoteUri;

    /*
    public String getAmqpHost() {
        return amqpHost;
    }
    
    public void setAmqpHost(String amqpHost) {
        this.amqpHost = amqpHost;
    }

    public String getAmqpPort() {
        return amqpPort;
    }

    public void setAmqpPort(String amqpPort) {
        this.amqpPort = amqpPort;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }
    */

    // the only really required config property, as it cannot be set
    // via camel.component.amqp.* properties
    public String getRemoteUri() {
        return remoteUri;
    }
    
    public void setRemoteUri(String remoteUri) {
        this.remoteUri = remoteUri;
    }

    
    /* tmielke: I would even disable @Bean on this method and only enable it on the next method
       that creates the JmsPoolConnectionFactory. 
    */
    // @Bean
    public org.apache.qpid.jms.JmsConnectionFactory amqpConnectionFactory() {
        org.apache.qpid.jms.JmsConnectionFactory jmsConnectionFactory = new org.apache.qpid.jms.JmsConnectionFactory();
        jmsConnectionFactory.setRemoteURI(remoteUri);
        // jmsConnectionFactory.setUsername(userName);
        // jmsConnectionFactory.setPassword(pass);
        return jmsConnectionFactory;
    }
    

    /* tmielke: Recommendation should be to use connection pooling. By using a named bean
       we could directly reference it in 
       camel.component.amqp.connection-factory = #connectionPoolFactory
       but its technically not needed if there is only one connectionFactory registered in 
       the Spring Boot registry 
    */ 
    @Bean(name = "connectionPoolFactory", initMethod = "start", destroyMethod = "stop")
    public JmsPoolConnectionFactory jmsPoolConnectionFactory() {
        JmsPoolConnectionFactory jmsPoolConnectionFactory = new JmsPoolConnectionFactory();
        jmsPoolConnectionFactory.setConnectionFactory(amqpConnectionFactory());
        return jmsPoolConnectionFactory;
    }
}
