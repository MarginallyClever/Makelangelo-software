package com.marginallyclever.core.node;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.filechooser.FileNameExtensionFilter;

import com.marginallyclever.core.select.Select;
import com.marginallyclever.core.select.SelectFile;

/**
 * Convenience class
 * @author Dan Royer
 * @since 7.25.0
 */
public class NodeConnectorExistingFile extends NodeConnectorString {
	private FileNameExtensionFilter myFilter;
	
	public NodeConnectorExistingFile(String newName,FileNameExtensionFilter filter,String defaultValue) {
		super(newName,defaultValue);
		myFilter=filter;
	}
	public NodeConnectorExistingFile(String newName) {
		super(newName,"");
	}

	@Override
	public Select getSelect() {
		SelectFile s = new SelectFile(this.getName(),myFilter,this.getValue());
		s.addPropertyChangeListener(new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				setValue((String)evt.getNewValue());
			}
		});
		return s;
	}
}
