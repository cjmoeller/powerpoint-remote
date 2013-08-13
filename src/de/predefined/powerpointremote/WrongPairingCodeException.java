package de.predefined.powerpointremote;

/**
 * Throw this Exception when the user enters a wrong pairing Code.
 * @author Julius
 */
public class WrongPairingCodeException extends Exception {
    /**
	 * 
	 */
	private static final long serialVersionUID = -7077812156956083914L;
	public WrongPairingCodeException() {

    }
	/**
	 * The constructor
	 * @param msg
	 * 		A custom message to display.
	 */
    public WrongPairingCodeException(String msg) {
        super(msg);
    }
}
