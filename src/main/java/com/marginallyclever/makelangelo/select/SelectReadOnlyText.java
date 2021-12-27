package com.marginallyclever.makelangelo.select;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JLabel;

/**
 * Read only HTML-rich text that can display AT MOST one clickable link.
 * @author Dan Royer
 * @since 7.24.0
 */
public class SelectReadOnlyText extends Select {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8918068053490064344L;
	private static final String A_HREF = "<a href=";
	private static final String HREF_CLOSED = ">";
	
	private JLabel label;
	
	public SelectReadOnlyText(String internalName,String labelKey) {
		super(internalName);
		
		label = new JLabel("<html>"+ labelKey+"</html>",JLabel.LEADING);
		label.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
		        String text = label.getText();
		        try {
		        	String link = getPlainLink(text);
		        	if(link==null) return;
		            URI uri = new java.net.URI(link);
		            Desktop.getDesktop().browse(uri);
		        } catch (URISyntaxException | IOException e) {
		            throw new AssertionError(e.getMessage() + ": " + text); //NOI18N
		        }
			}
		});
		this.add(label,BorderLayout.CENTER);
	}
	

	private String getPlainLink(String s) {
		int first = s.indexOf(A_HREF);
		if(first<0) return null;
		int last = s.indexOf(HREF_CLOSED,first);
		if(last<0) return null;
	    return s.substring(first + A_HREF.length()+1, last-1);
	}
}
