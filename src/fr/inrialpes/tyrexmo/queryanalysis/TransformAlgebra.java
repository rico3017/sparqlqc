package fr.inrialpes.tyrexmo.queryanalysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.SortCondition;
import com.hp.hpl.jena.sparql.ARQNotImplemented;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpVisitor;
import com.hp.hpl.jena.sparql.algebra.op.*;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.core.VarExprList;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.syntax.*;

/** Convert an Op expression in SPARQL syntax, that is, the reverse of algebra generation */   
public class TransformAlgebra {	
    private Stack<Object> lst;
    private Query query;
    /*
    private boolean containsOpt;
    private boolean containsFilter;
    private boolean containsUn;
    */
    private Converter conv;
    
    public TransformAlgebra(Query q) {
	query = q;
    	Op op = Algebra.compile(query);
    	transformAlgebra(op);
    }
    public TransformAlgebra(String q) {
	query = QueryFactory.create(q);
    	Op op = Algebra.compile(query);
    	transformAlgebra(op);
    }
    private void transformAlgebra( Op op ) {
        Query query = QueryFactory.make() ;
        conv = new Converter(query) ;
        //OpWalker.walk(op, v) ;
        op.visit( conv ) ;       
        lst = conv.getQueryElems();
	/*
        containsOpt = v.containsOptional();
        containsFilter = v.containsFilter();
        containsUn = v.containsUnion();
	*/
    }
    public boolean containsOpt () { return conv.containsOptional(); }
    public boolean hasFilter () { return conv.containsFilter(); }
    public boolean hasUnion () { return conv.containsUnion(); }
    public Stack<Object> getQueryPattern() { return lst; }
    
    /********************************************************/
    // Distinguished variables
	/*public List<Var> getResultVars() {
		Query query = QueryFactory.create(this.query);
		//List<String> vars = query.getResultVars();
		VarExprList vars = new VarExprList() ;
		vars = query.getProject();
		
		return vars.getVars();
	}*/
    
    public List<Var> getResultVars() {
    	Query q = QueryFactory.create(query);
    	if ( q.isQueryResultStar() ) {
	    List<Var> list = new ArrayList<Var>();
	    list.addAll( q.getQueryPattern().varsMentioned() );
	    return list;     		
    	} else return QueryFactory.create(query).getProject().getVars();
    }
    /********************************************************/
    // Distinguished variable names
    public List<String> getProjectVars() {
	return QueryFactory.create(query).getResultVars();
    }
	
    // all the variables in the query
    public Set<Var> getVars() {
	Query q = QueryFactory.create(query);
	return q.getQueryPattern().varsMentioned();
    }
    // non-distinguished vars
    public Collection<Var> getNonDistVars() {
	return difference( getResultVars(), getVars() );
    }
	
    //set difference of distinguished vars and all the vars in a query
    private Collection<Var> difference(Collection<Var> dVars, Collection<Var> allVars) {
	Collection<Var> result = new ArrayList<Var>(allVars);
	result.removeAll(dVars);
	return result;
    }
    /**
     * triples that made up the query in a list
     * @return
     */
    public List<Triple> getTriples() {
	Object [] triples = lst.toArray();
	List<Triple> t = new ArrayList<Triple>();
	for ( int i = 0; i < lst.size(); i++ ) {
	    if (triples[i] instanceof Triple)
		t.add((Triple)triples[i]);
	}
	return t;
    }
    
    public static class Converter implements OpVisitor {
        private Query query;
        private ElementGroup currentGroup = null;
        private Stack<ElementGroup> stack = new Stack<ElementGroup>();
        private Stack<Object> elems = new Stack<Object>();
        private boolean containsOptional = false;
        private boolean containsFilter = false;
        private boolean containsUnion = false;

        public Converter(Query q) {
            query = q;
            currentGroup = new ElementGroup() ;
        }
        
        public Stack<Object> getQueryElems() {        	
        	return elems;	
         }
        
        Element asElement( Op op ) {
            ElementGroup g = asElementGroup(op) ;
            if ( g.getElements().size() == 1 )
                return g.getElements().get(0) ;
            return g ;
        }
        
        ElementGroup asElementGroup( Op op ) {
            startSubGroup() ;
            op.visit(this) ;
            return endSubGroup() ;
        }

        public void visit( OpBGP opBGP ) {
            currentGroup().addElement(process(opBGP.getPattern())) ;
            //System.out.println("ETB"+process(opBGP.getPattern()).toString());   
            //elems.add(process(opBGP.getPattern()));
        }

