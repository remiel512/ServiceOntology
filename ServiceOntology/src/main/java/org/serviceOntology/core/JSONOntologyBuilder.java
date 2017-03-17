package org.serviceOntology.core;

import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONObject;

public class JSONOntologyBuilder extends OntologyBuilder{

	private JSONObject jsonObj;
	private HashMap<String, JSONObject> schemas = new HashMap<String, JSONObject>();
	public JSONOntologyBuilder(JSONObject jobj, String srvProviderName){
		this.jsonObj = jobj;
		sBase = "http://edgar.semanticweb.org/ontologies/"+srvProviderName+"/service";
	}
	
	public void parse(){
		Iterator<String> keySchemas;
		boolean isSchemaDup = false;
		String serviceName = jsonObj.getString("name");
		String serviceVer = jsonObj.getString("version");
		try{
			keySchemas =  jsonObj.getJSONObject("schemas").keys();
			
			while(keySchemas.hasNext()){
				
				String schemaKey = keySchemas.next();
				if(schemas.containsKey(serviceName+"."+serviceVer+"."+schemaKey)){
					isSchemaDup = true;
				}
				schemas.put(serviceName+"."+serviceVer+"."+schemaKey, jsonObj.getJSONObject("schemas").getJSONObject(schemaKey));
			}
		}catch(Exception e){
			System.out.println("ERRRRRRRRRR");
		}
		
		
		
		Iterator<String> keyUrl =  jsonObj.getJSONObject("resources").keys();
		while(keyUrl.hasNext()){
			String group = keyUrl.next();
			System.out.println(group+"------------------------");
			JSONObject methods;
			try{
				methods = jsonObj.getJSONObject("resources").getJSONObject(group).getJSONObject("methods");
			}catch(Exception e){
				System.out.println("!!!!!");
				continue;
			}
			Iterator<String> keyI = methods.keys();
			while(keyI.hasNext()){
				String key = keyI.next();
				System.out.println("Method:"+key);

				JSONObject mObj = methods.getJSONObject(key);
//				JsonReader jsonReader = Json.createReader(new StringReader("[]"));
//				jsonUtil.convert(jsonSchema, targetNameSpaceUri, wrapping, name)
				String wsdlSrvName = mObj.getString("id");
				String tns = mObj.getString("path");
				String httpMethod = mObj.getString("httpMethod");
				String desc = mObj.has("description")?mObj.getString("description"):null;
				JSONObject pObj = mObj.has("parameters")? mObj.getJSONObject("parameters"):null;
				
				JSONObject rObj = mObj.has("response")?mObj.getJSONObject("response"):null;
				if(rObj != null && rObj.has("$ref")){
					String result = rObj.getString("$ref");
					JSONObject resultObj = schemas.get(serviceName+"."+serviceVer+"."+result);
				}
			}
		}
		
		keyUrl =  jsonObj.getJSONObject("resources").keys();
		
		System.out.println("==================================================");
		keyUrl = null;
	}
}
