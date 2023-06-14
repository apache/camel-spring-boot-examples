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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.jms.JMSException;

@Configuration
public class JmsConfig {

    @Value("${ARTEMIS_HOST}")
    private String artemisHost;
    @Value("${ARTEMIS_SERVICE_PORT}")
    private String artemisPort;
    @Value("${ARTEMIS_SERVICE_USERNAME}")
    private String userName;
    @Value("${ARTEMIS_SERVICE_PASSWORD}")
    private String pass;
    @Value("${ARTEMIS_REMOTE_URI}")
    private String remoteUri;

    public String getArtemisHost() {
       return artemisHost;
    }

    public void setArtemisHost(String artemisHost) {
       this.artemisHost = artemisHost;
    }

    public String getArtemisPort() {
        return artemisPort;
    }

    public void setArtemisPort(String artemisPort) {
        this.artemisPort = artemisPort;
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

    public String getRemoteUri() {
        return remoteUri;
    }
    
    public void setRemoteUri(String remoteUri) {
        this.remoteUri = remoteUri;
    }

    @Bean
    public org.apache.activemq.artemis.jms.client.ActiveMQJMSConnectionFactory artemisConnectionFactory() throws JMSException{
        org.apache.activemq.artemis.jms.client.ActiveMQJMSConnectionFactory artemisConnectionFactory = new org.apache.activemq.artemis.jms.client.ActiveMQJMSConnectionFactory();
        artemisConnectionFactory.setBrokerURL(remoteUri);
        artemisConnectionFactory.setUser(userName);
        artemisConnectionFactory.setPassword(pass);
        return artemisConnectionFactory;
    }

}
