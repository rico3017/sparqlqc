package fr.inrialpes.tyrexmo.testqc;

import java.lang.Exception;

public class ContainmentTestException extends Exception {

    private static final long serialVersionUID = 673;

    public ContainmentTestException( String message ) {
	super( message );
    }
    
    public ContainmentTestException( String message, Exception e ) {
	super( message, e );
    }
}
