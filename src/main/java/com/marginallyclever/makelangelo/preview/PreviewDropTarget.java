package com.marginallyclever.makelangelo.preview;

import com.marginallyclever.makelangelo.MainFrame;
import com.marginallyclever.makelangelo.actions.LoadFileAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.util.List;

/**
 * Allows the user to drag and drop a file onto the {@link OpenGLPanel}.
 */
public class PreviewDropTarget extends DropTargetAdapter {
    private static final Logger logger = LoggerFactory.getLogger(PreviewDropTarget.class);
    private final MainFrame frame;

    public PreviewDropTarget(MainFrame frame) {
        super();
        this.frame = frame;
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
                            (new LoadFileAction(null,frame,null)).openFile(file.getAbsolutePath());
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
}
