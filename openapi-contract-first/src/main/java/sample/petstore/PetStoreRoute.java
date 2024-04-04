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
package sample.petstore;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import sample.petstore.model.Pet;
import sample.petstore.model.Pet.StatusEnum;

/**
 * Camel routes for the PetStore example
 */
@Component
public class PetStoreRoute extends RouteBuilder {

    @Value("${petName}")
    private String petName;

    @Override
    public void configure() throws Exception {
        // turn on json binding and scan for POJO classes in the model package
        restConfiguration().bindingMode(RestBindingMode.json)
                .bindingPackageScan("sample.petstore.model");

        rest().openApi().specification("petstore.json").missingOperation("ignore");

        from("direct:getPetById")
                .process(e -> {
                    // build response body as POJO
                    Pet pet = new Pet();
                    pet.setId(e.getMessage().getHeader("petId", long.class));
                    pet.setName(petName);
                    pet.setStatus(StatusEnum.AVAILABLE);
                    e.getMessage().setBody(pet);
                });

        from("direct:updatePet")
                .process(e -> {
                    Pet pet = e.getMessage().getBody(Pet.class);
                    pet.setStatus(StatusEnum.PENDING);
                });
    }
}
