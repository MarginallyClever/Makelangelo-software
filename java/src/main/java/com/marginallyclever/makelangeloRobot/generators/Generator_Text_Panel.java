package com.marginallyclever.makelangeloRobot.generators;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.marginallyclever.makelangelo.SelectInteger;

public class Generator_Text_Panel extends JPanel implements DocumentListener, ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	JTextArea text;
	SelectInteger size;
	Generator_Text generator;
	JComboBox<String> fontChoices;
	
	
	Generator_Text_Panel(Generator_Text generator) {
		this.generator = generator;

		text = new JTextArea(generator.getLastMessage(), 6, 60);
		size = new SelectInteger(generator.getLastSize());
		fontChoices = new JComboBox<String>(generator.getFontNames());
		fontChoices.setSelectedIndex(generator.getLastFont());
		
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTHWEST;

		c.gridx=0;
		add(size,c);
		c.gridx=1;
		add(fontChoices,c);
		c.gridx=0;
		c.gridy++;
		c.gridwidth=2;
		add(new JScrollPane(text),c);
		
		text.getDocument().addDocumentListener(this);
		size.getDocument().addDocumentListener(this);
		fontChoices.addActionListener(this);
	}

	@Override
	public void changedUpdate(DocumentEvent arg0) {
		validate();
	}

	@Override
	public void insertUpdate(DocumentEvent arg0) {
		validate();
	}

	@Override
	public void removeUpdate(DocumentEvent arg0) {
		validate();
	}
	
	public void validate() {
		generator.setMessage(text.getText());
		generator.setSize(((Number)size.getValue()).intValue());
		generator.regenerate();
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		generator.setFont(fontChoices.getSelectedIndex());
		generator.regenerate();
	}
}
