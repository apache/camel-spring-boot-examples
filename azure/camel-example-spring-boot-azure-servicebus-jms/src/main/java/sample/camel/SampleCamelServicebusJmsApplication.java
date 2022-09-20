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

import org.apache.camel.component.jms.springboot.JmsComponentConfiguration;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.jms.ConnectionFactory;

@SpringBootApplication
public class SampleCamelServicebusJmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SampleCamelServicebusJmsApplication.class, args);
    }

    private static final String AMQP_URI_FORMAT = "amqps://%s?amqp.idleTimeout=%d";

    @Value("${hostName}")
    private String hostName;

    private static final long idleTimeout = 1800000;

    @Bean
    public ConnectionFactory jmsConnectionFactory(JmsComponentConfiguration conf) {
        String username = conf.getConfiguration().getUsername();
        String password = conf.getConfiguration().getPassword();
        String remoteUri = String.format(AMQP_URI_FORMAT, hostName, idleTimeout);
        return new JmsConnectionFactory(username, password, remoteUri);
    }

}
