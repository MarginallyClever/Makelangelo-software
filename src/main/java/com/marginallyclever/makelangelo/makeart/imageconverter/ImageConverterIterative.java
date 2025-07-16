package com.marginallyclever.makelangelo.makeart.imageconverter;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.preview.OpenGLPanel;
import com.marginallyclever.makelangelo.preview.PreviewListener;
import com.marginallyclever.donatello.select.SelectToggleButton;
import com.marginallyclever.makelangelo.preview.ShaderProgram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Extends {@link ImageConverter} to run in an evolving, iterative process until it converges on some goal or the user pauses it.
 * Implements {@link PreviewListener} to draw progress while it runs.
 * @author Dan Royer
 * @since 7?
 */
public abstract class ImageConverterIterative extends ImageConverter implements PreviewListener {
    private static final Logger logger = LoggerFactory.getLogger(ImageConverterIterative.class);
    private ImageConverterThread imageConverterThread;
    private final SelectToggleButton pauseButton;
    protected final Lock lock = new ReentrantLock();

    public ImageConverterIterative() {
        super();

        pauseButton = new SelectToggleButton("pauseButton",Translator.get("PlotterControls.Pause"));
        add(pauseButton);

        pauseButton.addSelectListener((evt) -> {
            imageConverterThread.setPaused(pauseButton.isSelected());
        });
    }


    @Override
    public void start(Paper paper, TransformedImage image) {
        super.start(paper,image);
        logger.debug("start()");
        if(imageConverterThread!=null) {
            logger.warn("called while thread is still running.");
            stop();
        }
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
        return imageConverterThread != null && imageConverterThread.isCancelled();
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

    /**
     * Callback from {@link OpenGLPanel} that it is time to render to the WYSIWYG display.
     *
     * @param shader the render context
     */
    @Override
    public void render(ShaderProgram shader) {}
}
