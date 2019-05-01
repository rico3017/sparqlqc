package fr.inrialpes.tyrexmo.queryanalysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.HelpFormatter;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.io.File;
import java.net.URI;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;

public class Analysis {
    final static Logger logger = LoggerFactory.getLogger( Analysis.class );

    protected Options options = null;

    protected String queryDir = null;

    protected String outputType = "html";

    protected String outputFile = null;

    protected PrintStream stream = null;

    static final int NONE = 0; // means not even a and... (triple pattern)
    static final int AND = 1; // means only and (basic graph pattern)
    static final int UNION = 2; // means UNION and AND (UCQ)
    static final int OPT = 3;
    static final int FILTER = 4;
    static final int UNION_OPT = 5;
    static final int OPT_FILTER = 6;
    static final int FILTER_UNION = 7;
    static final int UNION_OPT_FILTER = 8;

    static final int CYCLE = 0;
    static final int DAG = 1;
    static final int TREE = 2;

    static final int PROJ = 0;
    static final int NOPROJ = 1;

    int totalNumber = 0;
    int correctNumber = 0;
    int failure = 0;
    int ndVarCycles = 0;
    int[][][] resultArray = null;

    public Analysis() {
	resultArray = new int[NOPROJ+1][TREE+1][UNION_OPT_FILTER+1];
	for( int i = 0; i <= NOPROJ; i++ ) {
	    for( int j = 0; j <= TREE; j++ ) {
		for( int k = 0; k <= UNION_OPT_FILTER; k++ ) {
		    resultArray[i][j][k] = 0;
		}
	    }
	}
	options = new Options();
	options.addOption( "h", "help", false, "Print this page" );
	options.addOption( OptionBuilder.withLongOpt( "output" ).hasArg().withDescription( "Result FILE" ).withArgName("FILE").create( 'o' ) );
	options.addOption( OptionBuilder.withLongOpt( "format" ).hasArg().withDescription( "Output format [NIY]" ).withArgName("TYPE (asc|plot)").create( 'f' ) );
    }

    public static void main( String[] args ) throws Exception {
	new Analysis().run( args );
    }

    public void run ( String [] args ) throws Exception, IOException {
	// Read parameters
	String[] argList = null;
	try { 
	    CommandLineParser parser = new PosixParser();
	    CommandLine line = parser.parse( options, args );
	    if ( line.hasOption( 'h' ) ) { usage(); System.exit( 0 ); }
	    if ( line.hasOption( 'f' ) ) outputType = line.getOptionValue( 'f' );
	    if ( line.hasOption( 'o' ) ) outputFile = line.getOptionValue( 'o' );
	    argList = line.getArgs();
	    if ( argList.length < 1 ) {
		logger.error( "Usage: TestContain SolverClass Q1 Q2" );
		usage();
		System.exit( -1 );
	    }
	} catch( ParseException exp ) {
	    logger.error( exp.getMessage() );
	    usage();
	    System.exit(-1);
	}
	queryDir = argList[0];

	// Set output file
	if ( outputFile == null ) {
	    stream = System.out;
	} else {
	    stream = new PrintStream( new FileOutputStream( outputFile ) );
	}

	File [] subdir = ( new File( queryDir ) ).listFiles();
	int size = subdir.length;
	for ( int i=0 ; i < size; i++ ) {
	    File queryFile = subdir[i];
	    if( queryFile.isFile() ) {
		logger.trace( queryFile.toString() );
		totalNumber++;
		try {
		    // PARSE
		    Query q = parseQuery( queryFile.toURI() );
		    // ANALYSE
		    analyse( q );
		    correctNumber++;
		} catch ( Exception ex ) {
		    failure++;
		}
	    }
	}
	// RENDER
	render();
    }

    /** THIS SHOULD BE PASSED TO THE PARSER **/
    String missingPrefixes = "PREFIX geo: <http://www.example.com/>   PREFIX foaf: <http://xmlns.com/foaf/0.1/>";

    public Query parseQuery( URI qFile ) throws Exception {
	if ( qFile != null ) {
	    return QueryFactory.read( qFile.toString() );
	} else throw new Exception( "Cannot parse query" );
    }

    public void analyse( Query query ) throws Exception {
	int projected;
	int cycle;
	int constr;
	TransformAlgebra ta = new TransformAlgebra( query );
	// CycleDetector.java AcyclicnessTester CyclicQuery.java
	cycle = 0;
	projected = 0;
	// OptionalCount.java => done
	if ( ta.containsOpt() ) {
	    if ( ta.hasUnion() ) {
		if ( ta.hasFilter() ) {
		    constr = UNION_OPT_FILTER;
		} else {
		    constr = UNION_OPT;
		}
	    } else {
		if ( ta.hasFilter() ) {
		    constr = OPT_FILTER;
		} else {
		    constr = OPT;
		}
	    }
	} else {
	    if ( ta.hasUnion() ) {
		if ( ta.hasFilter() ) {
		    constr = FILTER_UNION;
		} else {
		    constr = UNION;
		}
	    } else {
		if ( ta.hasFilter() ) {
		    constr = FILTER;
		} else {
		    constr = NONE;
		}
	    }
	}
		/*****/
	// ... this is an extra one
	// CyclesAmongNDvars.java
    	int acyclic = 0, cyclic = 0, syntaxError = 0;    	    	
	TransformAlgebra tf = new TransformAlgebra(query);
	CyclesAmongNDvars cyclicNDvars = new CyclesAmongNDvars( tf.getTriples() );    			
	cyclicNDvars.constantsAndDvars.addAll( cyclicNDvars.convertFromVarToString(tf.getResultVars() ) );
	if (cyclicNDvars.isThereAcycleAmongNDvars( tf.getNonDistVars() ) ) ndVarCycles++;
	(resultArray[projected][cycle][constr])++;
    }

    public void render() {
	stream.println( failure+" errors over "+totalNumber+" queries (residu: "+correctNumber+")" );
	stream.println( "Number of queries with cycles using only ndvariables: "+ndVarCycles+"\n" );
	stream.println( "\t\tproj\t\t\tnoproj" );
	stream.println( "\t\ttree\tdag\tcycle\ttree\tdag\tcycle" );
	stream.println( "none\t\t"+resultArray[0][0][NONE] );
	stream.println( "and\t\t"+"??" );
	stream.println( "union\t\t"+resultArray[0][0][UNION] );
	stream.println( "opt\t\t"+resultArray[0][0][OPT] );
	stream.println( "filter\t\t"+resultArray[0][0][FILTER] );
	stream.println( "un-opt\t\t"+resultArray[0][0][UNION_OPT] );
	stream.println( "opt-filt\t"+resultArray[0][0][OPT_FILTER] );
	stream.println( "filt-un\t\t"+resultArray[0][0][FILTER_UNION] );
	stream.println( "un-opt-filt\t"+resultArray[0][0][UNION_OPT_FILTER] );
	stream.println( "TOTAL\t\t"+resultArray[0][0][UNION_OPT_FILTER] );
    }

    public void usage() {
	Package pkg = this.getClass().getPackage();
	new HelpFormatter().printHelp( 80, pkg+" [options] queryDir\nAnalyses the queries contained in queryDir", "\nOptions:", options, "" );
    }

}
