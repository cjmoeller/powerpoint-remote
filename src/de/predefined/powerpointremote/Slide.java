package de.predefined.powerpointremote;

import android.graphics.Bitmap;

/**
 * This class represents a slide.
 * @author Julius
 */
public class Slide {
	/** The current view. */
	private Bitmap currentView;
	/** The notes. */
	private String notes;

	/**
	 * Gets the notes.
	 * 
	 * @return The notes of a slide
	 */
	public String getNotes() {
		return notes;
	}

	/**
	 * Sets the notes.
	 * 
	 * @param notes
	 *            The new notes for the slide
	 */
	public void setNotes(String notes) {
		this.notes = notes;
	}

	/**
	 * Gets the current view.
	 * 
	 * @return The image of a slide
	 */
	public Bitmap getCurrentView() {
		return currentView;
	}

	/**
	 * Sets the current view.
	 * 
	 * @param currentView
	 *            The new image for the slide
	 */
	public void setCurrentView(Bitmap currentView) {
		this.currentView = currentView;
	}

}
