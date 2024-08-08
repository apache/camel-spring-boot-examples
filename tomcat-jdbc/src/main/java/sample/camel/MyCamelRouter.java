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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * A simple Camel route that triggers from a timer and calls a bean and prints to system out.
 * <p/>
 * Use <tt>@Component</tt> to make Camel auto-detect this route when starting.
 */
@Component
public class MyCamelRouter extends RouteBuilder {

    // we can use spring dependency injection
    @Autowired
    MyBean myBean;

    @Override
    public void configure() throws Exception {
        // route to keep inserting messages in the database
        // start from a timer
        from("timer:hello?period={{myPeriod}}")
            .routeId("hello")
            // store the message from the bean in a header
            .setHeader("message").method(myBean, "saySomething")
            // prepare the insert SQL
            .setBody(simple("insert into messages(message) values('${header.message}')"))
            // insert the message into the database
            .to("spring-jdbc:default")
            // print the body
            .to("log:info");

        // route to print inserted messages
        from("timer:query?period={{myPeriod}}")
            .routeId("query")
            .setBody(constant("select count(*) as \"C\" from messages"))
            .to("spring-jdbc:default")
            .setBody(simple("There are ${body[0][C]} messages in the database."))
            .to("log:info");
    }

}
