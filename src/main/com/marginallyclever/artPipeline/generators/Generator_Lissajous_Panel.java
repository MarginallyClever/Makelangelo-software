package com.marginallyclever.artPipeline.generators;

import java.util.Observable;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectReadOnlyText;
import com.marginallyclever.makelangelo.select.SelectSlider;

public class Generator_Lissajous_Panel extends ImageGeneratorPanel {
	protected SelectSlider field_a;
	protected SelectSlider field_b;
	protected SelectSlider field_numSamples;
	protected SelectSlider field_delta;
	protected Generator_Lissajous generator;
	
	Generator_Lissajous_Panel(Generator_Lissajous generator) {
		super();
		this.generator = generator;

		add(field_a = new SelectSlider(Translator.get("LissajousA"),100,1,Generator_Lissajous.getA()));
		add(field_b = new SelectSlider(Translator.get("LissajousB"),100,1,Generator_Lissajous.getB()));
		add(field_delta = new SelectSlider(Translator.get("LissajousDelta"),1000,0,(int)(Generator_Lissajous.getDelta()*1000.0)));
		add(field_numSamples = new SelectSlider(Translator.get("SpirographNumSamples"),2000,50,Generator_Lissajous.getNumSamples()));
		add(new SelectReadOnlyText("<a href='https://en.wikipedia.org/wiki/Lissajous_curve'>Learn more</a>"));
		finish();
	}

	@Override
	public void update(Observable o, Object arg) {
		super.update(o, arg);

		int newB = field_b.getValue();
		int newA = field_a.getValue();
		float newDelta = field_delta.getValue()/1000.0f;
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
