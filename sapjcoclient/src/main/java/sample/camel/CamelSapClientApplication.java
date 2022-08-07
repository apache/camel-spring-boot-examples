package sample.camel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class CamelSapClientApplication {

	private static final Logger LOG = LoggerFactory.getLogger(CamelSapClientApplication.class);
	
	
	
	public static void main(String[] args) {
		SpringApplication.run(CamelSapClientApplication.class, args);
		LOG.info("CamelSapClientApplication is up and running");
	}


}
