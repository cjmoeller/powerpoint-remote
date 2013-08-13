package de.predefined.powerpointremote;

/**
 * Constants for the Message id. No enum, because we have the custom ordinal
 * 100.
 * 
 * @author Julius
 * 
 */
public class MessageID {
	
	/** Authentication ID. */
	public static final int AUTHENTICATE = 0;
	/** Start presentatjon ID. */
	public static final int START_PRESENTATION = 1;
	/** Stop pesentation ID. */
	public static final int STOP_PRESENTATION = 2;
	/** Display next slide ID. */
	public static final int NEXT_SLIDE = 3;
	/** Display previous slide ID. */
	public static final int PREVIOUS_SLIDE = 4;
	/** Receive new notes to display ID. */
	public static final int NOTES_DATA = 5;
	/** Receive a new image to display ID. */
	public static final int IMAGE_DATA = 6;
	/** Ping ID. */
	public static final int PING = 100;

}
