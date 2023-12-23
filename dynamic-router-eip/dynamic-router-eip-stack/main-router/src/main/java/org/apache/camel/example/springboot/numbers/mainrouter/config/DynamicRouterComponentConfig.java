/*
 *   Licensed to the Apache Software Foundation (ASF) under one or more
 *   contributor license agreements.  See the NOTICE file distributed with
 *   this work for additional information regarding copyright ownership.
 *   The ASF licenses this file to You under the Apache License, Version 2.0
 *   (the "License"); you may not use this file except in compliance with
 *   the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.apache.camel.example.springboot.numbers.mainrouter.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import static org.apache.camel.component.dynamicrouter.routing.DynamicRouterConstants.MODE_ALL_MATCH;
import static org.apache.camel.component.dynamicrouter.routing.DynamicRouterConstants.MODE_FIRST_MATCH;

/**
 * @param routingChannel    The dynamic router channel.
 * @param recipientMode     The recipient mode -- first matching filter only, or all matching filters.
 */
@Validated
@ConfigurationProperties(prefix = "main-router.dynamic-router-component")
public record DynamicRouterComponentConfig(

        @NotBlank String routingChannel,

        @NotBlank @Pattern(regexp = "^" + MODE_FIRST_MATCH + "|" + MODE_ALL_MATCH + "$") String recipientMode) {
}
