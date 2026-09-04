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
package org.apache.camel.example.kafka.avro;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.avro.AvroDataFormat;
import org.springframework.stereotype.Component;

/**
 * Produces {@link Employee} records to Kafka and consumes them back, using Camel's
 * {@link AvroDataFormat} to marshal/unmarshal Avro binary. No Confluent Schema Registry is
 * involved: both routes share the same generated Avro schema on the classpath, so a plain
 * Kafka broker (e.g. started with {@code camel infra run kafka}) is all that is required.
 */
@Component
public class AvroRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        AvroDataFormat employeeAvroFormat = new AvroDataFormat(Employee.getClassSchema());
        employeeAvroFormat.setInstanceClassName(Employee.class.getName());

        from("timer://foo?period={{period}}")
            .process(new KafkaAvroMessageProcessor())
            .marshal(employeeAvroFormat)
            .to("kafka:{{producer.topic}}?brokers={{kafka.bootstrap.url}}"
                + "&keySerializer=org.apache.kafka.common.serialization.StringSerializer"
                + "&valueSerializer=org.apache.kafka.common.serialization.ByteArraySerializer"
                + "&recordMetadata=true")
            .process(new KafkaAvroProcessor());

        from("kafka:{{consumer.topic}}?brokers={{kafka.bootstrap.url}}"
                + "&groupId={{consumer.group}}"
                + "&keyDeserializer=org.apache.kafka.common.serialization.StringDeserializer"
                + "&valueDeserializer=org.apache.kafka.common.serialization.ByteArrayDeserializer")
            .unmarshal(employeeAvroFormat)
            .process(new KafkaAvroMessageConsumerProcessor());
    }
}
