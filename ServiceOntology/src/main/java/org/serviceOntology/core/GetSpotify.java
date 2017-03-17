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

public class GetSpotify {
	static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	static OWLDataFactory dataFactory = manager.getOWLDataFactory();
	static String base = "http://edgar.semanticweb.org/ontologies/spotify/service";
	static String[][] allUrl = {{"https://developer.spotify.com/web-api/endpoint-reference/","spotifyWebApi"} };
	
	public static void main(String[] args) throws IOException, OWLOntologyStorageException, OWLOntologyCreationException{
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		FileOutputStream resultFile = new FileOutputStream(new File("SpotifyOntology.rdf"));
		OWLOntology ontology = manager.createOntology(IRI.create(base));
        
		for(String[] url : allUrl){
			String service =  url[1];
		
			System.out.println(service);
			Document doc = Jsoup.connect(url[0])
					  .userAgent("Mozilla")
					  .timeout(8000)
					  .get();
			Elements submenus = doc.getElementsByClass("sub-menu");

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
				Elements lis = e.getElementsByTag("li");
				setSubClass(ontology,url[0],"Spotify",service);
				
				
				for(Element li : lis){
					Elements alist = li.getElementsByTag("a");				
					if(alist.text().indexOf("API Endpoint Reference") >= 0){
						Element submenu = li.getElementsByClass("sub-menu").get(0);
						for(Node lid : submenu.childNodes()){
							
							if(lid.childNodeSize() == 0)
								continue;
							
							OWLNamedIndividual wClass = null;
							wClass = dataFactory.getOWLNamedIndividual(IRI.create(base+"#"+service+"."+lid.childNode(0).childNode(0)));
	
							
							//inner link
							System.out.println(lid.childNode(0).attr("href"));
							Document innerdoc = Jsoup.connect(lid.childNode(0).attr("href"))
									  .userAgent("Mozilla")
									  .timeout(8000)
									  .get();

							Element request = innerdoc.getElementsByClass("page_content").get(0);
							OWLAnnotation parmAnno = dataFactory.getOWLAnnotation(dataFactory.getRDFSComment(), dataFactory.getOWLLiteral(
									request.getElementsByTag("p").get(0).text(), "en"));

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
							
							
						}
					}
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
