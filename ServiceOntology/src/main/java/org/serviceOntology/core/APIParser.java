package org.serviceOntology.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.wsdl.WSDLException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.ethlo.schematools.jsons2xsd.Jsons2Xsd;

public class APIParser {

	public void parse() throws IOException{
		Document doc = Jsoup.connect("https://www.googleapis.com/discovery/v1/apis")
				  .userAgent("Mozilla")
				  .timeout(8000)
				  .ignoreContentType(true)
				  .get();
		
		JSONObject obj = new JSONObject(doc.body().text());
		JSONArray objarray = obj.getJSONArray("items");
		//System.out.println(objarray.length());
		HashMap<String, JSONObject> schemas = new HashMap<String, JSONObject>();
		Jsons2Xsd jsonUtil = new Jsons2Xsd();
		boolean isSchemaDup = false;
		int totMethodCount = 0;
		//for (int i = 0; i < objarray.length(); i++){
		for (int i = 0; i < 1; i++){
			//String serviceName = objarray.getJSONObject(i).getString("name");
			//String serviceVer = objarray.getJSONObject(i).getString("version");
			String serviceName = "drive";
			String serviceVer = "v2";
			System.out.println("Service Name:"+serviceName+", version:"+serviceVer);
			doc = Jsoup.connect("https://www.googleapis.com/discovery/v1/apis/"+serviceName+"/"+serviceVer+"/rest")
					  .userAgent("Mozilla")
					  .timeout(8000)
					  .ignoreContentType(true)
					  .get();
			
			JSONObject srvObj = new JSONObject(doc.body().text());
//			JSONObject methods = srvObj.getJSONObject("resources").getJSONObject("url").getJSONObject("methods");
			String baseurl = srvObj.getString("baseUrl");
			String owner = srvObj.getString("ownerName");
			JSONObject gPObj = srvObj.has("parameters")? srvObj.getJSONObject("parameters"):null;
			
			Iterator<String> keySchemas;
			try{
				keySchemas =  srvObj.getJSONObject("schemas").keys();
				
				while(keySchemas.hasNext()){
					String schemaKey = keySchemas.next();
					if(schemas.containsKey(serviceName+"."+serviceVer+"."+schemaKey)){
						isSchemaDup = true;
					}
					schemas.put(serviceName+"."+serviceVer+"."+schemaKey, srvObj.getJSONObject("schemas").getJSONObject(schemaKey));
				}
			}catch(Exception e){
				System.out.println("ERRRRRRRRRR");
			}
			
			
			
			Iterator<String> keyUrl =  srvObj.getJSONObject("resources").keys();
			while(keyUrl.hasNext()){
				String group = keyUrl.next();
				System.out.println(group+"------------------------");
				JSONObject methods;
				try{
					methods = srvObj.getJSONObject("resources").getJSONObject(group).getJSONObject("methods");
				}catch(Exception e){
					System.out.println("!!!!!");
					continue;
				}
				Iterator<String> keyI = methods.keys();
				while(keyI.hasNext()){
					Service s = new Service();
					String key = keyI.next();
					System.out.println("Method:"+key);
					totMethodCount++;
					JSONObject mObj = methods.getJSONObject(key);
					s.setOwner(owner);
//					JsonReader jsonReader = Json.createReader(new StringReader("[]"));
//					jsonUtil.convert(jsonSchema, targetNameSpaceUri, wrapping, name)
					s.setServiceName(owner+"."+mObj.getString("id")+".service");
					s.setXmlnsimpl(baseurl+mObj.getString("path"));
					s.setTargetNamespace(baseurl+mObj.getString("path"));
					s.setOperationName(owner+"."+mObj.getString("id"));
					s.setPortType(owner+"."+mObj.getString("id")+".port");
					s.setRequestName(owner+"."+mObj.getString("id")+".request");
					s.setRequestType(mObj.getString("httpMethod"));
					s.setResponseName(owner+"."+mObj.getString("id")+".response");
					
					if(gPObj != null){
						s.setgParamaterObject(gPObj);
					}

					JSONObject pObj = mObj.has("parameters")? mObj.getJSONObject("parameters"):null;
					if(pObj != null)
						s.setParamaterObject(pObj);
					JSONObject reqObj = mObj.has("request")?mObj.getJSONObject("request"):null;
					if(reqObj != null && reqObj.has("$ref")){
						String result = reqObj.getString("$ref");
						reqObj = schemas.get(serviceName+"."+serviceVer+"."+result).getJSONObject("properties");
						s.setRequestObject(reqObj);
					}
					
					JSONObject rObj = mObj.has("response")?mObj.getJSONObject("response"):null;
					if(rObj != null && rObj.has("$ref")){
						String result = rObj.getString("$ref");
						rObj = schemas.get(serviceName+"."+serviceVer+"."+result).getJSONObject("properties");
						s.setResponseObject(rObj);
					}
					
					
					try {
						s.generateWSDL();
					} catch (WSDLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
			keyUrl =  srvObj.getJSONObject("resources").keys();
			
			System.out.println("==================================================");
			doc = null;
			srvObj = null;
			keyUrl = null;
		}
		
		//System.out.println(isSchemaDup);
		
		//System.out.println(totMethodCount);
		
	}
	
	public List<Service> parse(String apiname) throws IOException{

		System.out.println("apiname:"+apiname);
		HashMap<String, JSONObject> schemas = new HashMap<String, JSONObject>();
		Jsons2Xsd jsonUtil = new Jsons2Xsd();
		boolean isSchemaDup = false;
		int totMethodCount = 0;
		//for (int i = 0; i < objarray.length(); i++){
		ArrayList<Service> returnList = new ArrayList<Service>();
		System.out.println(apiname.split("\\.").length);
		String serviceName = apiname.split("\\.")[1];
		String serviceVer = "v1";
		System.out.println("Service Name:"+serviceName+", version:"+serviceVer);
		Document doc = Jsoup.connect("https://www.googleapis.com/discovery/v1/apis/"+serviceName+"/"+serviceVer+"/rest")
					  .userAgent("Mozilla")
					  .timeout(8000)
					  .ignoreContentType(true)
					  .get();
			
			JSONObject srvObj = new JSONObject(doc.body().text());
//			JSONObject methods = srvObj.getJSONObject("resources").getJSONObject("url").getJSONObject("methods");
			String baseurl = srvObj.getString("baseUrl");
			String owner = srvObj.getString("ownerName");
			JSONObject gPObj = srvObj.has("parameters")? srvObj.getJSONObject("parameters"):null;
			
			Iterator<String> keySchemas;
			try{
				keySchemas =  srvObj.getJSONObject("schemas").keys();
				
				while(keySchemas.hasNext()){
					String schemaKey = keySchemas.next();
					if(schemas.containsKey(serviceName+"."+serviceVer+"."+schemaKey)){
						isSchemaDup = true;
					}
					schemas.put(serviceName+"."+serviceVer+"."+schemaKey, srvObj.getJSONObject("schemas").getJSONObject(schemaKey));
				}
			}catch(Exception e){
				System.out.println("ERRRRRRRRRR");
			}
			
			if(apiname.split("\\.").length>2){
				Service s = new Service();
				JSONObject methods = srvObj.getJSONObject("resources").getJSONObject(apiname.split(".")[2]).getJSONObject("methods");
				JSONObject mObj = methods.getJSONObject(apiname.split("\\.")[3]);
				s.setOwner(owner);
//				JsonReader jsonReader = Json.createReader(new StringReader("[]"));
//				jsonUtil.convert(jsonSchema, targetNameSpaceUri, wrapping, name)
				s.setServiceName(owner+"."+mObj.getString("id")+".service");
				s.setXmlnsimpl(baseurl+mObj.getString("path"));
				s.setTargetNamespace(baseurl+mObj.getString("path"));
				s.setOperationName(owner+"."+mObj.getString("id"));
				s.setPortType(owner+"."+mObj.getString("id")+".port");
				s.setRequestName(owner+"."+mObj.getString("id")+".request");
				s.setRequestType(mObj.getString("httpMethod"));
				s.setResponseName(owner+"."+mObj.getString("id")+".response");
				
				if(gPObj != null){
					s.setgParamaterObject(gPObj);
				}

				JSONObject pObj = mObj.has("parameters")? mObj.getJSONObject("parameters"):null;
				if(pObj != null)
					s.setParamaterObject(pObj);
				JSONObject reqObj = mObj.has("request")?mObj.getJSONObject("request"):null;
				if(reqObj != null && reqObj.has("$ref")){
					String result = reqObj.getString("$ref");
					reqObj = schemas.get(serviceName+"."+serviceVer+"."+result).getJSONObject("properties");
					s.setRequestObject(reqObj);
				}
				
				JSONObject rObj = mObj.has("response")?mObj.getJSONObject("response"):null;
				if(rObj != null && rObj.has("$ref")){
					String result = rObj.getString("$ref");
					rObj = schemas.get(serviceName+"."+serviceVer+"."+result).getJSONObject("properties");
					s.setResponseObject(rObj);
				}
				returnList.add(s);
			}else{
				Iterator<String> keyUrl =  srvObj.getJSONObject("resources").keys();
				while(keyUrl.hasNext()){
					String group = keyUrl.next();
					System.out.println(group+"------------------------");
					JSONObject methods;
					try{
						methods = srvObj.getJSONObject("resources").getJSONObject(group).getJSONObject("methods");
					}catch(Exception e){
						System.out.println("!!!!!");
						continue;
					}
	
					Iterator<String> keyI = methods.keys();
					while(keyI.hasNext()){
						Service s = new Service();
						String key = keyI.next();
						System.out.println("Method:"+key);
						totMethodCount++;
						JSONObject mObj = methods.getJSONObject(key);
						s.setOwner(owner);
	//						JsonReader jsonReader = Json.createReader(new StringReader("[]"));
	//						jsonUtil.convert(jsonSchema, targetNameSpaceUri, wrapping, name)
						s.setServiceName(owner+"."+mObj.getString("id")+".service");
						s.setXmlnsimpl(baseurl+mObj.getString("path"));
						s.setTargetNamespace(baseurl+mObj.getString("path"));
						s.setOperationName(owner+"."+mObj.getString("id"));
						s.setPortType(owner+"."+mObj.getString("id")+".port");
						s.setRequestName(owner+"."+mObj.getString("id")+".request");
						s.setRequestType(mObj.getString("httpMethod"));
						s.setResponseName(owner+"."+mObj.getString("id")+".response");
						
						if(gPObj != null){
							s.setgParamaterObject(gPObj);
						}
	
						JSONObject pObj = mObj.has("parameters")? mObj.getJSONObject("parameters"):null;
						if(pObj != null)
							s.setParamaterObject(pObj);
						JSONObject reqObj = mObj.has("request")?mObj.getJSONObject("request"):null;
						if(reqObj != null && reqObj.has("$ref")){
							String result = reqObj.getString("$ref");
							reqObj = schemas.get(serviceName+"."+serviceVer+"."+result).getJSONObject("properties");
							s.setRequestObject(reqObj);
						}
						
						JSONObject rObj = mObj.has("response")?mObj.getJSONObject("response"):null;
						if(rObj != null && rObj.has("$ref")){
							String result = rObj.getString("$ref");
							rObj = schemas.get(serviceName+"."+serviceVer+"."+result).getJSONObject("properties");
							s.setResponseObject(rObj);
						}
						returnList.add(s);
					}
				
				
					
					
//					try {
//						s.generateWSDL();
//					} catch (WSDLException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
				}
			}
			
			doc = null;
			srvObj = null;
			return returnList;
		
		
		//System.out.println(isSchemaDup);
		
		//System.out.println(totMethodCount);
		
	}
	
	public static void main(String [] args) throws IOException{
		new APIParser().parse();
	}
}
