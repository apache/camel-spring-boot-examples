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
package org.apache.camel.example.springboot;

import static org.apache.camel.model.rest.RestParamType.body;
import static org.apache.camel.model.rest.RestParamType.path;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;

import org.springframework.stereotype.Component;

/**
 * A simple Camel REST DSL route.
 */
@Component
public class CamelRouter extends RouteBuilder {

	@Override
	public void configure() throws Exception {
		restConfiguration()
				.bindingMode(RestBindingMode.json);

		// @formatter:off
        rest("/todos").description("Todo REST service")
            .consumes("application/json")
            .produces("application/json")

            .get().description("Find all todos").outType(Todo[].class)
                .responseMessage().code(200).message("All todos successfully returned").endResponseMessage()
                .to("bean:todoService?method=listAll")
        
            .get("/{id}").description("Find todo by ID")
                .outType(Todo.class)
                .param().name("id").type(path).description("The ID of the todo").dataType("long").endParam()
                .responseMessage().code(200).message("Todo successfully returned").endResponseMessage()
                .to("bean:todoService?method=findById(${header.id})")

            .patch("/{id}").description("Update a todo").type(Todo.class)
                .param().name("id").type(path).description("The ID of the todo to update").dataType("long").endParam()
                .param().name("body").type(body).description("The todo to update").endParam()
                .responseMessage().code(204).message("Todo successfully updated").endResponseMessage()
				.to("bean:todoService?method=update(${body}, ${header.id})")

            .post().description("Create a todo").type(Todo.class)
                .param().name("body").type(body).endParam()
                .responseMessage().code(201).message("Todo successfully created").endResponseMessage()
                .to("bean:todoService?method=create")

            .delete().description("Delete completed todos")
                .responseMessage().code(200).message("Todos deleted").endResponseMessage()
                .to("bean:todoService?method=deleteCompleted")

            .delete("/{id}").description("Delete by id")
                .param().name("id").type(path).description("The ID of the todo to delete").dataType("long").endParam()
                .responseMessage().code(200).message("Todo deleted").endResponseMessage()
                .to("bean:todoService?method=deleteOne(${header.id})");
        // @formatter:on
	}
}
