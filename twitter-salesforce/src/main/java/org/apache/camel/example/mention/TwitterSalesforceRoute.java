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
package org.apache.camel.example.mention;

import java.lang.Class;
import java.lang.reflect.Method;
import org.apache.camel.salesforce.draftdto.Contact;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;
import twitter4j.v1.Status;
import twitter4j.v1.User;

@Component
public class TwitterSalesforceRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("twitter-timeline:mentions")
            .log("Tweet id ${body.id} mention: ${body}")
            .process(exchange -> {
                Status status = exchange.getIn().getBody(Status.class);
                User user = status.getUser();
                String name = user.getName();
                String screenName = user.getScreenName();
                Class contact = null;
                if (Class.forName("org.apache.camel.salesforce.dto.Contact") != null) {
                    contact = Class.forName("org.apache.camel.salesforce.dto.Contact");
                } else {
                    contact = Contact.class;
                }

                Object contactObject = contact.newInstance();
                Method setLastName = contact.getMethod("setLastName", String.class);
                Method setTwitterScreenName__c = contact.getMethod("setTwitterScreenName__c", String.class);
                setLastName.invoke(contactObject, name);
                setTwitterScreenName__c.invoke(contactObject, screenName);
                exchange.getIn().setBody(contactObject);

            })
            .to("salesforce:upsertSObject?sObjectIdName=TwitterScreenName__c")
            .log("SObject ID: ${body?.id}");
    }

}
