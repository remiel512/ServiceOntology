package org.serviceOntology.core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.StreamDocumentTarget;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;


public class GetGoogle {
	static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	static OWLDataFactory dataFactory = manager.getOWLDataFactory();
	static String base = "http://edgar.semanticweb.org/ontologies/google/service";
	static String[][] allUrl = {{"https://developers.google.com/drive/v3/reference/","drive"},
		                        {"https://developers.google.com/gmail/api/v1/reference","gmail"},
		                        {"https://developers.google.com/+/domains/api/","googleplus"},
		                        {"https://developers.google.com/youtube/v3/docs/","youtube"},
		                        {"https://developers.google.com/google-apps/calendar/v3/reference/","calendar"},
		                        {"https://developers.google.com/ad-exchange/seller-rest/reference/v2.0/","seller-rest"}};
	
	public static void main(String[] args) throws IOException, OWLOntologyStorageException, OWLOntologyCreationException{
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		FileOutputStream resultFile = new FileOutputStream(new File("GoogleOntology.rdf"));
		OWLOntology ontology = manager.createOntology(IRI.create(base));
        
		for(String[] url : allUrl){
			String service =  url[1];
		
			System.out.println(service);
			Document doc = Jsoup.connect(url[0])
					  .userAgent("Mozilla")
					  .timeout(8000)
					  .get();
			Elements sections = doc.getElementsByTag("section");

	        PrefixManager pm = new DefaultPrefixManager(null, null, base);
	        
	        IRI rootElement = null;
//	        OWLOntology ontology = manager.createOntology(IRI.create(base));
	        
	        OWLNamedIndividual topClass = null;
			for(Element e : sections){
				Elements h2e = e.getElementsByTag("h2");
				
				if(h2e.text().indexOf("Resource types") >= 0){
					continue;
				}
				
				if("youtube".equals(service)){
					h2e = e.getElementsByTag("h3");
				}

				String[] objs = h2e.text().split("\\.");
				setSubClass(ontology,url[0],"Google",service);
				OWLNamedIndividual wClass = null;
				if(objs.length < 2){
					wClass = dataFactory.getOWLNamedIndividual(IRI.create(base+"#"+service+"."+objs[0]));
					
				}else{
					if(topClass == null)
						topClass = dataFactory.getOWLNamedIndividual(IRI.create(base+"#"+service+"."+objs[0]));
					wClass = dataFactory.getOWLNamedIndividual(IRI.create(base+"#"+service+"."+objs[0]+"."+objs[objs.length-1]));
					
					OWLObjectProperty hasObject = dataFactory.getOWLObjectProperty(IRI.create(base+"#"+"hasObject"));
					OWLObjectProperty isObjectOf = dataFactory.getOWLObjectProperty(IRI.create(base+"#"+"isObjectOf"));
					manager.applyChange(new AddAxiom(ontology, dataFactory.getOWLObjectPropertyAssertionAxiom(hasObject, topClass, wClass)));
					manager.applyChange(new AddAxiom(ontology, dataFactory.getOWLObjectPropertyAssertionAxiom(isObjectOf, wClass, topClass)));
					
				}
				
				Elements tbodys = e.getElementsByTag("tbody");
				
				for(Element tbody : tbodys){
					Elements trs = tbody.getElementsByTag("tr");
					OWLObjectProperty hasMethod = dataFactory.getOWLObjectProperty(IRI.create(base+"#"+"hasMethod"));
					OWLObjectProperty hasUri = dataFactory.getOWLObjectProperty(IRI.create(base+"#"+"hasUri"));
					OWLObjectProperty hasParam = dataFactory.getOWLObjectProperty(IRI.create(base+"#"+"hasParameter"));
					OWLObjectProperty hasResponse = dataFactory.getOWLObjectProperty(IRI.create(base+"#"+"hasResponse"));
					for(Element tr : trs){
						Elements tds = tr.getElementsByTag("td");
						if(tds.get(0).getElementsByTag("a").size() == 0){
							continue;
						}
						Element a = tds.get(0).getElementsByTag("a").get(0);
						OWLNamedIndividual innerClass = dataFactory.getOWLNamedIndividual(IRI.create(wClass.getIRI()+"."+a.text()));
						OWLAnnotation commentAnno = dataFactory.getOWLAnnotation(dataFactory.getRDFSComment(), dataFactory.getOWLLiteral(
								tds.get(2).text(), "en"));
						
						OWLAxiom ax = dataFactory.getOWLAnnotationAssertionAxiom(innerClass.getIRI(), commentAnno);
						manager.applyChange(new AddAxiom(ontology, ax));
//						OWLNamedIndividual uri = dataFactory.getOWLNamedIndividual(IRI.create(base+"#"+tds.get(1).getElementsByTag("code").get(0).text()));
//						manager.applyChange(new AddAxiom(ontology, dataFactory.getOWLObjectPropertyAssertionAxiom(hasUri, innerClass, uri)));
						
						OWLObjectPropertyAssertionAxiom assertion = dataFactory.getOWLObjectPropertyAssertionAxiom(hasMethod, wClass, innerClass);
						AddAxiom addAxiomChange = new AddAxiom(ontology, assertion);
						manager.applyChange(addAxiomChange);
						
						Document innerdoc = Jsoup.connect(a.attr("href"))
								  .userAgent("Mozilla")
								  .timeout(8000)
								  .get();
						
						Element request = innerdoc.getElementById("request");
						if(request.getElementsByTag("tbody").size() == 0)
							continue;
						Element parastbody = request.getElementsByTag("tbody").get(0);
						Elements subtrs = parastbody.getElementsByTag("tr");
						for(Element subtr : subtrs){
							if(subtr.text().indexOf("parameters") >= 0)
								continue;
							Elements subtds = subtr.getElementsByTag("td");
							//TODO value and description will be added
							OWLNamedIndividual parm = dataFactory.getOWLNamedIndividual(IRI.create(innerClass.getIRI()+"."+subtds.get(0).text()));
							if(subtds.size() == 3){
								OWLAnnotation parmAnno = dataFactory.getOWLAnnotation(dataFactory.getRDFSComment(), dataFactory.getOWLLiteral(
										subtds.get(2).text(), "en"));

								manager.applyChange(new AddAxiom(ontology, dataFactory.getOWLAnnotationAssertionAxiom(parm.getIRI(), parmAnno)));
								
								manager.applyChange(new AddAxiom(ontology,dataFactory.getOWLDataPropertyAssertionAxiom(
										dataFactory.getOWLDataProperty(IRI.create(base+"#parameterType")), parm, subtds.get(1).text())));
								
								manager.applyChange(new AddAxiom(ontology, dataFactory.getOWLObjectPropertyAssertionAxiom(hasParam, innerClass, parm)));
							}
						}
						
//						Elements sublis = request.getElementsByTag("li");
						OWLDataProperty dp = dataFactory.getOWLDataProperty(IRI.create(base+"#httpRequestURL"));
//						for(Element li : sublis){
//							
//							OWLAnnotation parmAnno = dataFactory.getOWLAnnotation(dataFactory.getRDFSComment(), dataFactory.getOWLLiteral(
//									li.text(), "en"));
//							manager.applyChange(new AddAxiom(ontology, dataFactory.getOWLAnnotationAssertionAxiom(dp.getIRI(), parmAnno)));
//							
//							
//						}
						
						Elements pres = request.getElementsByTag("pre");
						for(Element pre : pres){
							manager.applyChange(new AddAxiom(ontology,dataFactory.getOWLDataPropertyAssertionAxiom(
									dp, innerClass, pre.text())));
						}
						
						Element response = innerdoc.getElementById("response");
//						System.out.println(response.text());
						OWLNamedIndividual responseObj = dataFactory.getOWLNamedIndividual(IRI.create(innerClass.getIRI()+".responseObject"));
						if(response.getElementsByTag("pre").size() > 0){
							Element jsonDesp = response.getElementsByTag("pre").get(0);
//							System.out.println(jsonDesp.text());
							OWLAnnotation parmAnno = dataFactory.getOWLAnnotation(dataFactory.getRDFSComment(), dataFactory.getOWLLiteral(
									jsonDesp.text(), "en"));

							manager.applyChange(new AddAxiom(ontology, dataFactory.getOWLAnnotationAssertionAxiom(responseObj.getIRI(), parmAnno)));
						}
						
						if(response.getElementsByTag("tbody").size() > 0){
							Element responseTbody = response.getElementsByTag("tbody").get(0);
							Elements restrs = responseTbody.getElementsByTag("tr");
							for(Element subtr : restrs){
								if(subtr.text().indexOf("parameters") >= 0)
									continue;
								Elements subtds = subtr.getElementsByTag("td");
//								System.out.println(subtds.get(0).text());
								OWLNamedIndividual parm = dataFactory.getOWLNamedIndividual(IRI.create(responseObj.getIRI()+"."+subtds.get(0).text()));
								if(subtds.size() == 3){
									OWLAnnotation parmAnno = dataFactory.getOWLAnnotation(dataFactory.getRDFSComment(), dataFactory.getOWLLiteral(
											subtds.get(2).text(), "en"));

									manager.applyChange(new AddAxiom(ontology, dataFactory.getOWLAnnotationAssertionAxiom(parm.getIRI(), parmAnno)));
									
									manager.applyChange(new AddAxiom(ontology,dataFactory.getOWLDataPropertyAssertionAxiom(
											dataFactory.getOWLDataProperty(IRI.create(base+"#parameterType")), parm, subtds.get(1).text())));
									
									manager.applyChange(new AddAxiom(ontology, dataFactory.getOWLObjectPropertyAssertionAxiom(hasParam, responseObj, parm)));
								}
							}
						}
						
						
						if(response.getElementsByTag("a").size() > 0){
							Element res_a = response.getElementsByTag("a").get(0);
							
							Document innerinnerdoc = Jsoup.connect(res_a.attr("href"))
									  .userAgent("Mozilla")
									  .timeout(8000)
									  .get();
							
							Element resource = innerinnerdoc.getElementById("resource");
//							System.out.println(resource.text());
							if(resource.getElementsByTag("pre").size() > 0){
								Element jsonDesp = resource.getElementsByTag("pre").get(0);
//								System.out.println(jsonDesp.text());
								OWLAnnotation parmAnno = dataFactory.getOWLAnnotation(dataFactory.getRDFSComment(), dataFactory.getOWLLiteral(
										jsonDesp.text(), "en"));

								manager.applyChange(new AddAxiom(ontology, dataFactory.getOWLAnnotationAssertionAxiom(responseObj.getIRI(), parmAnno)));
							}
							
							if(resource.getElementsByTag("tbody").size() > 0){
								Element resourceTbody = resource.getElementsByTag("tbody").get(0);
								Elements reotrs = resourceTbody.getElementsByTag("tr");
								for(Element subtr : reotrs){
									if(subtr.text().indexOf("parameters") >= 0)
										continue;
									Elements subtds = subtr.getElementsByTag("td");

									OWLNamedIndividual parm = dataFactory.getOWLNamedIndividual(IRI.create(responseObj.getIRI()+"."+subtds.get(0).text()));
									if(subtds.size() >= 3){
										OWLAnnotation parmAnno = dataFactory.getOWLAnnotation(dataFactory.getRDFSComment(), dataFactory.getOWLLiteral(
												subtds.get(2).text(), "en"));

										manager.applyChange(new AddAxiom(ontology, dataFactory.getOWLAnnotationAssertionAxiom(parm.getIRI(), parmAnno)));
										
										manager.applyChange(new AddAxiom(ontology,dataFactory.getOWLDataPropertyAssertionAxiom(
												dataFactory.getOWLDataProperty(IRI.create(base+"#parameterType")), parm, subtds.get(1).text())));
										
										manager.applyChange(new AddAxiom(ontology, dataFactory.getOWLObjectPropertyAssertionAxiom(hasParam, responseObj, parm)));
									}
								}
							}
							
						}
						
						manager.applyChange(new AddAxiom(ontology, dataFactory.getOWLObjectPropertyAssertionAxiom(hasResponse, innerClass, responseObj)));
						
						
						
					}
					
				}
				System.out.println("----------");
//				OWLNamedIndividual subInd = dataFactory.getOWLNamedIndividual("#"+objs[1], pm);

		        
//		        OWLClassAssertionAxiom classAssertion = dataFactory.getOWLClassAssertionAxiom(rootClass, subInd);

		        
			}
			
		
		}
		manager.saveOntology(ontology, new StreamDocumentTarget(result));
		resultFile.write(result.toByteArray());
		System.out.println("Success!!");
		
	}

	public static void setSubClass(OWLOntology ontology,String url, String... iriElements){
		OWLClass parentClass = null;
		for(String iriName : iriElements){
			OWLClass ontologyClass = dataFactory.getOWLClass(base+"#"+iriName);
			if(ontologyClass == null)
				ontologyClass = dataFactory.getOWLClass(IRI.create(base+"#"+iriName));
			
			if(parentClass != null){
				OWLAxiom axiom = dataFactory.getOWLSubClassOfAxiom(ontologyClass, parentClass);
				manager.addAxiom(ontology, axiom);
				
				OWLAnnotation parmAnno = dataFactory.getOWLAnnotation(dataFactory.getRDFSComment(), dataFactory.getOWLLiteral(
						url, "en"));
				manager.applyChange(new AddAxiom(ontology, dataFactory.getOWLAnnotationAssertionAxiom(ontologyClass.getIRI(), parmAnno)));
			}
			parentClass = ontologyClass;
		}
		
	}
}
