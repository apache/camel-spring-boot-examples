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
