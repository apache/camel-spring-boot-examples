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
package org.apache.camel.example.springboot.milvus.bean;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class VectorUtils implements Processor {

    private String normalizationRanges;
    private float[][] ranges;

    @Override
    public void process(Exchange exchange) throws Exception {
        // Converts a semicolon-separated string of numeric values into a normalized List feature vector.
        String[] parts = exchange.getIn().getBody(String.class).trim().split(";");
        List<Float> vector = new ArrayList<>(parts.length);
        for (int i = 0; i < parts.length; i++) {
            float val = Float.parseFloat(parts[i].trim());
            if (ranges != null && i < ranges.length) {
                val = (val - ranges[i][0]) / (ranges[i][1] - ranges[i][0]);
                val = Math.max(0.0f, Math.min(1.0f, val));
            }
            vector.add(val);
        }
        exchange.getIn().setBody(vector);
    }

    public String getNormalizationRanges() {
        return normalizationRanges;
    }

    /**
     * Each dimension is normalized to a 0-1 range using configurable min/max ranges, so all features contribute equally to distance calculations in Milvus.
     */
    public void setNormalizationRanges(String normalizationRanges) {
        this.normalizationRanges = normalizationRanges;
        if (normalizationRanges != null && !normalizationRanges.isBlank()) {
            String[] pairs = normalizationRanges.split(",");
            this.ranges = new float[pairs.length][2];
            for (int i = 0; i < pairs.length; i++) {
                String[] minMax = pairs[i].trim().split(":");
                this.ranges[i][0] = Float.parseFloat(minMax[0].trim());
                this.ranges[i][1] = Float.parseFloat(minMax[1].trim());
            }
        } else {
            this.ranges = null;
        }
    }
}
