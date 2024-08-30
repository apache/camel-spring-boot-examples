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
import org.apache.camel.spring.spi.TransactionErrorHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.print.attribute.standard.MediaSize;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple Camel route that triggers from a timer and keeps trying to insert names in 2 databases.
 * <p/>
 * Use <tt>@Component</tt> to make Camel auto-detect this route when starting.
 */
@Component
public class MyCamelRouter extends RouteBuilder {

    public static final List<String> NAMES = List.of(
        "Maria",
        "João",
        "Diego",
        "Ramona",
        "Maria",
        "Ramona"
    );

    @Override
    public void configure() throws Exception {
        // route to insert names in the database
        // split the list and send each name to be processed
        from("timer://runOnce?repeatCount=1&delay=1000")
            .id("split")
            .setBody(constant(NAMES))
            .split(body())
            .to("direct:processName");

        from("direct:processName")
            .transacted()
            .id("processName")
            .delay(simple("{{myPeriod}}"))
            .to("log:info?showAll=true")
            .setHeader("name", body())
            // prepare the insert SQL
            .setBody(simple("insert into names(name) values('${header.name}')"))
            .log("insert into the first database (non-unique)")
            .to("spring-jdbc:ds1?resetAutoCommit=false")
            .log("insert into the second database (unique)")
            .to("spring-jdbc:ds2?resetAutoCommit=false");

        // route to print inserted names
        from("timer:query?period={{myPeriod}}")
            .routeId("query1")
            .delay(200)
            .setBody(constant("select count(*) as \"C\" from names"))
            .to("spring-jdbc:ds1")
            .setBody(simple("There are ${body[0][C]} names in the ds1 database."))
            .to("log:info");

        // route to print inserted names
        from("timer:query?period={{myPeriod}}")
            .routeId("query2")
            .delay(200)
            .setBody(constant("select count(*) as \"C\" from names"))
            .to("spring-jdbc:ds2")
            .setBody(simple("There are ${body[0][C]} names in the ds2 database."))
            .to("log:info");
    }

}
