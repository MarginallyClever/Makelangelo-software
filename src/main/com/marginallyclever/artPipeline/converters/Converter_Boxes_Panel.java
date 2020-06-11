package com.marginallyclever.artPipeline.converters;

import java.util.Observable;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectFloat;
import com.marginallyclever.makelangelo.select.SelectInteger;
import com.marginallyclever.makelangelo.select.SelectReadOnlyText;

public class Converter_Boxes_Panel extends ImageConverterPanel {
	private Converter_Boxes converter;
	private SelectInteger boxSize;
	private SelectFloat   cutoff;
	
	public Converter_Boxes_Panel(Converter_Boxes arg0) {
		super();
		this.converter=arg0;
		
		add(boxSize = new SelectInteger(Translator.get("BoxGeneratorMaxSize"),converter.getBoxMasSize()));
		add(cutoff = new SelectFloat(Translator.get("BoxGeneratorCutoff"),converter.getCutoff()));
		add(new SelectReadOnlyText(Translator.get("BoxGeneratorMaxSizeNote")));
		finish();
	}

	@Override
	public void update(Observable o, Object arg) {
		super.update(o, arg);
		
		converter.setBoxMaxSize((int)boxSize.getValue());
		converter.setCutoff((int)cutoff.getValue());
		if(loadAndSaveImage!=null) loadAndSaveImage.reconvert();
	}
}
