package com.marginallyclever.makelangelo;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.util.List;

import javax.swing.TransferHandler;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.makelangeloRobot.MakelangeloRobot;

/**
 * support for drag & drop files into Makelangelo software.
 * @author Dan Royer
 *
 */
public class MakelangeloTransferHandler  extends TransferHandler {
	MakelangeloRobot robot;
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MakelangeloTransferHandler(MakelangeloRobot robot) {
		super();
		this.robot = robot;
	}
	
	@Override
    public boolean canImport(TransferHandler.TransferSupport info) {
        // we only import FileList
        if (!info.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            Log.message("Does not support files of type(s): "+info.getDataFlavors());
            return false;
        }
        return true;
    }
    
    @SuppressWarnings("unchecked")
	@Override
    public boolean importData(TransferHandler.TransferSupport info) {
        if (!info.isDrop()) return false;            
        if(!canImport(info)) return false;
        if(robot.getControlPanel()==null) return false;

        // Get the fileList that is being dropped.
        Transferable t = info.getTransferable();
        List<File> data;
        try {
            data = (List<File>)t.getTransferData(DataFlavor.javaFileListFlavor);
        } 
        catch (Exception e) { return false; }

        if(data==null) return false;
        if(data.size()<1) return false;
        
        String filename = data.get(0).getAbsolutePath();

        // Check if we know this file type via LoadFileType.canLoad()
        // Load the file.
        return robot.getControlPanel().openFileOnDemand(filename);
    }
}