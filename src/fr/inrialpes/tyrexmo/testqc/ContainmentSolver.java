package fr.inrialpes.tyrexmo.testqc;

import com.hp.hpl.jena.query.Query;
// or ontology.OntModel
import com.hp.hpl.jena.rdf.model.Model;

public interface ContainmentSolver {

    public void warmup() throws ContainmentTestException;

    public boolean entailed( Query q1, Query q2 ) throws ContainmentTestException;
	
    public boolean entailedUnderSchema( Model schema, Query q1, Query q2 ) throws ContainmentTestException;

    // Provisory before schemas are really parsed from Jena
    public boolean entailedUnderSchema( String schema, Query q1, Query q2 ) throws ContainmentTestException;

    public void cleanup() throws ContainmentTestException;

}
