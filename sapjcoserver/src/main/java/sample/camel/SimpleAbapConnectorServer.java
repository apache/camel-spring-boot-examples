package sample.camel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.ext.DestinationDataProvider;
import com.sap.conn.jco.ext.ServerDataProvider;
import com.sap.conn.jco.server.DefaultServerHandlerFactory;
import com.sap.conn.jco.server.JCoServer;
import com.sap.conn.jco.server.JCoServerContextInfo;
import com.sap.conn.jco.server.JCoServerErrorListener;
import com.sap.conn.jco.server.JCoServerExceptionListener;
import com.sap.conn.jco.server.JCoServerFactory;
import com.sap.conn.jco.server.JCoServerFunctionHandler;
import com.sap.conn.jco.server.JCoServerState;
import com.sap.conn.jco.server.JCoServerStateChangedListener;

import sample.camel.util.MyDestinationDataProvider;
import sample.camel.util.MyServerDataProvider;

@Component("simpleAbapConnectorServer")
public class SimpleAbapConnectorServer
		implements JCoServerErrorListener, JCoServerExceptionListener, JCoServerStateChangedListener, InitializingBean {

	private static Logger logger = LoggerFactory.getLogger(SimpleAbapConnectorServer.class);

	@Value("${sap.system.name}")
	private String sapSystemName;

	@Value("${jco.client.ashost}")
	private String jcoClientashost;

	@Value("${jco.client.client}")
	private String jcoClientclient;

	@Value("${jco.client.lang}")
	private String jcoClientlang;

	@Value("${jco.client.sysnr}")
	private String jcoClientsysnr;

	@Value("${jco.client.user}")
	private String jcoClientuser;

	@Value("${jco.client.passwd}")
	private String jcoClientpasswd;

	@Value("${jco.client.network}")
	private String jcoClientnetwork;

	@Value("${jco.server.connection_count}")
	private String jcoServerConnection_count;

	@Value("${jco.server.gwhost}")
	private String jcoServerGwhost;

	@Value("${jco.server.gwserv}")
	private String jcoServerGwserv;

	@Value("${jco.server.progid}")
	private String jcoServerProgid;
	
	@Value("${jco.server.repository_destination}")
	private String jcoServerRepository_destination;
	
	@Value("${jco.server.rfc.name}")
	private String jcoServerrfcname;

	@Value("${jco.server.rfc.tableparam.exist}")
	private String jcoServerRfcTableparamExist;

	@Autowired
	MyDestinationDataProvider myDestinationDataProvider;

	@Autowired
	MyServerDataProvider myServerDataProvider;
	
	@Autowired
	AbapCallHandler abapCallHandler;


	public void init() {
		Properties destinationProperties = new Properties();
		destinationProperties.setProperty(DestinationDataProvider.JCO_ASHOST, jcoClientashost);
		destinationProperties.setProperty(DestinationDataProvider.JCO_SYSNR, jcoClientsysnr);
		destinationProperties.setProperty(DestinationDataProvider.JCO_CLIENT, jcoClientclient);
		destinationProperties.setProperty(DestinationDataProvider.JCO_USER, jcoClientuser);
		destinationProperties.setProperty(DestinationDataProvider.JCO_PASSWD, jcoClientpasswd);
		destinationProperties.setProperty(DestinationDataProvider.JCO_LANG, jcoClientlang);
		destinationProperties.setProperty(DestinationDataProvider.JCO_NETWORK, jcoClientnetwork);
		
		
		Properties serverProperties = new Properties();

		serverProperties.setProperty(ServerDataProvider.JCO_CONNECTION_COUNT, jcoServerConnection_count);
		serverProperties.setProperty(ServerDataProvider.JCO_GWHOST, jcoServerGwhost);
		serverProperties.setProperty(ServerDataProvider.JCO_GWSERV, jcoServerGwserv);
		serverProperties.setProperty(ServerDataProvider.JCO_PROGID, jcoServerProgid);
		serverProperties.setProperty(ServerDataProvider.JCO_REP_DEST, jcoServerRepository_destination);

		myDestinationDataProvider.addDestination(sapSystemName, destinationProperties);
		com.sap.conn.jco.ext.Environment.registerDestinationDataProvider(myDestinationDataProvider);
		myServerDataProvider.addServer(sapSystemName, serverProperties);
		com.sap.conn.jco.ext.Environment.registerServerDataProvider(myServerDataProvider);
		
	}




	public void serve() {
		JCoServer server;
		try {
			server = JCoServerFactory.getServer(
					sapSystemName);
		} catch (JCoException e) {
			throw new RuntimeException("Unable to create the server "
					+ sapSystemName
					+ ", because of " + e.getMessage(), e);
		}

		DefaultServerHandlerFactory.FunctionHandlerFactory factory = new DefaultServerHandlerFactory.FunctionHandlerFactory();
		factory.registerHandler(abapCallHandler.getRFCName(), abapCallHandler);
		server.setCallHandlerFactory(factory);

		// Add listener for errors.
		server.addServerErrorListener(this);
		// Add listener for exceptions.
		server.addServerExceptionListener(this);
		// Add server state change listener.
		server.addServerStateChangedListener(this);

		// Stop server when get end from SystemIn
		new Thread(()->{
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String line = null;
			try {
				while ((line = br.readLine()) != null) {
					// Check if the server should be ended.
					if (line.equalsIgnoreCase("end")) {
						// Stop the server.
						server.stop();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();

		// Start the server
		server.start();
		logger.info("The program can be stopped typing 'END'");
	}

	@Override
	public void serverExceptionOccurred(JCoServer jcoServer, String connectionId, JCoServerContextInfo arg2,
			Exception exception) {
		logger.error("Exception occured on " + jcoServer.getProgramID() + " connection " + connectionId, exception);
	}

	@Override
	public void serverErrorOccurred(JCoServer jcoServer, String connectionId, JCoServerContextInfo arg2, Error error) {
		logger.error("Error occured on " + jcoServer.getProgramID() + " connection " + connectionId, error);
	}

	@Override
	public void serverStateChangeOccurred(JCoServer server, JCoServerState oldState, JCoServerState newState) {
		// Defined states are: STARTED, DEAD, ALIVE, STOPPED;
		// see JCoServerState class for details.
		// Details for connections managed by a server instance
		// are available via JCoServerMonitor
		logger.info("Server state changed from " + oldState.toString() + " to " + newState.toString()
				+ " on server with program id " + server.getProgramID());
		if (newState.equals(JCoServerState.ALIVE)) {
			logger.info("Server with program ID '" + server.getProgramID() + "' is running");
		}
		if (newState.equals(JCoServerState.STOPPED)) {
			logger.info("Exit program");
			System.exit(0);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		init();
		logger.info(" SimpleAbapConnectorServer is ready to start");
		serve();
	}


}