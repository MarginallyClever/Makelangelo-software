package com.marginallyclever.makelangelo.select;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URI;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;

/**
 * A file selection dialog
 * @author Dan Royer
 * @since 7.24.0
 */
public class SelectFile extends Select {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1731350044217921795L;
	private JLabel label;
	private JTextField field;
	private JButton chooseButton;
	private FileFilter filter = null;
	private JFileChooser choose = new JFileChooser();
	
	public SelectFile(String internalName,String labelValue,String defaultValue) {
		super(internalName);
		
		label = new JLabel(labelValue,JLabel.LEADING);

		field = new JTextField(defaultValue, 16);
		field.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				validate();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				validate();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				validate();
			}
			
			void validate() {
				notifyPropertyChangeListeners(null,field.getText());
			}
		});
		//field.setBorder(new LineBorder(Color.BLACK));

		chooseButton = new JButton("...");
		chooseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				field.setText(selectFile(field.getText()));
			}
		});
		
		JPanel panel2 = new JPanel(new BorderLayout());
		//
		panel2.add(field,BorderLayout.LINE_END); // (so the size of the text field is 13 chars no more / no less do not resize even on windows resize.)
		//panel2.add(field,BorderLayout.CENTER); // As set in center the field can expend to use maximun space, usefull for long filename and/or on windows resize.		
		
		this.add(label,BorderLayout.LINE_START);
		this.add(panel2,BorderLayout.CENTER);
		this.add(chooseButton,BorderLayout.LINE_END);
	}
	
	/**
	 * PPAC37 : For my this is odd to use getText() that return a String, and not a getFile() that return a File in a Select*File*. 
	 * 
	 * In the case of a drag and drop directly in the text field, the text can be a URI.
	 * ex : file:///home/q6/firmw%20are.hex and therefore to not have to handle this particular case downstream, use getFile which does it.
	 * @return
	 * @deprecated use getFile
	 */
	@Deprecated
	public String getText() {
		return field.getText();
	}
	
	/**
	 * To get a File from the value of field.getText().
	 * <p>
	 *  take care of a posible URI.
	 * <p>
	 * do not check if the file exist or if it can be read.
	 * 
	 * NOT IMPLEMENTED : if Multiple files the first.
	 * <p>
	 * <ul>
	 * <li>
	 * On my system if i drop 2 file from "Fichiers" ( the File exploreur on my linux Ubuntu 18.04 for the Graphical serveur i use gnome3 ) 
	 * <code>file:///home/q6/firmw%20are.hex file:///home/q6/makelangelo.properties</code>
	 * </li>
	 * <li>
	 * On my Win7 if i drop to file from the Windows Explorateur de fichiers
	 * </li>
	 * </ul>
	 * 
	 * 
	 * @return null if not a valid uri file or isBlank text field.
	 */
	public File getFile(){
	    String s = field.getText(); 	    
	    if (s != null && !s.isBlank()) {
		// maybe a uri if a drag and drop ex : "file:///home/q6/firmw%20are.hex"; 
		if (s.startsWith("file://")) {
		    try {
			URI uri = new URI(s);
			s = uri.getPath(); // To decode the uri is anyspécial char ( like a space ' ' is normaly coded "%20" in its uri forms.
		    } catch (Exception e) {

		    }
		}
		return new File(s);
	    }
	    return null;	    
	}
	
	private String selectFile(String cancelValue) {
		choose.setFileFilter(filter);
		int returnVal = choose.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = choose.getSelectedFile();
			return file.getAbsolutePath();
		} else {
			return cancelValue;
		}
	}
	
	public void setFilter(FileFilter filter) {
		this.filter = filter;
	}

	/**
	 * Will notify observers that the value has changed.
	 * @param string
	 */
	public void setText(String string) {
		field.setText(string);
	}

	public void setPathOnly() {
		choose.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	}
	
	public void setFileOnly() {
		choose.setFileSelectionMode(JFileChooser.FILES_ONLY);
	}

	public void setFileHidingEnabled(boolean b) {
	    choose.setFileHidingEnabled(b);
	}
	
	
}