        public void visit(OpTriple opTriple) { 
	    currentGroup().addElement(process(opTriple.getTriple())) ; 
        	//System.out.println(opTriple.getTriple().toString());
        }

       
        private ElementTriplesBlock process(BasicPattern pattern) {
	    int twoAdded = pattern.size();
	    ElementTriplesBlock e = new ElementTriplesBlock() ;
	    for ( Triple t : pattern ) {
                // Leave bNode variables as they are
                // Query serialization will deal with them. 
                e.addTriple(t) ;
                // add each triple pattern into the stack
                elems.add(t); 
		twoAdded--;           
                if (twoAdded >= 1) elems.add("AND"); 
            }
            //elems.add(e);
            return e ;
        }
        
        private ElementTriplesBlock process(Triple triple)
        {
            // Unsubtle
            ElementTriplesBlock e = new ElementTriplesBlock() ;
            e.addTriple(triple) ;
            //System.out.println(triple.toString());
            elems.push(triple);
            return e ;
        }
        
        public void visit(OpQuadPattern quadPattern)
        { throw new ARQNotImplemented("OpQuadPattern") ; }

        public void visit(OpPath opPath)
        { throw new ARQNotImplemented("OpPath") ; }

        public void visit(OpJoin opJoin)
        {
            // Keep things clearly separated.
	    elems.add("(");
            Element eLeft = asElement(opJoin.getLeft()) ;
            elems.add("AND");
            Element eRight = asElementGroup(opJoin.getRight()) ;
            elems.add(")");
            
            ElementGroup g = currentGroup() ;
            g.addElement(eLeft) ;
            //elems.push(eLeft);
            //elems.add("AND");
            g.addElement(eRight) ;
            //elems.push(eRight);
            return ;
        }

        private static boolean emptyGroup(Element element)
        {
            if ( ! ( element instanceof ElementGroup ) )
                return false ;
            ElementGroup eg = (ElementGroup)element ;
            return eg.isEmpty() ;
        }
        
	/*************************************************************/
        public void visit( OpLeftJoin opLeftJoin ) {
	    containsOptional = true; // JE: to be discussed
            Element eLeft = asElement( opLeftJoin.getLeft() );
            ElementGroup eRight = asElementGroup( opLeftJoin.getRight() );
            /*if ( opLeftJoin.getExprs() != null ) {
                for ( Expr expr : opLeftJoin.getExprs() ) {
                    ElementFilter f = new ElementFilter(expr) ;
                    eRight.addElement(f) ;
                }
            }*/ 
            ElementGroup g = currentGroup() ;
            if ( !emptyGroup(eLeft) ) g.addElement(eLeft) ;
            g.addElement( new ElementOptional(eRight) ) ;                                   
            
        }
	/*************************************************************/

        public boolean containsOptional () { return containsOptional; }

        public boolean containsFilter () { return containsFilter; }

        public boolean containsUnion () { return containsUnion; }

        public void visit( OpDiff opDiff ) { throw new ARQNotImplemented("OpDiff") ; }

        public void visit( OpMinus opMinus ) { 
	    // throw new ARQNotImplemented("OpMinus") ;
	    // Keep things clearly separated.
	    elems.add("(");
            Element eLeft = asElement(opMinus.getLeft()) ;
            elems.add("MINUS");
            Element eRight = asElementGroup(opMinus.getRight()) ;
            elems.add(")");
            
            ElementGroup g = currentGroup() ;
            g.addElement(eLeft) ;
            //elems.push(eLeft);
            //elems.add("AND");
            g.addElement(eRight) ;
            //elems.push(eRight);
            return ;
        }

        public void visit(OpUnion opUnion) {
	    containsUnion = true;
	    elems.add("(");
            Element eLeft = asElementGroup(opUnion.getLeft()) ;
            elems.add("UNION");
            Element eRight = asElementGroup(opUnion.getRight()) ;
            elems.add(")");
            if ( eLeft instanceof ElementUnion ) {
                ElementUnion elUnion = (ElementUnion)eLeft ;
                //elems.add("UNION");
                elUnion.addElement(eRight) ;
                return ;
            }
            
//            if ( eRight instanceof ElementUnion )
//            {
//                ElementUnion elUnion = (ElementUnion)eRight ;
//                elUnion.getElements().add(0, eLeft) ;
//                return ;
//            }
            
            ElementUnion elUnion = new ElementUnion() ;
            elUnion.addElement(eLeft) ;
	    // elems.add(eLeft);
            //elems.add("UNION");
	    // elems.add(eRight);
            elUnion.addElement(eRight) ;
            currentGroup().addElement(elUnion) ;
        }

        public void visit(OpConditional opCondition)
        { throw new ARQNotImplemented("OpCondition") ; }

        public void visit(OpFilter opFilter) {
	    containsFilter = true;
            // (filter .. (filter ( ... ))   (non-canonicalizing OpFilters)
            // Inner gets Grouped unnecessarily. 
            Element e = asElement(opFilter.getSubOp()) ;
            if ( currentGroup() != e )
                currentGroup().addElement(e) ;
            currentGroup();
            
            ExprList exprs = opFilter.getExprs() ;
            for ( Expr expr : exprs ) {
                ElementFilter f = new ElementFilter(expr) ;
                currentGroup().addElement(f) ;
            }
        }

