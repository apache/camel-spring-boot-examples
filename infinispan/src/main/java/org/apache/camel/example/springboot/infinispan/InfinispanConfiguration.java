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
package org.apache.camel.example.springboot.infinispan;

import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.configuration.cache.CacheMode;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InfinispanConfiguration {

    public static final String CACHE_NAME = "default";

    /**
     * camel infra run infinispan only provisions the "default" cache-container, not a cache inside it, so the
     * cache must be created on first connect. Camel autowires this bean into infinispan:// endpoints by type.
     */
    @Bean
    public RemoteCacheManager cacheContainer(
            @Value("${infinispan.host}") String host,
            @Value("${infinispan.port}") int port,
            @Value("${infinispan.username}") String username,
            @Value("${infinispan.password}") String password) {

        org.infinispan.client.hotrod.configuration.ConfigurationBuilder clientBuilder
                = new org.infinispan.client.hotrod.configuration.ConfigurationBuilder();
        clientBuilder.addServer().host(host).port(port);
        clientBuilder.security().authentication()
                .username(username)
                .password(password)
                .serverName("infinispan")
                .saslMechanism("SCRAM-SHA-512")
                .realm("default");

        RemoteCacheManager cacheManager = new RemoteCacheManager(clientBuilder.build());
        cacheManager.administration().getOrCreateCache(
                CACHE_NAME,
                new org.infinispan.configuration.cache.ConfigurationBuilder()
                        .clustering().cacheMode(CacheMode.DIST_SYNC).build());
        return cacheManager;
    }
}
