package com.marginallyclever.makelangelo.makeart.imageconverter;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.select.SelectToggleButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Image converter that runs an evolving, iterative process until it converges on some goal or the user pauses it.
 * @author Dan Royer
 */
public abstract class IterativeImageConverter extends ImageConverter {
    private static final Logger logger = LoggerFactory.getLogger(IterativeImageConverter.class);
    private ImageConverterThread imageConverterThread;
    private final SelectToggleButton pauseButton;

    public IterativeImageConverter() {
        super();

        pauseButton = new SelectToggleButton("pauseButton",Translator.get("PlotterControls.Pause"));
        add(pauseButton);
        pauseButton.addPropertyChangeListener((evt) -> {
            imageConverterThread.setPaused(pauseButton.isSelected());
        });
    }
    
    @Override
    public void start(Paper paper, TransformedImage image) {
        super.start(paper,image);
        imageConverterThread = new ImageConverterThread(this);
        imageConverterThread.execute();
    }

    @Override
    public void stop() {
        super.stop();
        logger.debug("stop()");
        imageConverterThread.setPaused(true);
        imageConverterThread.endThreadGracefully();
        while(!imageConverterThread.isDone());
        imageConverterThread = null;
    }

    public boolean isThreadCancelled() {
        if(imageConverterThread!=null && imageConverterThread.isCancelled()) return true;
        return false;
    }

    public void setThread(ImageConverterThread p) {
        imageConverterThread = p;
    }

    /**
     * run one "step" of an iterative image conversion process.
     * @return true if conversion should iterate again.
     */
    public abstract boolean iterate();

    /**
     * called when the user pauses the conversion.  Should generate the {@link com.marginallyclever.makelangelo.turtle.Turtle} output.
     */
    public abstract void generateOutput();

    protected ImageConverterThread getThread() {
        return imageConverterThread;
    }

    public abstract void resume();
}
