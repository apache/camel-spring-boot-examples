package sample.camel;

import org.apache.camel.component.jms.springboot.JmsComponentConfiguration;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.jms.ConnectionFactory;

@SpringBootApplication
public class SampleCamelServicebusJmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(SampleCamelServicebusJmsApplication.class, args);
    }

    private static final String AMQP_URI_FORMAT = "amqps://%s?amqp.idleTimeout=%d";

    //the format is like: <namespacename>.servicebus.windows.net
    private static final String host = "";

    private static final long idleTimeout = 1800000;

    @Bean
    public ConnectionFactory jmsConnectionFactory(JmsComponentConfiguration conf) {
        String username = conf.getConfiguration().getUsername();
        String password = conf.getConfiguration().getPassword();
        String remoteUri = String.format(AMQP_URI_FORMAT, host, idleTimeout);
        return new JmsConnectionFactory(username, password, remoteUri);
    }
}
