package sample.camel.util;



import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XMLUtil {
	static Logger logger = LoggerFactory.getLogger("XMLUtil");

	public static String prettyPrintXml(String xmlStringToBeFormatted) {
	    String formattedXmlString = null;
	    try {
	        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
	        documentBuilderFactory.setValidating(false);
	        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
	        InputSource inputSource = new InputSource(new StringReader(xmlStringToBeFormatted));
	        Document document = documentBuilder.parse(inputSource);

	        Transformer transformer = TransformerFactory.newInstance().newTransformer();
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

	        StreamResult streamResult = new StreamResult(new StringWriter());
	        DOMSource dOMSource = new DOMSource(document);
	        transformer.transform(dOMSource, streamResult);
	        formattedXmlString = streamResult.getWriter().toString().trim();
	    } catch (Exception ex) {
	        StringWriter sw = new StringWriter();
	        ex.printStackTrace(new PrintWriter(sw));
	        logger.error(sw.toString());
	    }
	    return formattedXmlString;
	}
}
