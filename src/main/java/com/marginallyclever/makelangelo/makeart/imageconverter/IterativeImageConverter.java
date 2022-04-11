package com.marginallyclever.makelangelo.makeart.imageconverter;

import com.marginallyclever.makelangelo.Translator;

import javax.swing.*;

/**
 * Image converter that repeatedly converts an image until it converges or the user pauses it.
 * @author Dan Royer
 */
public abstract class IterativeImageConverter extends ImageConverter {
    private boolean keepIterating=true;
    private final ProgressMonitor progressMonitor;
    private ImageConverterThread thread;

    public IterativeImageConverter() {
        super();

        progressMonitor = new ProgressMonitor(null, Translator.get("Converting"), "", 0, 100);
        progressMonitor.setProgress(0);
        progressMonitor.setMillisToPopup(0);
    }

    public void setProgress(int d) {
        if(progressMonitor ==null) return;
        progressMonitor.setProgress(d);
    }

    public boolean getKeepGoing() {
        return keepIterating;
    }

    public void setKeepGoing(boolean newValue) {
        keepIterating = newValue;
    }

    public boolean isThreadCancelled() {
        if(thread!=null && thread.isCancelled()) return true;
        if(progressMonitor !=null && !progressMonitor.isCanceled()) return true;
        return false;
    }

    public void setThread(ImageConverterThread p) {
        thread = p;

        thread.addPropertyChangeListener((evt) -> {
            String propertyName = evt.getPropertyName();
            if (propertyName.equals("progress")) {
                int progress = (Integer) evt.getNewValue();
                progressMonitor.setProgress(progress);
                String message = String.format("%d%%.\n", progress);
                progressMonitor.setNote(message);
            }
        });
    }

    /**
     * run one "step" of an iterative image conversion process.
     * @return true if conversion should iterate again.
     */
    public abstract boolean iterate();

    public void stopIterating() {
        keepIterating=false;
    }

}
