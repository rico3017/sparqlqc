package fr.inrialpes.tyrexmo.queryanalysis;

import java.io.File;
import java.io.IOException;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;

public class OptionalCount {
	String dirName = "";
	public OptionalCount (String dirName) {
		this.dirName = dirName;
	}
	// Get all files in a directory 
    private File [] getAllFiles () throws IOException{
    	File folder = new File(this.dirName);
    	File[] listOfFiles = null;
    	if (folder.isDirectory() )
    		listOfFiles = folder.listFiles();              
    	return listOfFiles;
    }
    
    public int [] countOptional () throws IOException {
    	File [] queries = getAllFiles ();
    	int [] counts = new int[2];
    	int countAndOptional = 0, countUnionOptional = 0;
    	for (int i = 0; i < queries.length; i++) {
    		String fname = queries[i].getAbsolutePath();    		    		
    		try {
    			Query query = QueryFactory.read(fname);
        		TransformAlgebra ta = new TransformAlgebra (query) ;
	    		if (ta.containsOpt() && ta.hasUnion())
	    			++countUnionOptional;
	    		if (ta.containsOpt() && ! ta.hasUnion() )
	    			++countAndOptional;
    		} catch(Exception e) {
    			continue;
    		}
    		
    	}
    	counts[0] = countUnionOptional; 
    	counts[1] = countAndOptional;
    	return counts;
    }
    
    /*
    public int countOptional () throws IOException {
    	File [] queries = getAllFiles ();
    	int count = 0;
    	for (int i = 0; i < queries.length; i++) {
    		String fname = queries[i].getAbsolutePath();    		    		
    		try {
    			Query query = QueryFactory.read(fname);
        		TransformAlgebra ta = new TransformAlgebra (query) ;
	    		if (ta.containsOpt())
	    			++count;
    		} catch(Exception e) {
    			continue;
    		}
    		
    	}
    	return count;
    } */
    
	public static void main (String [] args) throws IOException {
		try {
			String dirName = "/Users/mel/Desktop/these/DBpediaLog/queries1";  //args[0];
//			String dirName = "/Users/mel/Desktop/these/DBpediaLog/optional";
			//String dirName = args[0];
			OptionalCount oc = new OptionalCount (dirName);
			System.out.println("# of queries that contain OPTIONAL with UNION patterns: " + oc.countOptional()[0]);
			System.out.println("# of queries that contain OPTIONAL with BGPs: " + oc.countOptional()[1]);
		} catch (Exception e) {
			System.err.println("Please enter a valid directory that contains SPARQL queries.");
		}
	}
}
