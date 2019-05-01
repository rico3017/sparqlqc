package fr.inrialpes.tyrexmo.queryanalysis;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.Subgraph;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.sparql.core.Var;

public class CyclesAmongNDvars {

    DirectedGraph<String, DefaultEdge> g =
        new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
    UndirectedGraph<String, DefaultEdge> g1 =
	new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
    ArrayList<String> nodeNames = new ArrayList<String>();
    Set<String> tripleNodeNames = new HashSet<String> ();
    Set<String> constantsAndDvars = new HashSet<String> ();
    
    CyclesAmongNDvars () {};

    public CyclesAmongNDvars (List<Triple> triples) {
    	createGraph(triples);
    }
	/**
     * Creates a directed graph based on URL objects that represents link
     * structure.
     *
     * @return a graph based on String objects (URIs, Vars, Literals).
     */
    private void reification(Triple t)
    {
    	// subject, predicate, and object of a triple 
//    	String tripleNode = genTripleNodeName();
//    	nodeNames.add(tripleNode);
//    	System.out.println(tripleNode);
    	String tripleNode = getUniqueTripleNodeName();
    	tripleNodeNames.add(tripleNode);
//    	System.out.println(getUniqueTripleNodeName());
        String subject = t.getSubject().toString(); //t.getSubject().getName();
        String predicate = t.getPredicate().toString(); //t.getPredicate().getLocalName();
        String object = t.getObject().toString();//t.getObject().getName();
        
        if (t.getSubject().isURI() ||  t.getSubject().isBlank())
        	constantsAndDvars.add(subject);
        if (t.getPredicate().isURI())
        	constantsAndDvars.add(predicate);
        if (t.getObject().isURI() || t.getObject().isLiteral() || t.getObject().isBlank())
        	constantsAndDvars.add(object);
        
        
        // add the vertices
        g.addVertex(tripleNode);
        g.addVertex(subject);
        g.addVertex(predicate);
        g.addVertex(object);

        // add edges to create linking structure
        g.addEdge(subject, tripleNode);
        g.addEdge(tripleNode, predicate);
        g.addEdge(tripleNode, object);
        
//        System.out.println("["+subject+" "+ predicate + " " + object+"]");
           
    }
    public boolean checkIfaDAGisTree() {
    	/*Set<DefaultEdge> edges = new HashSet<DefaultEdge>();
    	for (Iterator it = edges.iterator(); it.hasNext(); it.next()) {
    		if (this.g.getEdgeSource((DefaultEdge) it) == this.g.getEdgeSource((DefaultEdge) it.next()))
    			return true;
    	}*/
    	for (DefaultEdge edges1 : this.g.edgeSet()) {
    		for (DefaultEdge edges2 : this.g.edgeSet()) {
    			if (!edges1.equals(edges2)) {
    				if ( this.g.getEdgeTarget(edges1).equals(this.g.getEdgeTarget(edges2)))
    					return true;
    			}
    		}
    	}
    	return false;
    }
    private void createGraph(List<Triple> triples) {
    	for(Triple t : triples)
    		reification(t);
    }
    /**
     * Generate random names for triple nodes
     * @param varName
     * @return
     */
    static final String ab = "0123456789abcdefghijklmnopqrstuvwxyz";   //CDEFGHIJKLMNOPQRSTUVWXYZ";
    static Random rnd = new Random();

    String randomString( int len ) 
    {
       StringBuilder sb = new StringBuilder( len );
       for( int i = 0; i < len; i++ ) 
          sb.append( ab.charAt( rnd.nextInt(ab.length()) ) );
       return sb.toString();
    }


    private String getUniqueTripleNodeName () {
    	String tripleNodeName = randomString(5);
    	if (nodeNames.isEmpty())
    		nodeNames.add(tripleNodeName);
    	else {
    		if (nodeNames.contains(tripleNodeName))
    			return getUniqueTripleNodeName();
    	}
    	return tripleNodeName;
    }
    
    private boolean detectCycle(String varName) {
    	CycleDetector<String,DefaultEdge> detector = new CycleDetector<String,DefaultEdge>(this.g);
    	if(!g.containsVertex(varName)) {
    		System.err.println("Vertex does not exist in the graph");
    		return false;
    	}
    	return detector.detectCyclesContainingVertex(varName);
    }
    /**
     * Detect cycles among non-distinguished variables
     * @param ndvars : all the non-distinugished variables in the query
     * @return true or false
     */
    public boolean isCycle(Collection<Var> ndvars) {
    	boolean cycle = false; 
    	if (!ndvars.isEmpty()) {
	    	for (Var v : ndvars) {
	    		if (detectCycle(v.toString()) ) {
	    			cycle = true;
	    			break;
	    		} 			
	    	}
    	} else cycle = false;
    	return cycle;
    }
    public boolean isCyclic() {
    	boolean cycle = false;
    	CycleDetector<String,DefaultEdge> detector = new CycleDetector<String,DefaultEdge>(this.g);
    	cycle = detector.detectCycles();
    	return cycle;
    }
    public Set<String> convertFromVarToString (Collection<Var> ndvars) {
    	Set<String> vars = new HashSet<String> ();
    	for (Var var : ndvars) {
    		vars.add(var.toString());
    	}
    	return vars;
    }
    
    public Set<String> convertFromVarToString (List<Var> ndvars) {
    	Set<String> vars = new HashSet<String> ();
    	for (Var var : ndvars) {
    		vars.add(var.toString());
    	}
    	return vars;
    }
    public Set<String> convertListToString (List<String> ndvars) {
    	Set<String> vars = new HashSet<String> ();
    	for (String var : ndvars) {
    		vars.add(var);
    	}
    	return vars;
    }
    
