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
package org.apache.camel.example.springboot.cxf;

import java.util.Collection;

import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Service interface for managing users.
 */
public interface UserService {

    /**
     * Find a user by the given ID
     *
     * @param id
     *            the ID of the user
     * @return the user, or <code>null</code> if user not found.
     */
    @GET
    @Path("/user/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    User findUser(@PathParam("id") Integer id);

    /**
     * Find all users
     *
     * @return a collection of all users
     */
    @GET
    @Path("/user")
    @Produces(MediaType.APPLICATION_JSON)
    Collection<User> findUsers();

    /**
     * Update the given user
     *
     * @param user
     *            the user
     */
    @PUT
    @Path("/user")
    @Consumes(MediaType.APPLICATION_JSON)
    Response updateUser(@Valid User user);

}
