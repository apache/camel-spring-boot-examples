package com.example.demo;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.camel.Handler;

public class AvailableCars {
    private List<String> cars =  Arrays.asList(
            "Toyota Corolla",
            "Honda Civic",
            "Mazda 3",
            "Hyundai Elantra",
            "Subaru Impreza",
            "Volkswagen Jetta",
            "Volkswagen Golf",
            "Ford Fiesta",
            "Ford Focus",
            "Chevrolet Cruze",
            "Kia Ceed",
            "Skoda Octavia",
            "Citroen C4",
            "Peugeot 308"
        ); 
    
    @Handler
    public String getAvailableCar(){
        int index = (new Random()).nextInt(cars.size());
        String jsonResult= "{"+
                " \"bookingId\": "+(new Random()).nextInt(1000)+"," +
                " \"car\": \""+cars.get(index)+"\"," +
                " \"startDate\": \"12-11-2018\"," +
                " \"endDate\": \"15-11-2018\"," +
                " \"price\": "+((new Random()).nextInt(25) + 140) +
               " }";
        return jsonResult;
    }  
}
