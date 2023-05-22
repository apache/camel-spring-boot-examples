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

public class AvailableHotels {
    private List<String> hotels =  Arrays.asList(
            "Four Seasons",
            "Sheraton",
            "The Ritz",
            "Marriott",
            "Hilton",
            "Accor",
            "Hyatt",
            "Radisson"
        ); 

    @Handler
    public String getAvailableHotel(){
        int index = (new Random()).nextInt(hotels.size());
        String jsonResult= "{"+
                " \"bookingId\": "+(new Random()).nextInt(1000)+"," +
                " \"hotel\": \""+hotels.get(index)+"\"," +
                " \"startDate\": \"12-11-2018\"," +
                " \"endDate\": \"15-11-2018\"," +
                " \"price\": "+((new Random()).nextInt(150) + 150) +
                " }";
        return jsonResult;
    }  
}
