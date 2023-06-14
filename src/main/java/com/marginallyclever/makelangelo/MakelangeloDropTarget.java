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

public class MakelangeloDropTarget extends DropTargetAdapter {
    private static final Logger logger = LoggerFactory.getLogger(MakelangeloDropTarget.class);
    private final Makelangelo app;

    public MakelangeloDropTarget(Makelangelo app) {
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
                    if (o instanceof List<?>) {
                        List<?> list = (List<?>) o;
                        if (list.size() > 0) {
                            o = list.get(0);
                            if (o instanceof File) {
                                app.openFile(((File) o).getAbsolutePath());
                                dtde.dropComplete(true);
                                return;
                            }
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
