package com.marginallyclever.artPipeline.generators;

import java.util.Observable;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectInteger;

public class Generator_Package_Panel extends ImageGeneratorPanel {
	private Generator_Package generator;
	private SelectInteger width;
	private SelectInteger length;
	private SelectInteger height;
	
	Generator_Package_Panel(Generator_Package generator) {
		super();
		
		this.generator = generator;

		add(width = new SelectInteger(Translator.get("Width"),generator.getLastWidth()));
		add(height = new SelectInteger(Translator.get("Height"),generator.getLastHeight()));
		add(length = new SelectInteger(Translator.get("Length"),generator.getLastLength()));
		finish();
	}

	@Override
	public void update(Observable o, Object arg) {
		super.update(o, arg);
		
		generator.setWidth(((Number)width.getValue()).intValue());
		generator.setHeight(((Number)height.getValue()).intValue());
		generator.setLength(((Number)length.getValue()).intValue());
		makelangeloRobotPanel.regenerate(generator);
	}
}
