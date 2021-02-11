package com.marginallyclever.makelangelo.nodeConnector;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.imageio.ImageIO;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.marginallyclever.core.TransformedImage;
import com.marginallyclever.core.log.Log;
import com.marginallyclever.core.node.NodeConnector;
import com.marginallyclever.core.select.Select;
import com.marginallyclever.core.select.SelectFile;
import com.marginallyclever.makelangelo.Translator;

/**
 * Convenience class
 * @author Dan Royer
 * @since 7.25.0
 */
public class NodeConnectorTransformedImage extends NodeConnector<TransformedImage> {
	public NodeConnectorTransformedImage(String newName) {
		super(newName);
	}
	
	public NodeConnectorTransformedImage(String newName,TransformedImage d) {
		super(newName,d);
	}

	@Override
	public Select getSelect() {
		// TODO in read-only mode, show a thumbnail?
		// TODO in read/write mode, show a thumbnail? AND a file selection dialog.
		// TODO SelectTransformedImage(label,source filename)
		String name = Translator.get("TransformedImage.inputFileFormat");
		FileNameExtensionFilter filter = new FileNameExtensionFilter(name, ImageIO.getReaderFileSuffixes());
		SelectFile s = new SelectFile(name,filter,"");
		s.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				String filename = (String)evt.getNewValue();
				try {
					TransformedImage ti = TransformedImage.loadImage(filename);
					setValue(ti);
				}
				catch(Exception e) {
					Log.message("ImageIO failed to load "+filename);
				}
			}
		});
		return s;
	}
}
