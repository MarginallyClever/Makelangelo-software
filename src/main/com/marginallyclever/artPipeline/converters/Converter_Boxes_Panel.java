package com.marginallyclever.artPipeline.converters;

import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JLabel;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectFloat;
import com.marginallyclever.makelangelo.select.SelectInteger;

public class Converter_Boxes_Panel extends ImageConverterPanel implements PropertyChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Converter_Boxes converter;

	SelectInteger boxSize;
	SelectFloat   cutoff;
	
	public Converter_Boxes_Panel(Converter_Boxes arg0) {
		this.converter=arg0;
		
		boxSize = new SelectInteger(converter.getBoxMasSize());
		cutoff = new SelectFloat(converter.getCutoff());
		
		setLayout(new GridLayout(5,1));
		this.add(new JLabel(Translator.get("BoxGeneratorMaxSize")));
		this.add(boxSize);
		this.add(new JLabel(Translator.get("BoxGeneratorCutoff")));
		this.add(cutoff);
		this.add(new JLabel(Translator.get("BoxGeneratorMaxSizeNote")));
		
		boxSize.addPropertyChangeListener("value",this);
		cutoff.addPropertyChangeListener("value",this);
	}

	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		converter.setBoxMaxSize(((Number)boxSize.getValue()).intValue());
		converter.setCutoff(((Number)cutoff.getValue()).floatValue());
		if(loadAndSaveImage!=null) loadAndSaveImage.reconvert();
	}
}
