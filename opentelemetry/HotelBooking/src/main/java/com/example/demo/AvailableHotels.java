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
