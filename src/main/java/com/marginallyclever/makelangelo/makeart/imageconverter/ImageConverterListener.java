package com.marginallyclever.makelangelo.makeart.imageconverter;

/**
 * Interface for listeners to {@link ImageConverter}.
 */
public interface ImageConverterListener {
    void onRestart(ImageConverter panel);
    void onConvertFinished(ImageConverter converter);
}
