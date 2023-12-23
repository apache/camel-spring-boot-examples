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

package org.apache.camel.example.springboot.numbers.mainrouter.web;

import org.apache.camel.example.springboot.numbers.mainrouter.service.NumberStatisticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
public class NumberStatisticsController {

    private final NumberStatisticsService numberStatisticsService;

    public NumberStatisticsController(NumberStatisticsService numberStatisticsService) {
        this.numberStatisticsService = numberStatisticsService;
    }

    @GetMapping(path = "/counts")
    public Mono<Map<String, Long>> getCounts() {
        return Mono.just(numberStatisticsService.getCounts());
    }
}
