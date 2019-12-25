package com.marginallyclever.artPipeline.converters;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectBoolean;

public class Converter_Spiral_Panel extends ImageConverterPanel implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Converter_Spiral converter;
	
	SelectBoolean toCornersField;
	
	public Converter_Spiral_Panel(Converter_Spiral arg0) {
		this.converter=arg0;

		this.setLayout(new GridLayout(0, 1));

		this.setLayout(new GridLayout(0,1));
		this.add(new JLabel(Translator.get("SpiralToCorners")));
		this.add(toCornersField = new SelectBoolean(converter.getToCorners()));
		
		toCornersField.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		converter.setToCorners(toCornersField.isSelected());
		if(loadAndSaveImage!=null) loadAndSaveImage.reconvert();
	}
}
