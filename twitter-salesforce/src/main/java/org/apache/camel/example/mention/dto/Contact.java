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
package org.apache.camel.example.mention.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.camel.component.salesforce.api.dto.AbstractSObjectBase;

/**
 * Minimal, hand-maintained stand-in for the Salesforce {@code Contact} DTO that
 * {@code camel-salesforce-maven-plugin} would otherwise generate from a live org (see the
 * {@code generate-salesforce-dto} Maven profile). It only carries the fields this example
 * actually sets, so the module builds and runs without needing Salesforce credentials at
 * build time.
 */
public class Contact extends AbstractSObjectBase {

    private String lastName;
    private String twitterScreenNameC;

    public Contact() {
        getAttributes().setType("Contact");
    }

    // Salesforce field API names are case-sensitive (LastName, TwitterScreenName__c); without
    // @JsonProperty, Jackson would decapitalize the leading letter of the bean-derived property
    // name and silently send the wrong field names to the Salesforce REST API.
    @JsonProperty("LastName")
    public String getLastName() {
        return lastName;
    }

    @JsonProperty("LastName")
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @JsonProperty("TwitterScreenName__c")
    public String getTwitterScreenName__c() {
        return twitterScreenNameC;
    }

    @JsonProperty("TwitterScreenName__c")
    public void setTwitterScreenName__c(String twitterScreenNameC) {
        this.twitterScreenNameC = twitterScreenNameC;
    }

}
