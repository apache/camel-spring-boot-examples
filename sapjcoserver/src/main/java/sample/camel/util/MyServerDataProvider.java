package sample.camel.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sap.conn.jco.ext.ServerDataEventListener;
import com.sap.conn.jco.ext.ServerDataProvider;

@Component("myServerDataProvider")
public class MyServerDataProvider implements ServerDataProvider {
	
	private static final Logger LOG = LoggerFactory.getLogger(MyServerDataProvider.class);

	Map<String, Properties> propertiesForServerName = new HashMap<String, Properties>();

	public void addServer(String destinationName, Properties properties) {
		propertiesForServerName.put(destinationName, properties);
	}

	public Properties getServerProperties(String serverName) {
		if (propertiesForServerName.containsKey(serverName)) {
			return propertiesForServerName.get(serverName);
		} else {
			throw new RuntimeException("JCo server not found: " + serverName);
		}
	}

	@Override
	public void setServerDataEventListener(ServerDataEventListener listener) {
	}

	@Override
	public boolean supportsEvents() {
		return false;
	}	
}