package de.predefined.powerpointremote;

/**
 * Created by Julius on 23.05.13.
 * This Exception is thrown, when the user enters a wrong pairing Code
 */
public class WrongPairingCodeException extends Exception {
    /**
	 * 
	 */
	private static final long serialVersionUID = -7077812156956083914L;
	public WrongPairingCodeException() {

    }
    public WrongPairingCodeException(String msg) {
        super(msg);
    }
}
