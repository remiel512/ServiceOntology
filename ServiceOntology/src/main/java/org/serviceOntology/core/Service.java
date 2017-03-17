package org.serviceOntology.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.PortType;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLWriter;
import javax.xml.namespace.QName;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Service {
	private String targetNamespace;
	private String xmlnsimpl;
	private String xmlnswsdl;
	private String xmlnswsdlrestful;
	private String xmlnsxsd;
	private String requestName;
	private String requestType;
	private String responseName;
	private String portType;
	private String serviceName;
	private String operationName;
	private String owner;
	private JSONObject requestObject;
	private JSONObject paramaterObject;
	private JSONObject gParamaterObject;
	private JSONObject responseObject;
	


	public JSONObject getParamaterObject() {
		return paramaterObject;
	}
	public void setParamaterObject(JSONObject paramaterObject) {
		this.paramaterObject = paramaterObject;
	}
	public JSONObject getgParamaterObject() {
		return gParamaterObject;
	}
	public void setgParamaterObject(JSONObject gParamaterObject) {
		this.gParamaterObject = gParamaterObject;
	}
	public String getTargetNamespace() {
		return targetNamespace;
	}
	public void setTargetNamespace(String targetNamespace) {
		this.targetNamespace = targetNamespace;
	}
	public String getXmlnsimpl() {
		return xmlnsimpl;
	}
	public void setXmlnsimpl(String xmlnsimpl) {
		this.xmlnsimpl = xmlnsimpl;
	}
	public String getXmlnswsdl() {
		return xmlnswsdl;
	}
	public void setXmlnswsdl(String xmlnswsdl) {
		this.xmlnswsdl = xmlnswsdl;
	}
	public String getXmlnswsdlrestful() {
		return xmlnswsdlrestful;
	}
	public void setXmlnswsdlrestful(String xmlnswsdlrestful) {
		this.xmlnswsdlrestful = xmlnswsdlrestful;
	}
	public String getXmlnsxsd() {
		return xmlnsxsd;
	}
	public void setXmlnsxsd(String xmlnsxsd) {
		this.xmlnsxsd = xmlnsxsd;
	}
	public String getRequestName() {
		return requestName;
	}
	public void setRequestName(String requestName) {
		this.requestName = requestName;
	}
	public String getResponseName() {
		return responseName;
	}
	public void setResponseName(String responseName) {
		this.responseName = responseName;
	}
	public String getPortType() {
		return portType;
	}
	public void setPortType(String portType) {
		this.portType = portType;
	}
	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	public JSONObject getRequestObject() {
		return requestObject;
	}
	public void setRequestObject(JSONObject requestObject) {
		this.requestObject = requestObject;
	}
	public JSONObject getResponseObject() {
		return responseObject;
	}
	public void setResponseObject(JSONObject responseObject) {
		this.responseObject = responseObject;
	}

	public String generateWSDL() throws WSDLException, IOException{
		WSDLFactory factory = WSDLFactory.newInstance();
		WSDLWriter writer = factory.newWSDLWriter();

		 Definition def = factory.newDefinition();
		 String restfultns = "http://schemas.xmlsoap.org/wsdl/restful/";
		 String xsd = "http://www.w3.org/2001/XMLSchema";
		 String wsdl = "http://schemas.xmlsoap.org/wsdl/";
		 Types types = def.createTypes();
		 Part requestPart = def.createPart();
		 Part responsePart = def.createPart();
		 Message requestMsg = def.createMessage();
		 Message responseMsg = def.createMessage();
		 Input input = def.createInput();
		 Output output = def.createOutput();
		 Document doc = writer.getDocument(def);
		 Operation operation = def.createOperation();
		 PortType portType = def.createPortType();
		 javax.wsdl.Service srv = def.createService();
		 def.addNamespace("wsdlrestful", restfultns);
		 def.addNamespace("impl", this.xmlnsimpl);
		 //def.setQName(new QName(tns, "StockQuoteService"));
		 def.setTargetNamespace(this.targetNamespace);
		 def.addNamespace("xsd", xsd);
		 def.addNamespace("wsdl", wsdl);
		 
		
		 Element schema = doc.createElementNS(this.targetNamespace, "xsd:schema");
		 schema.setAttribute("elementFormDefault", "qualified");
		 schema.setAttribute("xmlns", xsd);
		 schema.setAttribute("targetNamespace", this.targetNamespace);
		 
		 if(this.getgParamaterObject() != null){
			 Element requestElement = doc.createElement("xsd:element");
			 requestElement.setAttribute("name", this.getOperationName()+".global.paramater");
			 requestElement.appendChild(processJsonElement(doc,this.getgParamaterObject()));
			 schema.appendChild(requestElement);
		 }
		 
		 if(this.getParamaterObject() != null){
			 Element requestElement = doc.createElement("xsd:element");
			 requestElement.setAttribute("name", this.getOperationName()+".paramater");
			 requestElement.appendChild(processJsonElement(doc,this.getParamaterObject()));
			 schema.appendChild(requestElement);
		 }
		 
		 if(this.getRequestObject() != null){
			 Element requestElement = doc.createElement("xsd:element");
			 requestElement.setAttribute("name", this.getRequestName());
			 requestElement.appendChild(processJsonElement(doc,this.getRequestObject()));
			 schema.appendChild(requestElement);
		 }
		 
		 
		 if(this.getResponseObject() != null){
			 Element responseElement = doc.createElement("xsd:element");
			 responseElement.setAttribute("name", this.getResponseName());
			 responseElement.appendChild(processJsonElement(doc,this.getResponseObject()));
			 schema.appendChild(responseElement);
		 }
		 
		 types.setDocumentationElement(schema);

		 def.setTypes(types);

		 requestPart.setName(this.getRequestName());
		 requestPart.setExtensionAttribute(new QName(restfultns, "contentType"), "application/json");
		 requestPart.setExtensionAttribute(new QName(restfultns, "requestType"), this.getRequestType());
		 requestPart.setElementName(new QName(this.getTargetNamespace(), this.getRequestName()));
		 requestMsg.setQName(new QName(this.targetNamespace, this.getRequestName()));
		 requestMsg.addPart(requestPart);
		 requestMsg.setUndefined(false);
		 def.addMessage(requestMsg);
		 responsePart.setName(this.getResponseName());
		 responsePart.setExtensionAttribute(new QName(restfultns, "contentType"), "application/json");
		 responsePart.setElementName(new QName(this.getTargetNamespace(), this.getResponseName()));
		 responseMsg.setQName(new QName(this.targetNamespace, this.getResponseName()));
		 responseMsg.addPart(responsePart);
		 responseMsg.setUndefined(false);
		 def.addMessage(responseMsg);
		 input.setMessage(requestMsg);
		 output.setMessage(responseMsg);
		 operation.setName(this.getOperationName());
		 operation.setInput(input);
		 operation.setOutput(output);
		 operation.setUndefined(false);
		 portType.setQName(new QName(this.targetNamespace, this.getPortType()));
		 portType.addOperation(operation);
		 portType.setUndefined(false);
		 def.addPortType(portType);
		 srv.setQName(new QName(this.targetNamespace, this.getServiceName()));
		 Element serviceElement = doc.createElement("wsdlrestful:address");
		 serviceElement.setAttribute("method", this.getRequestType());
		 
		 serviceElement.setAttribute("oauth", this.getOwner());
		 serviceElement.setAttribute("url", this.getTargetNamespace());
		 srv.setDocumentationElement(serviceElement);
		 def.addService(srv);
		 
//		 if(def != null){
//			 FileOutputStream resultFile = new FileOutputStream(new File(this.getServiceName()+".wsdl"));
//			 writer.writeWSDL(def, resultFile);
//			 resultFile.close();
//		 }
		 
		 return def.toString();
		 

		 
	}
	public String getOperationName() {
		return operationName;
	}
	public void setOperationName(String operationName) {
		this.operationName = operationName;
	}
	public String getRequestType() {
		return requestType;
	}
	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	
	private Element processJsonElement(Document doc, JSONObject j){
		if(j == null)
			return null;
		Iterator<String> keys = j.keys();
		Element first = doc.createElement("xsd:complexType");
		Element second = doc.createElement("xsd:sequence");
		while(keys.hasNext()){
			String value = keys.next();
			Object param = j.get(value);
			//JSONObject param = j.getJSONObject(value);
			
			Element third = doc.createElement("xsd:element");
			third.setAttribute("name", value);
			if(param instanceof String){
				if(value.equals("type")){
					third.setAttribute("type", "xsd:"+(String)param);
				}
			}
			
			if(param instanceof JSONObject){
				Iterator<String> innerkeys = ((JSONObject) param).keys();
				while(innerkeys.hasNext()){
					String key = innerkeys.next();
					Object obj = ((JSONObject) param).get(key);
					
					if(obj instanceof String){
						if(key.equals("type")){
							third.setAttribute("type", "xsd:"+(String)obj);
						}
					}
					
					if(obj instanceof JSONObject){
						third.appendChild(processJsonElement(doc, (JSONObject) obj));
					}
				}
			}
			
			
			second.appendChild(third);
		}
		
		first.appendChild(second);
		return first;
	}
	

}
