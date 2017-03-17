package org.serviceOntology.core;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public abstract class OntologyBuilder {
	static OWLOntologyManager sManager = OWLManager.createOWLOntologyManager();
	static OWLDataFactory sDataFactory = sManager.getOWLDataFactory();
	static OWLOntology sOntology ;
	static String sBase ;
	
	public abstract void parse();
	
	void build() throws OWLOntologyCreationException{
		sOntology = sManager.createOntology(IRI.create(sBase));
	}
	
	void genWSDL(){
		
	}
}
