package com.marginallyclever.makelangelo.donatelloimpl;

import com.marginallyclever.donatello.Donatello;
import com.marginallyclever.donatello.nodes.images.LoadImage;
import com.marginallyclever.donatello.ports.Filename;
import com.marginallyclever.donatello.ports.InputFilename;
import com.marginallyclever.nodegraphcore.Node;
import com.marginallyclever.nodegraphcore.port.Input;
import com.marginallyclever.makelangelo.donatelloimpl.nodes.turtle.LoadTurtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.util.List;

/**
 * Allows the user to drag and drop a file onto the {@link Donatello} panel.
 */
public class DonatelloDropTarget extends DropTargetAdapter {
    private static final Logger logger = LoggerFactory.getLogger(DonatelloDropTarget.class);
    private final Donatello donatello;

    public DonatelloDropTarget(Donatello donatello) {
        super();
        this.donatello = donatello;
    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
        try {
            Transferable tr = dtde.getTransferable();
            DataFlavor[] flavors = tr.getTransferDataFlavors();
            for (DataFlavor flavor : flavors) {
                logger.debug("Possible flavor: {}", flavor.getMimeType());
                if (flavor.isFlavorJavaFileListType()) {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    Object o = tr.getTransferData(flavor);
                    if (o instanceof List<?> list && !list.isEmpty()) {
                        o = list.getFirst();
                        if (o instanceof File file) {
                            Node n = loadFile(file.getAbsolutePath());
                            if(n != null) {
                                var sp = donatello.getPaintArea().getMousePosition();
                                // convert the mouse position to donatello view coordinates.
                                var wp = donatello.getPaintArea().transformScreenToWorldPoint(sp);
                                n.setPosition(wp);
                            }

                            dtde.dropComplete(true);
                            return;
                        }
                    }
                }
            }
            logger.debug("Drop failed: {}", dtde);
            dtde.rejectDrop();
        } catch (Exception e) {
            logger.error("Drop error", e);
            dtde.rejectDrop();
        }
    }

    private Node loadFile(String absolutePath) {
        // determine if the file is an image or a vector
        try {
            // Load the file and if it doesn't fail then it's probably an image.
            // If it's stupid and it works... it's not that stupid.
            if (ImageIO.read(new File(absolutePath)) != null) {
                return loadImage(absolutePath);
            }
        } catch (Exception ignored) {}

        return loadVector(absolutePath);
    }

    /**
     * In Donatello add a {@link LoadImage} with the given file.  Assumes {@link LoadImage} can load the file.
     * @param absPath The absolute path to the file.
     */
    private Node loadImage(String absPath) {
        LoadImage loadImage = new LoadImage();
        var first = loadImage.getPort(0);
        if(!(first instanceof Input<?> inputFile)) throw new IllegalStateException("First variable is not an Input");
        if(!(inputFile.getValue() instanceof Filename)) throw new IllegalStateException("Input value is not a Filename");
        donatello.getGraph().add(loadImage);
        inputFile.setValue(new Filename(absPath));
        donatello.submit(loadImage);
        loadImage.updateBounds();
        return loadImage;
    }

    /**
     * In Donatello add a {@link LoadTurtle} with the given file.  Assumes {@link LoadTurtle} can load the file.
     * @param absPath The absolute path to the file.
     */
    private Node loadVector(String absPath) {
        LoadTurtle loadTurtle = new LoadTurtle();
        var first = loadTurtle.getPort(0);
        if(!(first instanceof InputFilename inputFile)) throw new IllegalStateException("First variable is not an Input");
        if(inputFile.getValue() == null) throw new IllegalStateException("Input value is not a Filename");
        donatello.getGraph().add(loadTurtle);
        inputFile.setValue(new Filename(absPath));
        donatello.submit(loadTurtle);
        loadTurtle.updateBounds();
        return loadTurtle;
    }
}
