package com.marginallyclever.artPipeline.generators;

import java.util.Observable;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectFloat;
import com.marginallyclever.makelangelo.select.SelectInteger;

public class Generator_Lissajous_Panel extends ImageGeneratorPanel {
	protected SelectInteger field_a;
	protected SelectInteger field_b;
	protected SelectInteger field_numSamples;
	protected SelectFloat field_delta;
	protected Generator_Lissajous generator;
	
	Generator_Lissajous_Panel(Generator_Lissajous generator) {
		super();
		this.generator = generator;

		add(field_a = new SelectInteger(Translator.get("LissajousA"),Generator_Lissajous.getA()));
		add(field_b = new SelectInteger(Translator.get("LissajousB"),Generator_Lissajous.getB()));
		add(field_delta = new SelectFloat(Translator.get("LissajousDelta"),Generator_Lissajous.getDelta()));
		add(field_numSamples = new SelectInteger(Translator.get("SpirographNumSamples"),Generator_Lissajous.getNumSamples()));
	}

	@Override
	public void update(Observable o, Object arg) {
		super.update(o, arg);

		int newB = field_b.getValue();
		int newA = field_a.getValue();
		float newDelta = field_delta.getValue();
		int newNumSamples = field_numSamples.getValue();

		if(newB != Generator_Lissajous.getB() ||
			newA != Generator_Lissajous.getA() ||
			newDelta != Generator_Lissajous.getDelta() ||
			newNumSamples != Generator_Lissajous.getNumSamples() ) {
			Generator_Lissajous.setB(newB);
			Generator_Lissajous.setA(newA);
			Generator_Lissajous.setDelta(newDelta);
			Generator_Lissajous.setNumSamples(newNumSamples);
			makelangeloRobotPanel.regenerate(generator);
		}
	}
}
