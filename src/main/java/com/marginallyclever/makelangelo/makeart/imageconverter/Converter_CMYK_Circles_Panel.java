package com.marginallyclever.makelangelo.makeart.imageconverter;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectReadOnlyText;
import com.marginallyclever.makelangelo.select.SelectSlider;

public class Converter_CMYK_Circles_Panel extends ImageConverterPanel {
	private static final long serialVersionUID = 1L;

	public Converter_CMYK_Circles_Panel(Converter_CMYK_Circles converter) {
		super(converter);
		add(new SelectSlider("maxCircleSize", Translator.get("Converter_CMYK_Circles.maxCircleSize"), 10, 1, converter.getMaxCircleSize()));
		add(new SelectReadOnlyText("note",Translator.get("ConverterCMYKNote")));

		addPropertyChangeListener((evt)->{
			if(evt.getPropertyName().equals("maxCircleSize")) converter.setMaxCircleSize((int)evt.getNewValue());
			fireRestartConversion();
		});
	}
}
