package com.marginallyclever.makelangelo.makeart.imageconverter;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectBoolean;
import com.marginallyclever.makelangelo.select.SelectDouble;
import com.marginallyclever.makelangelo.select.SelectReadOnlyText;
import com.marginallyclever.makelangelo.select.SelectSlider;

public class Converter_FlowField_Panel extends ImageConverterPanel {
	private static final long serialVersionUID = 1L;

	public Converter_FlowField_Panel(Converter_FlowField converter) {
		super(converter);
		add(new SelectDouble("scaleX",Translator.get("Generator_FlowField.scaleX"), Converter_FlowField.getScaleX()));
		add(new SelectDouble("scaleY",Translator.get("Generator_FlowField.scaleY"),Converter_FlowField.getScaleY()));
		add(new SelectDouble("offsetX",Translator.get("Generator_FlowField.offsetX"),Converter_FlowField.getOffsetX()));
		add(new SelectDouble("offsetY",Translator.get("Generator_FlowField.offsetY"),Converter_FlowField.getOffsetY()));
		add(new SelectSlider("stepSize",Translator.get("Generator_FlowField.stepSize"),20,3,Converter_FlowField.getStepSize()));
		add(new SelectBoolean("rightAngle",Translator.get("Generator_FlowField.rightAngle"),Converter_FlowField.getRightAngle()));
		add(new SelectReadOnlyText("url","<a href='https://en.wikipedia.org/wiki/Perlin_noise'>"+Translator.get("TurtleGenerators.LearnMore.Link.Text")+"</a>"));
	}
}
