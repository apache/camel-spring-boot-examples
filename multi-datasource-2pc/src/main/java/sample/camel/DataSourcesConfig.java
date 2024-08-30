package sample.camel;

import io.agroal.springframework.boot.AgroalDataSource;
import io.agroal.springframework.boot.AgroalDataSourceAutoConfiguration;
import io.agroal.springframework.boot.jndi.AgroalDataSourceJndiBinder;
import org.jboss.tm.XAResourceRecoveryRegistry;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.sql.init.SqlDataSourceScriptDatabaseInitializer;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationAutoConfiguration;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.sql.init.DatabaseInitializationSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.jta.JtaTransactionManager;

import javax.sql.DataSource;

@Configuration
public class DataSourcesConfig {

    // -- first data source config

    @Bean("ds1properties")
    @ConfigurationProperties("app.datasource.ds1")
    public DataSourceProperties firstDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean("ds1")
    @ConfigurationProperties("app.datasource.ds1.agroal")
    public AgroalDataSource firstDataSource(
        @Qualifier("ds1properties") DataSourceProperties properties,
        JtaTransactionManager jtaPlatform,
        XAResourceRecoveryRegistry xaResourceRecoveryRegistry,
        ObjectProvider<AgroalDataSourceJndiBinder> jndiBinder) {

        return new AgroalDataSourceAutoConfiguration(jtaPlatform, xaResourceRecoveryRegistry)
            .dataSource(properties, false, false, jndiBinder);
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

    @Bean("ds2init")
    @ConfigurationProperties("app.datasource.ds2.sql.init")
    public SqlInitializationProperties secondDataSourceInit() {
        return new SqlInitializationProperties();
    }

    @Bean("ds2")
    @ConfigurationProperties("app.datasource.ds2.agroal")
    public AgroalDataSource secondDataSource(
        @Qualifier("ds2properties") DataSourceProperties properties,
        JtaTransactionManager jtaPlatform,
        XAResourceRecoveryRegistry xaResourceRecoveryRegistry,
        ObjectProvider<AgroalDataSourceJndiBinder> jndiBinder) {

        return new AgroalDataSourceAutoConfiguration(jtaPlatform, xaResourceRecoveryRegistry)
            .dataSource(properties, false, false, jndiBinder);
    }

    @Bean("ds2jdbc")
    public JdbcTemplate secondDataSourceJdbcTemplate(@Qualifier("ds2") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

}
