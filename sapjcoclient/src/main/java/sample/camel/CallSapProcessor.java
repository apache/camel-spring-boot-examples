package sample.camel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.sap.conn.jco.AbapException;
import com.sap.conn.jco.JCoContext;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoField;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoParameterList;
import com.sap.conn.jco.ext.DestinationDataProvider;
import sample.camel.util.MyDestinationDataProvider;
import sample.camel.util.XMLUtil;

@Component("callSapProcessor")
public class CallSapProcessor implements Processor{
	private static final Logger LOG = LoggerFactory.getLogger(CallSapProcessor.class);
	
	@Value("${jco.client.rfcName}")
	private String rfcName;
	
	@Value("${jco.client.sapSystemName}")
	private String sapSystemName;
	
	@Value("${jco.client.id}")
	private String clientId;
	
	
	@Autowired
	MyDestinationDataProvider myDestinationDataProvider;
	
	@Autowired
	private Environment env;
	
	private HashMap<String,JCoDestination> jcoDestinations= new HashMap<String,JCoDestination>();


	

	private JCoDestination initDestination(String sapSystemName,String clientID) throws JCoException {
		String destinationNameClient=sapSystemName+clientID;
		if(jcoDestinations.get(destinationNameClient)!=null&&!jcoDestinations.get(destinationNameClient).isValid()) {
			
		}else {
			Properties connectProperties = new Properties();
			connectProperties.setProperty(DestinationDataProvider.JCO_ASHOST, env.getProperty("jco.client.ashost"));
			connectProperties.setProperty(DestinationDataProvider.JCO_SYSNR, env.getProperty("jco.client.sysnr"));
			connectProperties.setProperty(DestinationDataProvider.JCO_CLIENT, clientID);
			connectProperties.setProperty(DestinationDataProvider.JCO_USER, env.getProperty("jco.client.user"));
			connectProperties.setProperty(DestinationDataProvider.JCO_PASSWD, env.getProperty("jco.client.passwd"));
			connectProperties.setProperty(DestinationDataProvider.JCO_LANG, env.getProperty("jco.client.lang"));
			myDestinationDataProvider.addDestination(destinationNameClient, connectProperties);
			if(!com.sap.conn.jco.ext.Environment.isDestinationDataProviderRegistered()) {
				com.sap.conn.jco.ext.Environment.registerDestinationDataProvider(myDestinationDataProvider);
			}
			
			JCoDestination jCoDestination= JCoDestinationManager.getDestination(destinationNameClient);
			jcoDestinations.put(destinationNameClient, jCoDestination);	
		}
		return jcoDestinations.get(destinationNameClient);
	}
	

	
	public void process(Exchange exg) throws JCoException {
		String result=callSAPRFC(sapSystemName,clientId);
		exg.getIn().setBody(result);
	}
	
	private String callSAPRFC( String sapSystemName, String clientID) throws JCoException {
		JCoDestination destination = null;
		StringBuilder builder =new StringBuilder();
		try {
			destination = initDestination(sapSystemName, clientID);
			
			JCoContext.begin(destination);
			JCoFunction function = destination.getRepository().getFunction(rfcName);
			
		
			
			if (function.getImportParameterList() != null) {

				Iterator<JCoField> importIterator = function.getImportParameterList().iterator();
				while (importIterator != null && importIterator.hasNext()) {
					JCoField importParam = importIterator.next();
					LOG.info(importParam.getName() + "  " + importParam.getValue() + System.getProperty("line.separator"));
					builder.append(importParam.getName() + "  " + importParam.getValue() + System.getProperty("line.separator"));
				}
			}

			function.execute(destination);
			
			
			if (function.getTableParameterList() != null) {
				LOG.info("--------------------- TableParameterList------------------------------------------");
				JCoParameterList tables = function.getTableParameterList();
				String xmlResult = null;
				if (tables != null) {
					xmlResult = tables.toXML();
					LOG.info("Table Result as XML " + System.getProperty("line.separator") + XMLUtil.prettyPrintXml(xmlResult) + System.getProperty("line.separator"));
					builder.append("Table Result as XML " + System.getProperty("line.separator") + XMLUtil.prettyPrintXml(xmlResult)+ System.getProperty("line.separator"));
				}
			}

			if (function.getExportParameterList() != null) {
				LOG.info("--------------------- Export ParamList ------------------------------------------");
				Iterator<JCoField> exportIterator = function.getExportParameterList().iterator();
				while (exportIterator != null && exportIterator.hasNext()) {
					JCoField exportParam = exportIterator.next();
					LOG.info(exportParam.getName() + "  " + exportParam.getValue() + System.getProperty("line.separator"));
					builder.append(exportParam.getName() + "  " + exportParam.getValue() + System.getProperty("line.separator"));
					
				}
			}
		} catch (AbapException e) {
			LOG.error("#### Error happend on CallSapProcessor {}", e);
		} finally {
			if (destination != null) {
				JCoContext.end(destination);
			}
		}
		return builder.toString();
	}


}
