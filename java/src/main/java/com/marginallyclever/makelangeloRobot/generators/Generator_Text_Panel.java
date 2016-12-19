package com.marginallyclever.makelangeloRobot.generators;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.marginallyclever.makelangelo.SelectInteger;
import com.marginallyclever.makelangelo.Translator;

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

		text = new JTextArea(generator.getLastMessage(), 8, 40);
		size = new SelectInteger(generator.getLastSize());
		fontChoices = new JComboBox<String>(generator.getFontNames());
		fontChoices.setSelectedIndex(generator.getLastFont());

		setLayout(new BoxLayout(this,BoxLayout.PAGE_AXIS));
		JPanel p;
		p = new JPanel(new GridLayout(0, 1));
		p.add(new JLabel(Translator.get("TextSize")),BorderLayout.WEST);
		p.add(size,BorderLayout.WEST);
		this.add(p);
		p = new JPanel(new GridLayout(0, 1));
		p.add(new JLabel(Translator.get("TextFont")),BorderLayout.WEST);
		p.add(fontChoices,BorderLayout.WEST);
		this.add(p);
		p = new JPanel(new GridLayout(0, 1));
		p.add(new JLabel(Translator.get("TextMessage")),BorderLayout.WEST);
		this.add(p);
		p = new JPanel(new GridLayout(0, 1));
		p.add(new JScrollPane(text),BorderLayout.WEST);
		this.add(p);
		
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
