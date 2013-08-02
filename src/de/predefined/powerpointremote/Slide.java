package de.predefined.powerpointremote;

import android.graphics.Bitmap;

/**
 * Created by Julius on 18.05.13.
 * The current slide data is stored in this class
 */
public class Slide {
    private Bitmap currentView;
    private String notes;

    /**
     *
     * @return returns the notes of a slide
     */
    public String getNotes() {
        return notes;
    }

    /**
     *
     * @param notes the new notes for the slide
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     *
     * @return returns the image of a slide
     */
    public Bitmap getCurrentView() {
        return currentView;
    }

    /**
     *
     * @param currentView the new image for the slide
     */
    public void setCurrentView(Bitmap currentView) {
        this.currentView = currentView;
    }


}
