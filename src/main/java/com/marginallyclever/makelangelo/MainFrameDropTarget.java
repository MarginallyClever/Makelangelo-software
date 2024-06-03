package com.marginallyclever.makelangelo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.util.List;

public class MainFrameDropTarget extends DropTargetAdapter {
    private static final Logger logger = LoggerFactory.getLogger(MainFrameDropTarget.class);
    private final Makelangelo app;

    public MainFrameDropTarget(Makelangelo app) {
        super();
        this.app = app;
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
                        o = list.get(0);
                        if (o instanceof File file) {
                            app.openFile(file.getAbsolutePath());
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
