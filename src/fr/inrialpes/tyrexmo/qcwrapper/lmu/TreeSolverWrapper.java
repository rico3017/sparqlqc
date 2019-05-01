package fr.inrialpes.tyrexmo.qcwrapper.lmu;

import fr.inrialpes.wam.treelogic.BottomUpSolver.FormulaSolver;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inrialpes.tyrexmo.testqc.ContainmentSolver;
import fr.inrialpes.tyrexmo.testqc.ContainmentTestException;

import fr.inrialpes.tyrexmo.queryanalysis.TransformAlgebra;
import fr.inrialpes.tyrexmo.queryanalysis.CyclicQuery;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.rdf.model.Model;

public class TreeSolverWrapper implements ContainmentSolver {
    final static Logger logger = LoggerFactory.getLogger( TreeSolverWrapper.class );

    /*kripke restriction \varphi_r */
    // JE: THIS FORMULA IS THE ONE FOR AFMU (but this was the original in Mel's program)
    private static String theta = "<-s>spr & <p>spr & <o>spr & !<s>true & !<-p>true & !<-o>true";
    private static String eta = "!<-s>true & !<o>true & !<p>true & !<d>true & !<-d>true & !<s>spr & !<-p>spr & !<-o>spr";
    private static String kappa = "[-s]("+eta+") & [p]("+eta+") & [o]("+eta+")";	    
    private final static String phiR = "(let x1 = nu "+theta+" & "+kappa+" & (!<d>true | <d>x1) in x1 end)";

    private FormulaSolver fs = null;
    
    // This is test #1
    private String WarmupFormula = "((let $v1 =  (_varx & <1>(_takesCourse & <2>_Course10))  | <1>$v1 | <2>$v1 in $v1) & (let $v0 =  (_varx & <1>(_takesCourse & <2>_Course20))  | <1>$v0 | <2>$v0 in $v0)) & ~((let $v2 =  (_varx & <1>(_takesCourse & <2>_Course10))  | <1>$v2 | <2>$v2 in $v2))";
    public void warmup() {
	evaluateFormula( WarmupFormula );
    };

    public boolean entailed( Query q1, Query q2 ) throws ContainmentTestException {
	return entailedUnderSchema( (String)null, q1, q2 );
    }

    public boolean entailedUnderSchema( Model schema, Query q1, Query q2 ) throws ContainmentTestException {
	throw new ContainmentTestException( "Cannot yet parse Jena Models" );
    };

    public boolean entailedUnderSchema( String schema, Query q1, Query q2 ) throws ContainmentTestException {
	//long start = System.currentTimeMillis();
	TransformAlgebra ta1 = new TransformAlgebra( q1 );
	TransformAlgebra ta2 = new TransformAlgebra( q2 );
	// Encode formula
	String formulaLeft, formulaRight;
	if ( supportedTest( q1, ta1, q2, ta2 ) ) {
	    if ( useSameEncoding( q1, q2 ) ){
		formulaLeft = new EncodeLHSQuery( ta1 ).getFormula();
		//System.out.println(formulaLeft);
		// JE : ??????? Fortunately, given the coding of useSameEncoding we barely go here!
		formulaRight = new EncodeLHSQuery( ta1 ).getFormula();
		//System.out.println(formulaRight);
	    } else {
		formulaLeft = new EncodeLHSQuery( ta1 ).getFormula();
		//System.out.println(formulaLeft);
		formulaRight = new EncodeRHSQuery( ta2 ).getFormula();
		//System.out.println(formulaRight);
	    }
	} else {
	    throw new ContainmentTestException( "Cannot deal with such a test" );
	}
	// Create checker
	//XPathContainment pc = new XPathContainment();
	// Test it
	String formula = "(" + formulaLeft + ") & ~("+ formulaRight + ")";
	if ( schema != null ) {
	    formula += " & "+new AxiomEncoder().encodeSchema( schema );
	}
	ta1 = null; ta2 = null; // free memory
	formulaLeft = null; formulaRight = null; // free memory
	//long end = System.currentTimeMillis();
	//System.err.println( "Ended encoding formulas ["+(end-start)+"ms]");
	// This is useless because the Lmu solver only look for tree-shaped models
	//String formula = phiR +"& (" + formulaLeft + ") & ~("+ formulaRight + ")";
	//logger.debug( "Encoded formula: {}", formula );
	//start = System.currentTimeMillis();
	//return !pc.checkSat( formula );
	int res = evaluateFormula( formula );
	//logger.debug( "Here is the result returned : {}", res );
	//end = System.currentTimeMillis();
	//System.err.println( "Ended testing containment ["+(end-start)+"ms]");
	if ( res ==  FormulaSolver.ERROR ) throw new ContainmentTestException( "Solver error" );
	else return (res==FormulaSolver.UNSATISFIABLE);
    };
    
    protected int evaluateFormula( String formula ) {
	fs = new FormulaSolver();
	return fs.solve_formula_int_result( formula, false, false, false, false, false, false, null );
    }

    public void cleanup() {
	fs = null;
	System.gc();
    };

    private boolean supportedTest( Query q1, TransformAlgebra ta1, Query q2, TransformAlgebra ta2 ) {
	if ( containsOptional( ta1, ta2 ) || isValidQueryType( q1, q2 ) || isCyclic( ta1, ta2 ) || haveSameDistVar( q1, q2 ) )
	    return false;
	else 
	    return true;
    }

    protected boolean containsOptional( TransformAlgebra left, TransformAlgebra right ) {
	return (left.containsOpt() || right.containsOpt());
    }

	//TODO: add same number and type of variables
	/**
	 *  same left-hand and right-hand side query encoding
	 */
    protected boolean useSameEncoding( Query leftQuery, Query rightQuery ) {
	return leftQuery.equals(rightQuery);
    }
	/**
	 * restrict query types to SELCET and ASK
	 */
    protected boolean isValidQueryType( Query leftQuery, Query rightQuery ) {
	return (leftQuery.isConstructType() || rightQuery.isConstructType() || 
		leftQuery.isDescribeType() || rightQuery.isDescribeType());
    }
	/**
	 * check if there is a cycle in the queries among the non-distinguished 
	 * variables
	 * 
	 * @return
	 */
    protected boolean isCyclic( TransformAlgebra left, TransformAlgebra right ) {
	CyclicQuery l = new CyclicQuery( left.getTriples() );
	CyclicQuery r = new CyclicQuery( right.getTriples() );
	if (l.isCyclic() || r.isCyclic())
	    return true;
	else {
	    // JE: I love useful tests as this one
	    if (l.checkIfaDAGisTree() && r.checkIfaDAGisTree())
		return false;
	    else 
		return false;
	}
    }
	/**
	 *  check if the left and right-hand side queries
	 *  have the same number and type of distinguished
	 *  variables.
	 */
    protected boolean haveSameDistVar( Query leftQuery, Query rightQuery ) {
	List <String> rightQueryDistVars = rightQuery.getResultVars(); 
	Collections.sort( rightQueryDistVars );
	List <String> leftQueryDistVars = leftQuery.getResultVars();
	Collections.sort( leftQueryDistVars );
	return !rightQueryDistVars.equals( leftQueryDistVars );	
    }

}