    public boolean isThereAcycleAmongNDvars(Collection<Var> ndvars) {
    	boolean cycle = false;
    	CycleDetector<String,DefaultEdge> detector = new CycleDetector<String,DefaultEdge>(this.g);
    	if (detectCyclesContainingNDvars(ndvars) && !ndvars.isEmpty()) {    
    		if (detectCyclesContainingConstantsAndDvars())
    			cycle = false;
    		else {
        	//cycle = detector.detectCycles();
    		Set<String> varsAppearingInAcycle = detector.findCycles();
    		
//    		Subgraph<String, ?, DirectedGraph<String, DefaultEdge>> sg = new Subgraph (this.g,detector.findCycles(), this.g.edgeSet());
//    		System.out.println( sg.edgeSet()); //this.g.edgeSet());
    		Set<DefaultEdge> edges = new HashSet<DefaultEdge> ();    		
    		for (String vertex : varsAppearingInAcycle) {
    			edges.addAll(this.g.edgesOf(vertex));    			
    		}
    		//System.out.println(edges);
    		// edges is a the cyclic component
    		// vertexes involved in the cyclic component 
    		Set<String> vertex = new HashSet<String> ();
    		for (DefaultEdge e : edges) {    			
    			vertex.add(this.g.getEdgeSource(e));
    			vertex.add(this.g.getEdgeTarget(e));
    		}
    		//System.out.println(vertex);
    		cycle = !cycleContainsAConstantOrADvar(vertex);
    		
		}
	} else cycle = false;
    	
    	return cycle;
    }
   
    private boolean detectCyclesContainingNDvars(Collection<Var> ndvars) {
    	boolean cycle = false;
    	if (ndvars.isEmpty())
    		cycle = false;
    	else {
    		for (Var var: ndvars) {
    			CycleDetector<String,DefaultEdge> detector = new CycleDetector<String,DefaultEdge>(this.g);
    			if (detector.detectCyclesContainingVertex(var.toString())) { 
    				cycle = true;
    				break;
    			}
    		}
    	}
    	return cycle;
    }
    
    // check if all the cycles involve constants 
    private boolean detectCyclesContainingConstantsAndDvars() {
    	boolean cycle = false;
    	if (constantsAndDvars.isEmpty())
    		cycle = false;
    	else {
    		for (String var: constantsAndDvars) {
    			CycleDetector<String,DefaultEdge> detector = new CycleDetector<String,DefaultEdge>(this.g);
    			if (detector.detectCyclesContainingVertex(var.toString())) { 
    				cycle = true;
    				break;
    			}
    		}
    	}
    	return cycle;
    }
    //cyclic component contains a constant or a dvar
    private boolean cycleContainsAConstantOrADvar(Set<String> vertex) {
    	boolean contains = false;
    	if (constantsAndDvars.isEmpty())
    		contains = false;
    	else {
    		for (String var: constantsAndDvars) {    			
    			if (vertex.contains(var)) { 
    				contains = true;
    				break;
    			}
    		}
    	}
    	return contains;
    }

    public static void main(String [] args) throws IOException {
    	String ucqDir = "/Users/mel/Desktop/these/DBpediaLog/ucq/";
    	String filterDir = "/Users/mel/Desktop/DBpediaLog/filter/";
    	String constructDir = "/Users/mel/Desktop/DBpediaLog/construct/";
    	String queries = "/Users/mel/Desktop/these/DBpediaLog/queries1/";   //dbpedia queries
    	String cyclic = "/Users/mel/Documents/Implementation/tests/benchmark/cyclic/";
    	String cyclic1 = "/Users/mel/Documents/Implementation/tests/cyclic/";
    	String gitCyclic = "/Users/mel/Documents/papers/mel/test/benchmark/cyclic/";
    	String askcyclic = "/Users/mel/Documents/papers/mel/test/benchmark/cyclic/ask/";
    	String proj = "/Users/mel/Documents/papers/mel/test/benchmark/projection/";
    	ucqCyclicComponentTest(cyclic);
    } 

    
    static void ucqCyclicComponentTest( String dir ) throws IOException {
    	String missingPrefixes = "PREFIX geo: <http://www.example.com/>   PREFIX foaf: <http://xmlns.com/foaf/0.1/>";
    	int acyclic = 0, cyclic = 0, syntaxError = 0;    	    	
    	File [] queries = getAllFiles (dir);
    	for (int i = 0; i < queries.length; i++) {
    		String fname = queries[i].getAbsolutePath();
    		String query = new ReadQuery().read(fname);
    		query = missingPrefixes + query;
    		try {
	            TransformAlgebra tf = new TransformAlgebra(query);
	            CyclesAmongNDvars cyclicNDvars = new CyclesAmongNDvars(tf.getTriples());    			
    			cyclicNDvars.constantsAndDvars.addAll(cyclicNDvars.convertFromVarToString(tf.getResultVars()));    			
    			if (cyclicNDvars.isThereAcycleAmongNDvars(tf.getNonDistVars()))
    				cyclic++;
    			else acyclic++;           
            }  catch (Exception e) { 
            	syntaxError++;
    			continue;
    		}    		
    	}
    	System.out.println("Queries that contain cyclic components among their ndvars = " + cyclic);
    	System.out.println("Queries that DO NOT contain cyclic components among their ndvars = " + acyclic);
    	System.out.println("Queries with SYNTAX ERRORS = "+ syntaxError );
    }
    
    // Get all files in a directory 
    private static File [] getAllFiles (String dirName) {
    	return new File(dirName).listFiles();
    }
    
   
}
