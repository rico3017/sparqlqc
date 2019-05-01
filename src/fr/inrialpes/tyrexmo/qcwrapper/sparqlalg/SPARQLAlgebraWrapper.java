package fr.inrialpes.tyrexmo.qcwrapper.sparqlalg;

import fr.inrialpes.tyrexmo.queryanalysis.CommonWrapper;
import fr.inrialpes.tyrexmo.queryanalysis.TransformAlgebra;

import fr.inrialpes.tyrexmo.testqc.ContainmentSolver;
import fr.inrialpes.tyrexmo.testqc.ContainmentTestException;

import amod.PropertyTester;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.rdf.model.Model;

public class SPARQLAlgebraWrapper extends CommonWrapper implements ContainmentSolver {

    public void warmup() {};

    /**
     * Developed based on the model of amod.PropertyTester
     */
    public boolean entailed( Query q1, Query q2 ) throws ContainmentTestException {
	if ( supportedTest( q1, q2 ) ) {
	    PropertyTester solver = new PropertyTester();
	    return solver.isContained( Algebra.compile(q1), Algebra.compile(q2) );
	} else {
	    throw new ContainmentTestException( "Cannot deal with such a test" );
	}
    }

    public boolean entailedUnderSchema( Model schema, Query q1, Query q2 ) throws ContainmentTestException {
	throw new ContainmentTestException( "Cannot deal with schema" );
    };

    public boolean entailedUnderSchema( String schema, Query q1, Query q2 ) throws ContainmentTestException {
	throw new ContainmentTestException( "Cannot deal with schema" );
    };

    public void cleanup() {};

    private boolean supportedTest( Query q1, Query q2 ) {
	TransformAlgebra ta1 = new TransformAlgebra( q1 );
	TransformAlgebra ta2 = new TransformAlgebra( q2 );
	if ( containsOptional( ta1, ta2 ) || isValidQueryType( q1, q2 ) || isCyclic( ta1, ta2 ) )
	    return false;
	else 
	    return true;
    }
}
