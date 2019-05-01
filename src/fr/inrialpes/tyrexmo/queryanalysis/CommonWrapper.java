package fr.inrialpes.tyrexmo.queryanalysis;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.hp.hpl.jena.query.Query;

public class CommonWrapper {
	
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
     *  check if the left and right-hand side queries
     *  have the same number and type of distinguished
     *  variables.
     * 
     * JE: It seems to check if variables have the same NAME!
     * which is wrong, their arity should be checked (what about "*"?).
     */
    protected boolean haveSameDistVar( Query leftQuery, Query rightQuery ) {
	List <String> rightQueryDistVars = rightQuery.getResultVars(); 
	Collections.sort( rightQueryDistVars );
	List <String> leftQueryDistVars = leftQuery.getResultVars();
	Collections.sort( leftQueryDistVars );
	return !rightQueryDistVars.equals( leftQueryDistVars );	
	//		return !leftQuery.getResultVars().equals(rightQuery.getResultVars());
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

}

