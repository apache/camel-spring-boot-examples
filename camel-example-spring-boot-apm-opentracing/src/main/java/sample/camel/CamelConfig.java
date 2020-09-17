package sample.camel;

import co.elastic.apm.opentracing.ElasticApmTracer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CamelConfig {
    @Bean
    public ElasticApmTracer tracer() {
        return new ElasticApmTracer();
    }

    @Bean
    public CounterBean counterBean() {
        return new CounterBean();
    }
}
