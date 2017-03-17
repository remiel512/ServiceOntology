package org.serviceOntology.core;

import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.PortType;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLWriter;
import javax.xml.namespace.QName;

import org.apache.cxf.tools.corba.processors.idl.WSDLSchemaManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sun.xml.internal.ws.wsdl.parser.WSDLConstants;

public class WSDL4JTest {

	
	private static ExtensionRegistry registry;

	public static void main(String[] args) throws WSDLException{
		WSDLFactory factory = WSDLFactory.newInstance();
		WSDLWriter writer = factory.newWSDLWriter();

		 Definition def = factory.newDefinition();
		 String tns = "http://api.accuweather.com/localweather/v1/{locationKey}";//input
		 String restfultns = "http://schemas.xmlsoap.org/wsdl/restful/";
		 String xsd = "http://www.w3.org/2001/XMLSchema";
		 String impl = "http://api.accuweather.com/localweather/v1/{locationKey}";//input
		 String wsdl = "http://schemas.xmlsoap.org/wsdl/";
		 Types types = def.createTypes();
		 Part part1 = def.createPart();
		 Part part2 = def.createPart();
		 Message msg1 = def.createMessage();
		 Message msg2 = def.createMessage();
		 Input input = def.createInput();
		 Output output = def.createOutput();
		 Document doc = writer.getDocument(def);
		 Operation operation = def.createOperation();
		 PortType portType = def.createPortType();
		 def.addNamespace("wsdlrestful", restfultns);
		 //def.setQName(new QName(tns, "StockQuoteService"));
		 def.setTargetNamespace(tns);
		 def.addNamespace("xsd", xsd);
		 def.addNamespace("impl", impl);
		 def.addNamespace("wsdl", wsdl);
		 
		
		 Element schema = doc.createElementNS(tns, "wsdl:schema");
		 schema.setAttribute("elementFormDefault", "qualified");
		 schema.setAttribute("xmlns", xsd);
		 schema.setAttribute("targetNamespace", tns);
		 
		 Element typesa = doc.createElement("import");
		 typesa.setAttribute("namespace", "aaaaaa");
		 
		 typesa.setAttribute("schmaLoaction", "bbbb.xsd");
		 
		 schema.appendChild(typesa);
		 

		 types.setDocumentationElement(schema);

		 def.setTypes(types);
		 
		 part1.setName("symbol");
		 part1.setTypeName(new QName(xsd, "string"));
		 msg1.setQName(new QName(tns, "getQuoteInput"));
		 msg1.addPart(part1);
		 msg1.setUndefined(false);
		 def.addMessage(msg1);
		 part2.setName("quote");
		 part2.setTypeName(new QName(xsd, "float"));
		 msg2.setQName(new QName(tns, "getQuoteOutput"));
		 msg2.addPart(part2);
		 msg2.setUndefined(false);
		 def.addMessage(msg2);
		 input.setMessage(msg1);
		 output.setMessage(msg2);
		 operation.setName("getQuote");
		 operation.setInput(input);
		 operation.setOutput(output);
		 operation.setUndefined(false);
		 portType.setQName(new QName(tns, "GetQuote"));
		 portType.addOperation(operation);
		 portType.setUndefined(false);
		 def.addPortType(portType);

		 writer.writeWSDL(def, System.out);
	}
	
}
