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

import io.agroal.api.security.AgroalSecurityProvider;
import io.agroal.springframework.boot.AgroalDataSource;
import io.agroal.springframework.boot.AgroalDataSourceAutoConfiguration;
import io.agroal.springframework.boot.jndi.AgroalDataSourceJndiBinder;
import org.jboss.tm.XAResourceRecoveryRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.jta.JtaTransactionManager;

import javax.sql.DataSource;
import java.util.Collections;

@Configuration
public class DataSourcesConfig {

    private final ObjectProvider<JtaTransactionManager> jtaPlatform;
    private final ObjectProvider<XAResourceRecoveryRegistry> xaResourceRecoveryRegistry;
    private final ObjectProvider<AgroalDataSourceJndiBinder> jndiBinder;
    private final ObjectProvider<AgroalSecurityProvider> securityProvider;

    public DataSourcesConfig(
        ObjectProvider<JtaTransactionManager> jtaPlatform,
        ObjectProvider<XAResourceRecoveryRegistry> xaResourceRecoveryRegistry,
        ObjectProvider<AgroalDataSourceJndiBinder> jndiBinder,
        ObjectProvider<AgroalSecurityProvider> securityProvider) {
        this.jtaPlatform = jtaPlatform;
        this.xaResourceRecoveryRegistry = xaResourceRecoveryRegistry;
        this.jndiBinder = jndiBinder;
        this.securityProvider = securityProvider;
    }

    private AgroalDataSource createDataSource(DataSourceProperties properties) {
        return new AgroalDataSourceAutoConfiguration(jtaPlatform, xaResourceRecoveryRegistry, jndiBinder, securityProvider)
            .dataSource(properties, true, false, false, Collections.emptyList(), Collections.emptyList());
    }

    // -- first data source config

    @Bean("ds1properties")
    @ConfigurationProperties("app.datasource.ds1")
    public DataSourceProperties firstDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean("ds1")
    @ConfigurationProperties("app.datasource.ds1.agroal")
    public AgroalDataSource firstDataSource(@Qualifier("ds1properties") DataSourceProperties properties) {
        return createDataSource(properties);
    }

    @Bean("ds1jdbc")
    public JdbcTemplate firstDataSourceJdbcTemplate(@Qualifier("ds1") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    // -- second data source config

    @Bean("ds2properties")
    @ConfigurationProperties("app.datasource.ds2")
    public DataSourceProperties secondDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean("ds2")
    @ConfigurationProperties("app.datasource.ds2.agroal")
    public AgroalDataSource secondDataSource(@Qualifier("ds2properties") DataSourceProperties properties) {
        return createDataSource(properties);
    }

    @Bean("ds2jdbc")
    public JdbcTemplate secondDataSourceJdbcTemplate(@Qualifier("ds2") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

}
