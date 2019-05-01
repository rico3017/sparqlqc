package fr.inrialpes.tyrexmo.queryanalysis;

// JE: 2013
// Most of this is useless
// There are here for the main below
// Or other mains for counting cycles in query logs...
// Should also be rewritten the name generation (not fully useful for finding cycles...)

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.jgrapht.DirectedGraph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.sparql.core.Var;

public class CyclicQuery {
    DirectedGraph<String, DefaultEdge> g =
        new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
    UndirectedGraph<String, DefaultEdge> g1 =
	new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
    ArrayList<String> nodeNames = new ArrayList<String>();
    
    public CyclicQuery (List<Triple> triples) {
    	createGraph(triples);
    }
    /**
     * Creates a directed graph based on URL objects that represents link
     * structure.
     *
     * @return a graph based on String objects (URIs, Vars, Literals).
     */
    private void reification(Triple t) {
    	// subject, predicate, and object of a triple 
//    	String tripleNode = genTripleNodeName();
//    	nodeNames.add(tripleNode);
//    	System.out.println(tripleNode);
    	String tripleNode = getUniqueTripleNodeName();
//    	System.out.println(getUniqueTripleNodeName());
        String subject = t.getSubject().toString(); //t.getSubject().getName();
        String predicate = t.getPredicate().toString(); //t.getPredicate().getLocalName();
        String object = t.getObject().toString();//t.getObject().getName();
        
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

    protected static int varRank = 0;
    protected String genTripleNodeName() { return "_t"+varRank++; }
    /*
    private String genTripleNodeName() {
    	
    	String vars = "xyz";
		String nums = "0123456789";
		String name = null;
		boolean unique = true;
		Random randomGen = new Random();
		while (unique) {
			int character = randomGen.nextInt(3);
			int num = randomGen.nextInt(10);
			name = vars.substring(character)+"_"+nums.substring(num);
			if(nodeNames.isEmpty()) { nodeNames.add(name); unique = false; //System.out.println(name);
			}
			else {
				if (!nodeNames.contains(name)) 
					unique = false;
			}
		} 
    	return "_t"+ name;
	}*/

    /**
     * Generate random names for triple nodes
     * @param varName
     * @return
     */
    static final String ab = "0123456789abcdefghijklmnopqrstuvwxyz";   //CDEFGHIJKLMNOPQRSTUVWXYZ";
    static Random rnd = new Random();

    String randomString( int len )  {
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
    
    /*public static void main(String [] args) {
    	String query3 = "PREFIX ex: <http://example.com/0.1/>"+
		"SELECT ?x WHERE {" +
		 "?x ex:a ?y . ?y ex:b ?z . ?z ex:c ?r . ?r ex:d ?y ."+
		 "}";
    	String query2 = "SELECT ?x ?l WHERE {?x ?p ?l}";
    	
    	String query1 = "PREFIX ex: <http://example.com/0.1/>"+ 
	    "PREFIX rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" + 
		"SELECT ?x  WHERE" +
	    "{{?x ex:translated ?l} UNION {?x ex:wrote ?l }  ?l rdf:type ex:Poem  .}";
    	
    	String query8 = "PREFIX rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
		"PREFIX bench:   <http://localhost/vocabulary/bench/> " +
		"PREFIX dc:      <http://purl.org/dc/elements/1.1/>" +
		"PREFIX dcterms: <http://purl.org/dc/terms/>" +
		"PREFIX foaf:    <http://xmlns.com/foaf/0.1/>" +
		"PREFIX swrc:    <http://swrc.ontoware.org/ontology#>" +
		
		"SELECT DISTINCT ?name1 ?name2" + 
		"WHERE {" +
		  "?article1 rdf:type bench:Article ." +
		  "?article2 rdf:type bench:Article ." +
		  "?article1 dc:creator ?author1 . " +
		  "?author1 foaf:name ?name1 ." +
		  "?article2 dc:creator ?author2 ." +
		  "?author2 foaf:name ?name2 ."+
		  "?article1 swrc:journal ?journal ."+
		  "?article2 swrc:journal ?journal ." +
		  "FILTER (?name1<?name2) }";
    	
    	SyntaxAnalyzer sa = new SyntaxAnalyzer(query8);
    	sa.setTriples();
    	CyclicQuery cq = new CyclicQuery();
    	cq.createGraph(sa.getTriples());
    	System.out.println(cq.g.edgeSet());
    	CycleDetector<String,DefaultEdge> cdetect = new CycleDetector<String,DefaultEdge>(cq.g);
    	System.out.println(cdetect.detectCycles());
    	//System.out.println(cq.detectCycles(cq.g, "ex:b"));
    }*/

    public static void main(String [] args) throws IOException {
    	String missingPrefixes = "PREFIX geo: <http://www.example.com/>   PREFIX foaf: <http://xmlns.com/foaf/0.1/>";
    	int acyclic = 0, cyclic = 0;
    	int dag = 0, tree = 0;
    	//for (int i = 378530; i > 378330; i--) {
	//    	for (int i = 1; i < 378001; i++) {
	//    		String fname = "/Users/mel/Desktop/DBpediaLog/queries1/query"+i;
    	for (int i = 1; i <= 9; i++) {
	    String fname = "/Users/mel/Documents/Implementation/tests/cyclic/q"+i;
	    String query = new ReadQuery().read(fname);
	    query = missingPrefixes + query;
	    //    		System.out.println(query);
	    try {
		TransformAlgebra tf = new TransformAlgebra(query);
		// if (new CyclicQuery(tf.getTriples()).isCycle(tf.getNonDistVars()))
		CyclicQuery cq = new CyclicQuery(tf.getTriples());
		if (cq.isCyclic()) 
		    //if (new CyclicQuery(tf.getTriples()).isCyclic())
		    cyclic++;
		else {
		    acyclic++;
		    if (cq.checkIfaDAGisTree()) dag++;
		    else tree++;
		}
	    }  catch (QueryParseException e) {   ///(Exception e) { // catch (QueryParseException e) {
		//continue;
		System.out.println(fname);
	    }
    	}
        /*String query = new ReadQuery().read("/Users/mel/Desktop/DBpediaLog/queries1/query4");
	  TransformAlgebra tf = new TransformAlgebra(query);
	  System.out.println(new CyclicQuery(tf.getTriples()).isCycle(tf.getNonDistVars()));*/
    	System.out.println("Acyclics = "+ acyclic + "    cyclics=" + cyclic);
    	System.out.println("DAG = "+ dag + "    Tree=" + tree);
    }
}
