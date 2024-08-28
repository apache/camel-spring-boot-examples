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
package org.apache.camel.example.springboot.cxf.otel;

import org.apache.camel.Exchange;
import org.apache.cxf.tracing.TracerContext;

import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;

/**
 * Service interface for managing users.
 */
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface RandomService {

    /**
     * Find a user by the given ID
     *
     * @param attempts
     *            the number of attempts, how many random numbers will be generated
     * @return Results, the results
     */
    @POST
    @Path("/play/{attempts}")
    Results play(@PathParam("attempts") Integer attempts);

    /**
     * Generate random number between 100 and 1000
     *
     * @return RandomNumber, the number
     */
    @GET
    @Path("/generate")
    RandomNumber generate(@Context TracerContext tracer);

    /**
     * Register result
     *
     * @param number
     *            the number to register
     *
     */
    @PUT
    @Path("/register")
    void register(@Valid RandomNumber number, Exchange exchange);

}