        public void visit(OpGraph opGraph)
        {
            startSubGroup() ;
            Element e = asElement(opGraph.getSubOp()) ;
            ElementGroup g = endSubGroup() ;
            
            Element graphElt = new ElementNamedGraph(opGraph.getNode(), e) ;
            currentGroup().addElement(graphElt) ;
        }

        public void visit(OpService opService)
        { 
            // Hmm - if the subnode has been optimized, we may fail.
            Op op = opService.getSubOp() ;
            Element x = asElement(opService.getSubOp()) ; 
            Element elt = new ElementService(opService.getService(), x) ;
            currentGroup().addElement(elt) ;
        }
        
        public void visit(OpDatasetNames dsNames)
        { throw new ARQNotImplemented("OpDatasetNames") ; }

        public void visit(OpTable opTable)
        { 
            // This will go in a group so simply forget it. 
            if ( opTable.isJoinIdentity() ) return ;
            throw new ARQNotImplemented("OpTable") ;
        }

        public void visit(OpExt opExt)
        {
//            Op op = opExt.effectiveOp() ;
//            // This does not work in all cases.
//            op.visit(this) ;
            throw new ARQNotImplemented("OpExt") ;
        }

        public void visit(OpNull opNull) { throw new ARQNotImplemented("OpNull") ; }

        public void visit(OpLabel opLabel) { /* No action */ }

        public void visit(OpAssign opAssign) { 
            for ( Var v : opAssign.getVarExprList().getVars() ) {
                Element elt = new ElementAssign(v, opAssign.getVarExprList().getExpr(v)) ;
                ElementGroup g = currentGroup() ;
                g.addElement(elt) ;
            }
        }
        public void visit(OpList opList) { /* No action */ }

        public void visit(OpOrder opOrder) {
            List<SortCondition> x = opOrder.getConditions() ;
            for ( SortCondition sc : x ) query.addOrderBy(sc);
            opOrder.getSubOp().visit(this) ;
        }

        public void visit(OpProject opProject) {
            query.setQueryResultStar(false);
            Iterator<Var> iter = opProject.getVars().iterator();
            for ( ; iter.hasNext() ; ) {
                Var v = iter.next();
                query.addResultVar(v) ;
            }
            opProject.getSubOp().visit(this) ;
        }

        public void visit(OpReduced opReduced) { 
            query.setReduced(true) ;
            opReduced.getSubOp().visit(this) ;
        }

        public void visit(OpDistinct opDistinct) { 
            query.setDistinct(true) ;
            opDistinct.getSubOp().visit(this) ;
        }

        public void visit(OpSlice opSlice) {
            if ( opSlice.getStart() != Query.NOLIMIT )
                query.setOffset(opSlice.getStart()) ;
            if ( opSlice.getLength() != Query.NOLIMIT )
                query.setLimit(opSlice.getLength()) ;
            opSlice.getSubOp().visit(this) ;
        }

        public void visit(OpGroupAgg opGroupAgg)
        { throw new ARQNotImplemented("OpGroupAgg") ; }
        
        private Element lastElement() {
            ElementGroup g = currentGroup ;
            if ( g == null || g.getElements().size() == 0 )
                return null ;
            int len = g.getElements().size() ;
            return g.getElements().get(len-1) ;
        }

        private void startSubGroup() {
            push(currentGroup) ;
            ElementGroup g = new ElementGroup() ;
            currentGroup = g ;
        }
        
        private ElementGroup endSubGroup() {
            ElementGroup g = pop() ;
            ElementGroup r = currentGroup ;
            currentGroup = g ;
            return r ;
        }
        
//        private void endCurrentGroup()
//        {
//            currentGroup = null ;
//            element = null ; //??
//        }
        
        private ElementGroup currentGroup() {
//            if ( currentGroup == null )
//                startSubGroup() ;
        	return currentGroup ;
        }
        
        private ElementGroup peek() {
            if ( stack.size() == 0 )
                return null ;
            return stack.peek();
        }
        private ElementGroup pop() { return stack.pop(); }
        private void push(ElementGroup el) { stack.push(el); }
        public void visit(OpProcedure opProcedure) {
            throw new ARQNotImplemented("OpProcedure") ;
        }
        
        public void visit(OpPropFunc opPropFunc) {
            throw new ARQNotImplemented("OpPropFunc") ;
        }
        
        public void visit(OpSequence opSequence) {
            throw new ARQNotImplemented("OpSequence") ;
        }
        
        public void visit(OpDisjunction opDisjunction) {
            throw new ARQNotImplemented("OpDisjunction") ;
        }

    }
}
