package sample.camel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class CamelSapServerApplication {

	private static final Logger LOG = LoggerFactory.getLogger(CamelSapServerApplication.class);
	
	
	
	public static void main(String[] args) {
		SpringApplication.run(CamelSapServerApplication.class, args);
		LOG.info("CamelSapServerApplication is up and running");
	}


}
