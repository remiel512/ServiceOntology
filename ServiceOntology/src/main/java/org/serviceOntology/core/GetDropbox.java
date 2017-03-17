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

public class GetDropbox {
	static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	static OWLDataFactory dataFactory = manager.getOWLDataFactory();
	static String base = "http://edgar.semanticweb.org/ontologies/dropbox/service";
	static String[][] allUrl = {{"https://www.dropbox.com/developers/documentation/http/documentation/","dropboxApi"} };
	
	public static void main(String[] args) throws IOException, OWLOntologyStorageException, OWLOntologyCreationException{
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		FileOutputStream resultFile = new FileOutputStream(new File("DropboxOntology.rdf"));
		OWLOntology ontology = manager.createOntology(IRI.create(base));
        
		for(String[] url : allUrl){
			String service =  url[1];
		
			System.out.println(service);
			Document doc = Jsoup.connect(url[0])
					  .userAgent("Mozilla")
					  .timeout(8000)
					  .get();
			
			Elements h3List = doc.getElementsByTag("h3");
			Elements dlList = doc.getElementsByTag("dl");
			
			

	        PrefixManager pm = new DefaultPrefixManager(null, null, base);
	        
	        IRI rootElement = null;
//	        OWLOntology ontology = manager.createOntology(IRI.create(base));
	        OWLObjectProperty hasMethod = dataFactory.getOWLObjectProperty(IRI.create(base+"#"+"hasMethod"));
			OWLObjectProperty hasUri = dataFactory.getOWLObjectProperty(IRI.create(base+"#"+"hasUri"));
			OWLObjectProperty hasParam = dataFactory.getOWLObjectProperty(IRI.create(base+"#"+"hasParameter"));
			OWLObjectProperty hasResponse = dataFactory.getOWLObjectProperty(IRI.create(base+"#"+"hasResponse"));
	        OWLDataProperty parameterType = dataFactory.getOWLDataProperty(IRI.create(base+"#parameterType"));
	        OWLDataProperty httpRequestURL = dataFactory.getOWLDataProperty(IRI.create(base+"#httpRequestURL"));
	        int h3id = 0;
			for(Element e : h3List){
				String serviceNmae = e.attr("id");
				setSubClass(ontology,url[0],"Dropbox",service);
				System.out.println(serviceNmae);
				OWLNamedIndividual wClass = null;
				wClass = dataFactory.getOWLNamedIndividual(IRI.create(base+"#"+service+"."+serviceNmae));
				Elements dtList = dlList.get(h3id).getElementsByTag("dt");
				Elements ddList = dlList.get(h3id).getElementsByTag("dd");
				int dtid = 0;
				for(Element dt : dtList){
				    System.out.println(dt.text());
					if(dt.text().indexOf("Description") >= 0){
						OWLAnnotation parmAnno = dataFactory.getOWLAnnotation(dataFactory.getRDFSComment(), dataFactory.getOWLLiteral(
								ddList.get(dtid).text(), "en"));

						manager.applyChange(new AddAxiom(ontology, dataFactory.getOWLAnnotationAssertionAxiom(wClass.getIRI(), parmAnno)));
					}
					

					if(dt.text().indexOf("Parameters") >= 0){
						Elements bList = ddList.get(dtid).getElementsByTag("b");

						for(int i = 0 ; i < bList.size() ;i++){
							OWLNamedIndividual innerClass = dataFactory.getOWLNamedIndividual(IRI.create(wClass.getIRI()+"."+bList.get(i).childNode(0).childNode(0)));
							
							OWLObjectPropertyAssertionAxiom assertion = dataFactory.getOWLObjectPropertyAssertionAxiom(hasParam, wClass, innerClass);
							AddAxiom addAxiomChange = new AddAxiom(ontology, assertion);
							manager.applyChange(addAxiomChange);
							
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
