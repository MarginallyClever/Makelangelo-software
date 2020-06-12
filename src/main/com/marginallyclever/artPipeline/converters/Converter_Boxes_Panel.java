package com.marginallyclever.artPipeline.converters;

import java.util.Observable;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectReadOnlyText;
import com.marginallyclever.makelangelo.select.SelectSlider;

public class Converter_Boxes_Panel extends ImageConverterPanel {
	private Converter_Boxes converter;
	private SelectSlider boxSize;
	private SelectSlider cutoff;
	
	public Converter_Boxes_Panel(Converter_Boxes arg0) {
		super();
		this.converter=arg0;
		
		add(boxSize = new SelectSlider(Translator.get("BoxGeneratorMaxSize"),20,1,converter.getBoxMasSize()));
		add(cutoff = new SelectSlider(Translator.get("BoxGeneratorCutoff"),255,0,converter.getCutoff()));
		add(new SelectReadOnlyText(Translator.get("BoxGeneratorMaxSizeNote")));
		finish();
	}

	@Override
	public void update(Observable o, Object arg) {
		super.update(o, arg);
		
		converter.setBoxMaxSize(boxSize.getValue());
		converter.setCutoff(cutoff.getValue());
		if(loadAndSaveImage!=null) loadAndSaveImage.reconvert();
	}
}
