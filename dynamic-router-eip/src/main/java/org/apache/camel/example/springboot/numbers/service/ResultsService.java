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
package org.apache.camel.example.springboot.numbers.service;

import org.apache.camel.util.StopWatch;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Holds processing results so that they can be displayed, etc.
 */
@Service
public class ResultsService {

    /**
     * A counter to keep track of the number of results.
     */
    private final AtomicInteger count = new AtomicInteger(0);

    /**
     * A map of the "bin" or "category" to the list of values in that bin.
     */
    private final ConcurrentHashMap<String, List<Integer>> results;

    /**
     * Create the service and initialize the results map.
     */
    public ResultsService() {
        results = new ConcurrentHashMap<>();
    }

    /**
     * Add a result value for the given key/bin.
     *
     * @param key the bin or category
     * @param value the value to add to the bin
     */
    public void addResult(final String key, int value) {
        results.computeIfAbsent(key, v -> new ArrayList<>()).add(value);
        count.incrementAndGet();
    }

    /**
     * Get the results map.
     *
     * @return the results map
     */
    public Map<String, List<Integer>> getResults() {
        return results;
    }

    /**
     * Get the result count.
     *
     * @return the result count
     */
    public int getTotal() {
        return count.get();
    }

    /**
     * Get a message that contains the statistics of the messaging.
     *
     * @param watch a {@link StopWatch} that was started at the beginning of messaging
     * @return a message that contains the statistics of the messaging
     */
    public String getStatistics(final StopWatch watch) {
        return results.entrySet().stream()
                .sorted((o1, o2) -> o2.getValue().size() - o1.getValue().size())
                .map(e -> String.format("%7s: %7d", e.getKey(), e.getValue().size()))
                .collect(Collectors.joining("\n\t",
                        "Finished\nDynamic Router Spring Boot Numbers Example Results:\n\t",
                        String.format("\nReceived count: %d in %dms", getTotal(), watch.taken())));

    }
}
