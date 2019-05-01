package fr.inrialpes.tyrexmo.queryanalysis;

/**
 * JE: This is useless... QueryFactory.read( f.toURI().toString() ) does it
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ReadQuery {
    private String firstQuery, secondQuery;
	
    public ReadQuery() {}
	
    /**
     * Read two queries from a file
     * @param f1 : filename of the first query
     *
     * @param f2: filename of sencod query
     * @throws IOException 
     */
    public ReadQuery (String f1, String f2) throws IOException {
	setFirstQuery( read(f1) );
	setSecondQuery( read(f2) );
    }
    /**
     *  Read a query from a file
     * @param fname a file containing a SPARQL query
     * @return returns the query as a string
     * @throws IOException
     */
    public String read(String fname) throws IOException {
	String qry = "";
	try {
	    BufferedReader in = new BufferedReader(new FileReader(fname));
	    String str;
	    while ((str = in.readLine()) != null) {
		qry += str;
	    }
	    in.close();
	} catch (IOException e) {}
	return qry;
    }
    protected void setFirstQuery(String q) {
	firstQuery = q;
    }
    protected void setSecondQuery(String q) {
	secondQuery = q;
    }
    public String getFirstQuery() {
	return firstQuery;
    }
    public String getSecondQuery() {
	return secondQuery;
    }
}	
