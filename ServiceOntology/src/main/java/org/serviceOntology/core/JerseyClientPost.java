package org.serviceOntology.core;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.xml.ws.Response;

public class JerseyClientPost {

  public static void main(String[] args) {

    try {

    	Client client = ClientBuilder.newClient();
    	
    	WebTarget target = client.target("http://localhost:8080").path("rest/ServiceOntology");

        

        String input = "{\"message\":\"Hello\"}";

        String entity = client.target("http://localhost:8080/rest")
                .path("ServiceOntology")
                .queryParam("greeting", "Hi World!")
                .request(MediaType.APPLICATION_JSON)
                .get(String.class);
        

        

        System.out.println("Output from Server .... \n");
        
        System.out.println(entity);

      } catch (Exception e) {

        e.printStackTrace();

      }

    }
}