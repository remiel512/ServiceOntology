package org.serviceOntology.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.serviceOntology.core.IServiceOntology;
import org.serviceOntology.core.OntologyFactory;

@Path("/ServiceOntology/{param}")
public class RestAPI {

	@GET
	public String sayPlainTextHello(@PathParam("param") String apiName, String data) {
		System.out.println(apiName);
		IServiceOntology obj = OntologyFactory.getOntology(apiName);
	    return obj.getWSDL();
	}
}
