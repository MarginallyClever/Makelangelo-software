package com.marginallyclever.makelangelo.makeart.imageconverter;

import com.marginallyclever.makelangelo.turtle.Turtle;

/**
 * Interface for listeners to {@link ImageConverter}.
 * @author Dan Royer
 * @since 2022-05-10
 */
public interface ImageConverterListener {

    /**
     * Called when the converter requests a complete restart.  This is usually due to user action in the GUI.
     */
    void onRestart(ImageConverter panel);

    /**
     * Called when the converter wants to offer a new turtle.
     * @param turtle
     */
    void onConvertFinished(Turtle turtle);
}
