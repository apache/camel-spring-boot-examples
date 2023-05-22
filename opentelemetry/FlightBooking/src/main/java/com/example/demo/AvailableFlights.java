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
package com.example.demo;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.camel.Handler;

public class AvailableFlights {
    private List<String> flights =  Arrays.asList(
        "American Airlines",
        "Delta Air Lines",
        "Lufthansa",
        "United Airlines",
        "Air France–KLM",
        "IAG",
        "Southwest Airlines",
        "China Southern Airlines",
        "All Nippon Airways",
        "China Eastern Airlines",
        "Ryanair",
        "Air China",
        "British Airways",
        "Emirates",
        "Turkish Airlines",
        "Qatar Airways"
    ); 
    @Handler
    public String getAvailableFlight(){
        int index = (new Random()).nextInt(flights.size());
        String jsonResult= "{"+
                " \"bookingId\": "+(new Random()).nextInt(1000)+"," +
                " \"flight\": \""+flights.get(index)+" "+((new Random()).nextInt(10000))+"\"," +
                " \"startDate\": \"12-11-2018\"," +
                " \"endDate\": \"15-11-2018\"," +
                " \"price\": "+((new Random()).nextInt(100) + 100) +
                " }";
        return jsonResult;
    }  
}
