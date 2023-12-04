package com.marginallyclever.makelangelo.select;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;


/**
 * A JFormattedTextField that sets itself up to format integers.  Also includes a "random" button.
 * @author Dan Royer
 * @since 7.50.5
 */
public class SelectRandomSeed extends Select {
	private JFormattedTextField field;
	private int value;
	private Timer timer=null;

	public SelectRandomSeed(String internalName, String labelKey, Locale locale, int defaultValue) {
		super(internalName);

		value = defaultValue;

		JLabel label = new JLabel(labelKey, JLabel.LEADING);

		field = new JFormattedTextField();
		createAndAttachFormatter(locale);
		Dimension d = field.getPreferredSize();
		d.width = 100;
		field.setPreferredSize(d);
		field.setMinimumSize(d);
		field.setValue(defaultValue);
		field.setHorizontalAlignment(JTextField.RIGHT);
		field.getDocument().addDocumentListener(new DocumentListener() {
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
				try {
					int newNumber = Integer.parseInt(field.getText());
					field.setForeground(UIManager.getColor("Textfield.foreground"));
					if(value != newNumber) {
						int oldValue = value;
						value = newNumber;

						if(timer!=null) timer.cancel();
						timer = new Timer("Delayed response");
						timer.schedule(new TimerTask() {
							public void run() {
								fireSelectEvent(oldValue,newNumber);
							}
						}, 100L); // brief delay in case someone is typing fast
					}
				} catch(NumberFormatException e1) {
					field.setForeground(Color.RED);
					return;
				}
			}
		});

		JButton chooseButton = new JButton("↻");
		chooseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				field.setText(Integer.toString((int)(Math.random()*Integer.MAX_VALUE)));
			}
		});


		JPanel panel2 = new JPanel(new BorderLayout());
		panel2.add(field,BorderLayout.LINE_END);

		this.add(label,BorderLayout.LINE_START);
		this.add(panel2,BorderLayout.CENTER);
		this.add(chooseButton,BorderLayout.LINE_END);
	}

	public SelectRandomSeed(String internalName, String labelKey, Locale locale) {
		this(internalName,labelKey,locale,0);
	}

	public SelectRandomSeed(String internalName, String labelKey, int defaultValue) {
		this(internalName,labelKey,Locale.getDefault(),defaultValue);
	}

	public SelectRandomSeed(String internalName) {
		super(internalName);
		createAndAttachFormatter(Locale.getDefault());
	}
	
	protected void createAndAttachFormatter(Locale locale) {
		NumberFormat nFloat = NumberFormat.getIntegerInstance(locale);
		nFloat.setGroupingUsed(false);
		
		NumberFormatter nff = new NumberFormatter(nFloat);
		DefaultFormatterFactory factory = new DefaultFormatterFactory(nff);
		field.setFormatterFactory(factory);
	}
	
	public void setReadOnly() {
		field.setEditable(false);
	}
	
	/**
	 * @return last valid integer typed into field.
	 */
	public int getValue() {
		return value;
	}
	
	public void setValue(int arg0) {
		field.setText(Integer.toString(arg0));
	}
}
