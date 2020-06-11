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
 * Read only HTML-rich text that can display a clickable link.  Can work with AT MOST one clickable link.
 * @author Dan Royer
 * @since 7.24.0
 */
public class SelectReadOnlyText extends Select {
	private static final String A_HREF = "<a href='";
	private static final String HREF_CLOSED = "'>";
	
	private JLabel label;
	
	public SelectReadOnlyText(String labelKey) {
		super();
		
		label = new JLabel("<html>"+ labelKey+"</html>",JLabel.LEADING);
		label.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
		        JLabel l = (JLabel) evt.getSource();
		        try {
		            URI uri = new java.net.URI(getPlainLink(label.getText()));
		            Desktop.getDesktop().browse(uri);
		        } catch (URISyntaxException | IOException use) {
		            throw new AssertionError(use + ": " + l.getText()); //NOI18N
		        }
			}
		});
		panel.add(label,BorderLayout.CENTER);
	}
	

	private String getPlainLink(String s) {
	    return s.substring(s.indexOf(A_HREF) + A_HREF.length(), s.indexOf(HREF_CLOSED));
	}
}
