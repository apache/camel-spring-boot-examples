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
package org.apache.camel.example.spring.jdbc;

import org.apache.camel.builder.RouteBuilder;

import org.springframework.stereotype.Component;

@Component
public class SpringJdbcRoute extends RouteBuilder {

    protected static final String INSERT_QUERY = "insert into horses(name,age) values('${header.name}',${header.age})";
    protected static final String SELECT_QUERY = "select * from horses";

    @Override
    public void configure() throws Exception {

        rest()
                .post("/horses")
                    .to("direct:persist")
                .get("/horses")
                    .to("direct:read");

        from("direct:persist")
                .choice().when(simple("${header.fail} == 'true'"))
                        .to("direct:rollback")
                    .otherwise()
                        .to("direct:commit");

        from("direct:commit")
                .setBody(simple(INSERT_QUERY))
                .transacted()
                    .to("spring-jdbc:default?resetAutoCommit=false")
                .setBody(constant("executed"));

        from("direct:rollback")
                .setBody(simple(INSERT_QUERY))
                .transacted()
                    .to("spring-jdbc:default?resetAutoCommit=false")
                .rollback("forced to rollback");

        from("direct:read")
                .setBody(simple(SELECT_QUERY))
                .to("spring-jdbc:default");

    }

}
