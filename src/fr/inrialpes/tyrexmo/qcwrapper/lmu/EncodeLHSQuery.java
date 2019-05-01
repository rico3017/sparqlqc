package fr.inrialpes.tyrexmo.qcwrapper.lmu;

import java.util.Stack;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;

import fr.inrialpes.tyrexmo.queryanalysis.TransformAlgebra;

public class EncodeLHSQuery extends QueryToFormula {
	
	Stack<Object> triples;
	//TODO: transform left query triples into an arraylist
	public EncodeLHSQuery (String leftQuery) {
		TransformAlgebra algebra = new TransformAlgebra(leftQuery);
		triples = algebra.getQueryPattern();
	}

	public EncodeLHSQuery (Query leftQuery) {
		TransformAlgebra algebra = new TransformAlgebra(leftQuery);
		triples = algebra.getQueryPattern();
	}
	
	public EncodeLHSQuery ( TransformAlgebra algebra ) {
		triples = algebra.getQueryPattern();
	}
	
	public EncodeLHSQuery (Stack<Object> triples) {
		System.out.println(mathcalA(triples));
	}
	
	protected String getFormula () {
		return mathcalA(triples);
	}
	
	/**
	 *  the (subject, predicate, object) of a triple 
	 *  need syntactic formatting in order to match
	 *  the syntactic restrictions of the satisfiability 
	 *  solver.
	 */
	protected String formatSubject(Triple t) {
    	String s = "";
    	if (t.getSubject().isVariable()) 
			//s = t.getSubject().toString();  // remove question mark, in variables
    		s = "_var"+t.getSubject().getName().replace("?", "");;
    	if (t.getSubject().isURI())
			s = "_"+t.getSubject().getLocalName();
		if (t.getSubject().isBlank()){
			s = t.getSubject().getBlankNodeLabel();
			String alphabet = "abcdefghijklmnopqrstuvwxyz";
			int charc = (int)(Math.random()*26);
			String append = alphabet.substring(charc, charc+1);
			s = "_"+append + s;
		}
    	return s;
    }
    protected String formatPredicate(Triple t) {
    	String p ="";
    	if (t.getPredicate().isVariable())
			//p = t.getPredicate().toString();  //remove question mark
    		p = "_var"+t.getPredicate().getName();
		if(t.getPredicate().isURI()) 
			p = "_"+t.getPredicate().getLocalName();
    	return p;
    }
    protected String formatObject(Triple t) {
    	String o="";
    	if (t.getObject().isVariable())
			//o =  t.getObject().toString() ;  // remove question mark
//    		o = "var"+t.getObject().getName();
    		o = "_var"+t.getObject().getName().replace("?", "");
		if (t.getObject().isURI()) 
			o = "_"+t.getObject().getLocalName();
		if (t.getObject().isLiteral())
			o = "_"+t.getObject().getLiteralLexicalForm();
		if (t.getObject().isBlank()){
			o = t.getSubject().getBlankNodeLabel();
			String alphabet = "abcdefghijklmnopqrstuvwxyz";
			int charc = (int)(Math.random()*26);
			String append = alphabet.substring(charc, charc+1);
			o = "_"+append + o;
		}
    	return o;
    }
}
