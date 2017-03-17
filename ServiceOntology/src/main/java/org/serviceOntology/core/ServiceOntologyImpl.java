package org.serviceOntology.core;

import java.io.IOException;

import javax.wsdl.WSDLException;

public class ServiceOntologyImpl extends ServiceOntology {

	@Override
	public void setService(Service s) {
		myService = s;
		serviceName = myService.getServiceName();
	}
	
	public String getServiceName(){
		return serviceName;
	}

	@Override
	public String getWSDL() {
		try {
			return myService.generateWSDL();
		} catch (WSDLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
