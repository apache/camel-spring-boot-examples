package sample.camel;

import java.util.Iterator;
import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.sap.conn.jco.JCoField;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoParameterList;
import com.sap.conn.jco.server.JCoServerContext;
import com.sap.conn.jco.server.JCoServerFunctionHandler;

import sample.camel.util.XMLUtil;

@Component("abapCallHandler")
public class AbapCallHandler implements JCoServerFunctionHandler {

	@Value("${jco.server.rfc.name}")
	private String RFC_FUNCTION_NAME;

	@Autowired
	private ProducerTemplate producerTemplate;

	@Value("${jco.server.rfc.importparam.exist}")
	public boolean isImportParamExisted;
	@Value("${jco.server.rfc.exportparam.exist}")
	public boolean isExportParamExisted;
	@Value("${jco.server.rfc.tableparam.exist}")
	public boolean isTableParamExisted;

	private static Logger logger = LoggerFactory.getLogger(AbapCallHandler.class);

	public String getRFCName() {
		return this.RFC_FUNCTION_NAME;
	}

	private void printRequestInformation(JCoServerContext serverCtx, JCoFunction function) {
		logger.info("----------------------------------------------------------------");
		logger.info("call              : " + function.getName());
		logger.info("ConnectionId      : " + serverCtx.getConnectionID());
		logger.info("SessionId         : " + serverCtx.getSessionID());
		logger.info("TID               : " + serverCtx.getTID());
		logger.info("repository name   : " + serverCtx.getRepository().getName());
		logger.info("is in transaction : " + serverCtx.isInTransaction());
		logger.info("is stateful       : " + serverCtx.isStatefulSession());
		logger.info("----------------------------------------------------------------");
		logger.info("gwhost: " + serverCtx.getServer().getGatewayHost());
		logger.info("gwserv: " + serverCtx.getServer().getGatewayService());
		logger.info("progid: " + serverCtx.getServer().getProgramID());
		logger.info("----------------------------------------------------------------");
		logger.info("attributes  : ");
		logger.info(serverCtx.getConnectionAttributes().toString());
		logger.info("----------------------------------------------------------------");
	}

	public void handleRequest(JCoServerContext serverCtx, JCoFunction function) {
		// Check if the called function is the supported one.
		if (!function.getName().equals(RFC_FUNCTION_NAME)) {
			logger.error("Function '" + function.getName() + "' is no supported to be handled!");
			return;
		}
		printRequestInformation(serverCtx, function);

		String xmlResultTable = null;

		if (isImportParamExisted) {
			logger.info("--------------------- Import ParamList ------------------------------------------");
			Iterator<JCoField> importIterator = function.getImportParameterList().iterator();
			StringBuilder importParamBuilder=new StringBuilder();
			while (importIterator != null && importIterator.hasNext()) {
				JCoField importParam = importIterator.next();
				logger.info(importParam.getName() + "  " + importParam.getValue() + "\n ");
				importParamBuilder.append(importParam.getName() + "  " + importParam.getValue() + "\n ");
			}
			producerTemplate.sendBody("direct:saprfc", "get result from sap " + importParamBuilder.toString());
			
		} else {
			logger.info("--------------------- No Import ParamList  ------------------------------------------");
		}

		if (isTableParamExisted) {
			logger.info("--------------------- TableParameterList------------------------------------------");
			JCoParameterList tables = function.getTableParameterList();

			if (tables != null) {
				xmlResultTable = XMLUtil.prettyPrintXml(tables.toXML());
				logger.info("Table Result as XML " + " \n" + XMLUtil.prettyPrintXml(xmlResultTable) + "\n ");
				producerTemplate.sendBody("direct:saprfc", "get result from sap " + xmlResultTable);
			}
		} else {
			logger.info("--------------------- No TableParameterList------------------------------------------");
		}

		if (isExportParamExisted) {
			logger.info("--------------------- Export ParamList ------------------------------------------");
			Iterator<JCoField> exportIterator = function.getExportParameterList().iterator();
			StringBuilder exportParamBuilder=new StringBuilder();
			while (exportIterator != null && exportIterator.hasNext()) {
				JCoField exportParam = exportIterator.next();
				logger.info(exportParam.getName() + "  " + exportParam.getValue() + "\n ");
				exportParamBuilder.append(exportParam.getName() + "  " + exportParam.getValue() + "\n ");
				producerTemplate.sendBody("direct:saprfc", "get result from sap " + exportParamBuilder.toString());
			}
		} else {
			logger.info("--------------------- No Export ParamList ------------------------------------------");
		}
	}
}