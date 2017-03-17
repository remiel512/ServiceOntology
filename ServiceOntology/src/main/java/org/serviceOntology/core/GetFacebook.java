package org.serviceOntology.core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.StreamDocumentTarget;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
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

public class GetFacebook {
	static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	static OWLDataFactory dataFactory = manager.getOWLDataFactory();
	static String base = "http://edgar.semanticweb.org/ontologies/facebook/service";
	static String[][] allUrl = {{"https://developers.facebook.com/docs/graph-api/reference/","facebookgraphapi"} };
	
	public static void main(String[] args) throws IOException, OWLOntologyStorageException, OWLOntologyCreationException{
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		FileOutputStream resultFile = new FileOutputStream(new File("FacebookOntology.rdf"));
		OWLOntology ontology = manager.createOntology(IRI.create(base));
        
		for(String[] url : allUrl){
			String service =  url[1];
		
			System.out.println(service);
			Document doc = Jsoup.connect(url[0])
					  .userAgent("Mozilla")
					  .timeout(8000)
					  .get();
			Elements submenus = doc.getElementsByTag("tbody");

	        PrefixManager pm = new DefaultPrefixManager(null, null, base);
	        
	        IRI rootElement = null;
//	        OWLOntology ontology = manager.createOntology(IRI.create(base));
	        OWLObjectProperty hasMethod = dataFactory.getOWLObjectProperty(IRI.create(base+"#"+"hasMethod"));
			OWLObjectProperty hasUri = dataFactory.getOWLObjectProperty(IRI.create(base+"#"+"hasUri"));
			OWLObjectProperty hasParam = dataFactory.getOWLObjectProperty(IRI.create(base+"#"+"hasParameter"));
			OWLObjectProperty hasResponse = dataFactory.getOWLObjectProperty(IRI.create(base+"#"+"hasResponse"));
	        OWLDataProperty parameterType = dataFactory.getOWLDataProperty(IRI.create(base+"#parameterType"));
	        OWLDataProperty httpRequestURL = dataFactory.getOWLDataProperty(IRI.create(base+"#httpRequestURL"));

			for(Element e : submenus){
				Elements trs = e.getElementsByTag("tr");
				setSubClass(ontology,url[0],"Facebook",service);

				for(Element tr : trs){
					Elements tdlist = tr.getElementsByTag("td");				
					Node td1 = tdlist.get(0);
					Node td2 = tdlist.get(1);
					String objectUri = td1.childNode(0).attr("href");
					System.out.println(objectUri);

					OWLNamedIndividual wClass = null;
					wClass = dataFactory.getOWLNamedIndividual(IRI.create(base+"#"+service+"."+td1.childNode(0).childNode(0).childNode(0)));
					OWLAnnotation parmAnno = dataFactory.getOWLAnnotation(dataFactory.getRDFSComment(), dataFactory.getOWLLiteral(
							td2.childNode(0).childNode(0).toString(), "en"));
					manager.applyChange(new AddAxiom(ontology, dataFactory.getOWLAnnotationAssertionAxiom(wClass.getIRI(), parmAnno)));
					
					Document innerdoc = Jsoup.connect("https://developers.facebook.com"+objectUri)
							  .userAgent("Mozilla")
							  .timeout(8000)
							  .get();
					
					Element reading = innerdoc.getElementById("Reading");//reading
					
					if(reading == null)
						reading = innerdoc.getElementById("read");//reading
					
					if(reading == null){
						System.out.println("no reading element");
						continue;
					}
					
					if(reading.parent().getElementsByTag("tbody").size() == 0){
						System.out.println("no reading paramater list");
						continue;
					}
					
					Element tbody = reading.parent().getElementsByTag("tbody").get(0);
					OWLNamedIndividual innerClass = dataFactory.getOWLNamedIndividual(IRI.create(wClass.getIRI()+".get"+td1.childNode(0).childNode(0).childNode(0)));
					
					OWLObjectPropertyAssertionAxiom assertion = dataFactory.getOWLObjectPropertyAssertionAxiom(hasMethod, wClass, innerClass);
					AddAxiom addAxiomChange = new AddAxiom(ontology, assertion);
					manager.applyChange(addAxiomChange);
					for(Element innertr : tbody.getElementsByTag("tr")){
						
						OWLNamedIndividual parm = dataFactory.getOWLNamedIndividual(IRI.create(innerClass.getIRI()+"."+innertr.getElementsByTag("td").get(0).text()));
						OWLAnnotation innerparmAnno = dataFactory.getOWLAnnotation(dataFactory.getRDFSComment(), dataFactory.getOWLLiteral(
								innertr.getElementsByTag("td").get(1).text(), "en"));
						
						manager.applyChange(new AddAxiom(ontology, dataFactory.getOWLAnnotationAssertionAxiom(parm.getIRI(), innerparmAnno)));

						manager.applyChange(new AddAxiom(ontology, dataFactory.getOWLObjectPropertyAssertionAxiom(hasParam, innerClass, parm)));

						
					}
					/*
					Element submenu = td.getElementsByTag("code").get(0);
					for(Node lid : submenu.childNodes()){
						
						if(lid.childNodeSize() == 0)
							continue;
						
						
						
						//inner link
						System.out.println(lid.childNode(0).attr("href"));
						Document innerdoc = Jsoup.connect(lid.childNode(0).attr("href"))
								  .userAgent("Mozilla")
								  .timeout(8000)
								  .get();

						Element request = innerdoc.getElementsByClass("page_content").get(0);
						

						manager.applyChange(new AddAxiom(ontology, dataFactory.getOWLAnnotationAssertionAxiom(wClass.getIRI(), parmAnno)));

//							if(request.getElementsByTag("code").size() > 0)
//								System.out.println(request.getElementsByTag("code").get(0).text());
						
						if(request.getElementsByTag("pre").size() == 0){
							Element tbody = request.getElementsByTag("tbody").get(0);
							for(Element tr : tbody.getElementsByTag("tr")){
								OWLNamedIndividual innerClass = dataFactory.getOWLNamedIndividual(IRI.create(wClass.getIRI()+"."+tr.getElementsByTag("td").get(2).text().replaceAll("'", "").replaceAll(" ", "_")));
								
								OWLObjectPropertyAssertionAxiom assertion = dataFactory.getOWLObjectPropertyAssertionAxiom(hasMethod, wClass, innerClass);
								AddAxiom addAxiomChange = new AddAxiom(ontology, assertion);
								manager.applyChange(addAxiomChange);

								
								System.out.println(tr.getElementsByTag("td").get(3).text());//TODO return object
								System.out.println(tr.getElementsByTag("td").get(4).text());//TODO oauth property
								String innerUrl = tr.getElementsByTag("td").get(1).child(0).attr("href");
								if(innerUrl.indexOf("https") < 0)
									innerUrl = "https://developer.spotify.com"+tr.getElementsByTag("td").get(1).child(0).attr("href");
								Document indoc = Jsoup.connect(innerUrl)
										  .userAgent("Mozilla")
										  .timeout(8000)
										  .get();
								
								Element innerReq = indoc.getElementsByClass("page_content").get(0);
								//description
								OWLAnnotation commentAnno = dataFactory.getOWLAnnotation(dataFactory.getRDFSComment(), dataFactory.getOWLLiteral(
										innerReq.getElementsByTag("p").get(0).text(), "en"));
								
								OWLAxiom ax = dataFactory.getOWLAnnotationAssertionAxiom(innerClass.getIRI(), commentAnno);
								manager.applyChange(new AddAxiom(ontology, ax));
								if(innerReq.getElementsByTag("pre").size() > 0){
									manager.applyChange(new AddAxiom(ontology,dataFactory.getOWLDataPropertyAssertionAxiom(
											httpRequestURL, innerClass, innerReq.getElementsByTag("pre").get(0).text())));
								}
								
								Element innertbody = innerReq.getElementsByTag("tbody").get(0);
								for(Element innertr : innertbody.getElementsByTag("tr")){
									OWLNamedIndividual parm = dataFactory.getOWLNamedIndividual(IRI.create(innerClass.getIRI()+"."+innertr.getElementsByTag("td").get(0).text()));
									OWLAnnotation innerparmAnno = dataFactory.getOWLAnnotation(dataFactory.getRDFSComment(), dataFactory.getOWLLiteral(
											innertr.getElementsByTag("td").get(1).text(), "en"));
									
									manager.applyChange(new AddAxiom(ontology, dataFactory.getOWLAnnotationAssertionAxiom(parm.getIRI(), innerparmAnno)));

									manager.applyChange(new AddAxiom(ontology, dataFactory.getOWLObjectPropertyAssertionAxiom(hasParam, innerClass, parm)));

									//parameters

								}
								//TODO response object
							}
						}else{
							Element innertbody = request.getElementsByTag("tbody").get(0);
							for(Element innertr : innertbody.getElementsByTag("tr")){
								OWLNamedIndividual parm = dataFactory.getOWLNamedIndividual(IRI.create(wClass.getIRI()+"."+innertr.getElementsByTag("td").get(0).text()));
								OWLAnnotation innerparmAnno = dataFactory.getOWLAnnotation(dataFactory.getRDFSComment(), dataFactory.getOWLLiteral(
										innertr.getElementsByTag("td").get(1).text(), "en"));
								
								manager.applyChange(new AddAxiom(ontology, dataFactory.getOWLAnnotationAssertionAxiom(parm.getIRI(), innerparmAnno)));

								manager.applyChange(new AddAxiom(ontology, dataFactory.getOWLObjectPropertyAssertionAxiom(hasParam, wClass, parm)));
							}
							//TODO response object
						}
					}*/
					
				}
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
