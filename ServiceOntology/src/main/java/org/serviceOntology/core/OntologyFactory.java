package org.serviceOntology.core;

import java.io.IOException;
import java.util.List;

import org.json.JSONObject;

public class OntologyFactory {
	
	public static IServiceOntology createOntology(JSONObject jobj){
		return null;
	}
	
	public static IServiceOntology getOntology(String serviceName){
		APIParser a = new APIParser();
		try {
			List<Service> list = a.parse(serviceName);
			ServiceOntologyImpl soi = new ServiceOntologyImpl();
			soi.setService(list.get(0));
			return soi;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
