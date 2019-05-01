package fr.inrialpes.tyrexmo.queryanalysis;

import java.io.File;
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
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.core.Var;

public class AcyclicnessTester {
	DirectedGraph<String, DefaultEdge> g =
        new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
	UndirectedGraph<String, DefaultEdge> g1 =
		new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
    ArrayList<String> nodeNames = new ArrayList<String>();
    
    AcyclicnessTester () {}
    public AcyclicnessTester (List<Triple> triples) {
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

    	String tripleNode = getUniqueTripleNodeName();
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
           
    }
    public boolean checkIfaDAGisTree() {
    	
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
 
    public static void main(String [] args) {
    	/*String ucqDir = "/Users/mel/Desktop/DBpediaLog/ucq/";
    	String filterDir = "/Users/mel/Desktop/DBpediaLog/filter/";
    	String constructDir = "/Users/mel/Desktop/DBpediaLog/construct/";
    	String queries = "/Users/mel/Desktop/DBpediaLog/queries1/";   //dbpedia queries */
//    	ucqTreeDagTest(ucqDir);
    	
    	try {
    		System.out.println(args[0]);
    		projNoProjTest(args[0]);
    	} catch (Exception e) {
    		//System.err.println(e);
    		System.err.println("Please, enter a valid directory containing SPARQL queries.");
    	}
    }
    
    static void ucqTreeDagTest(String dir) throws IOException {
    	String missingPrefixes = "PREFIX geo: <http://www.example.com/>   PREFIX foaf: <http://xmlns.com/foaf/0.1/>";
    	int acyclic = 0, cyclic = 0;
    	int dag = 0, tree = 0;
    	
    	File [] queries = getAllFiles (dir);
    	for (int i = 0; i < queries.length; i++) {
    		String fname = queries[i].getAbsolutePath();
    		String query = new ReadQuery().read(fname);
    		query = missingPrefixes + query;
    		try {
            TransformAlgebra tf = new TransformAlgebra(query);
            AcyclicnessTester cq = new AcyclicnessTester(tf.getTriples());
            if (cq.isCyclic()) 
            	cyclic++;
            else {
            	acyclic++;
            	if (cq.checkIfaDAGisTree()) dag++;
            	else tree++;
             }
    		}  catch (Exception e) { 
    			continue;
    		}
    		
    		
    	}
    	System.out.println("Acyclics = "+ acyclic + "    cyclics = " + cyclic);
    	System.out.println("DAG = "+ dag + "    Tree = " + tree);
    }
    
    // Get all files in a directory 
    private static File [] getAllFiles (String dirName) {
    	File folder = new File(dirName);
    	File[] listOfFiles = folder.listFiles();              
    	return listOfFiles;
    }
    
    private boolean projectionCount (String query) {
    	Query q = QueryFactory.create(query);
    	return q.isQueryResultStar();
    }
    private static void projNoProjTest(String dir) throws IOException {
    	String missingPrefixes = "PREFIX geo: <http://www.example.com/>   PREFIX foaf: <http://xmlns.com/foaf/0.1/>";
    	int cProj = 0, cNoProj = 0, syntaxerror = 0;  	
    	int dagProj = 0, dagNoProj = 0;
    	int treeProj = 0, treeNoProj = 0;
    	int acyclic = 0, cyclic = 0;
    	int dag = 0, tree = 0;
    	
    	File [] queries = getAllFiles (dir);
    	for (int i = 0; i < queries.length; i++) {
    		String fname = queries[i].getAbsolutePath();
    		String query = new ReadQuery().read(fname);
    		query = missingPrefixes + query;
    		try {
            TransformAlgebra tf = new TransformAlgebra(query);
            AcyclicnessTester cq = new AcyclicnessTester(tf.getTriples());
            if (cq.isCyclic()) {
            	cyclic++;
            	if (cq.projectionCount(query)) 
                	cNoProj++;
                else {
                	cProj++;
                 }
            }
            else {
            	acyclic++;
            	if (cq.checkIfaDAGisTree()){
            		dag++;
            		if (cq.projectionCount(query)) 
                    	dagNoProj++;
                    else {
                    	dagProj++;
                     }
            	}
            	else {
            		tree++;
            		if (cq.projectionCount(query)) 
                    	treeNoProj++;
                    else {
                    	treeProj++;
                     }
            	}
             }
    		}  catch (Exception e) { 
    			syntaxerror++;continue;
    		}
    		
    		
    	}
    	System.out.println("Acyclics = "+ acyclic + "    Cyclics=" + cyclic);
    	System.out.println("DAG = "+ dag + "    Tree=" + tree);
    	System.out.println("Cyclic stars = "+ cNoProj + "    Cyclic proj = " + cProj + "  Syntax error = " + syntaxerror);
    	System.out.println("DAG stars = "+ dagNoProj + "     DAG proj = " + dagProj + "  Syntax error = " + syntaxerror);
    	System.out.println("Tree stars = "+ treeNoProj + "    Tree proj = " + treeProj + "  Syntax error = " + syntaxerror);
    }
}
