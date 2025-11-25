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
package sample.camel;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Route that demonstrates Spring AI tool/function calling capabilities.
 * The AI can invoke the weather tool to get current weather information.
 */
@Component
public class ToolRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        // Define the weather tool that can be called by the AI
        from("spring-ai-tools:weatherTool?tags=weather&description=Get the current weather for a location"
            + "&parameter.location=string"
            + "&parameter.location.description=The city, e.g. Rome"
            + "&parameter.location.required=true")
            .routeId("weatherTool")
            .log("Weather tool called for location: ${header.location}")
            .to("direct:geocode")
            .log("Geocoding result - Latitude: ${header.latitude}, Longitude: ${header.longitude}")
            .to("direct:fetchWeather")
            .log("Weather tool response: ${body}");

        // Geocoding route - convert location name to coordinates
        from("direct:geocode")
            .routeId("geocodeRoute")
            .setHeader("savedLocation", simple("${header.location}"))
            .toD("https://geocoding-api.open-meteo.com/v1/search?name=${header.location}&count=1&language=en&format=json")
            .setHeader("latitude", jq(".results[0].latitude"))
            .setHeader("longitude", jq(".results[0].longitude"))
            .setHeader("location", simple("${header.savedLocation}"));

        // Weather fetching route - get current weather for coordinates
        from("direct:fetchWeather")
            .routeId("fetchWeatherRoute")
            .toD("https://api.open-meteo.com/v1/forecast?latitude=${header.latitude}&longitude=${header.longitude}&current=temperature_2m,weather_code,precipitation,wind_speed_10m,wind_direction_10m,relative_humidity_2m,cloud_cover&temperature_unit=celsius&wind_speed_unit=kmh")
            .setBody(simple("The weather in ${header.location} is ${jq(.current.temperature_2m)} degrees Celsius with ${jq(.current.relative_humidity_2m)}% humidity, ${jq(.current.cloud_cover)}% cloud cover, wind speed ${jq(.current.wind_speed_10m)} km/h from ${jq(.current.wind_direction_10m)} degrees, and ${jq(.current.precipitation)} mm precipitation."));

        // Route that uses AI with function calling
        from("timer:toolChat?repeatCount=1&delay=20000")
            .routeId("toolChatRoute")
            .setBody(constant("What is the weather like in Rome, Italy? Answer with English slang"))
            .log("Sending question with tool capability to AI: ${body}")
            .to("spring-ai-chat:toolChat?tags=weather")
            .log("AI Response with tool usage: ${body}");
    }
}
