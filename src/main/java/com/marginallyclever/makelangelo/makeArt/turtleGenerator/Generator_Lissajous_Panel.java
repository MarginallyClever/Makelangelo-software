package com.marginallyclever.makelangelo.makeArt.turtleGenerator;

import java.beans.PropertyChangeEvent;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectReadOnlyText;
import com.marginallyclever.makelangelo.select.SelectSlider;

/**
 * Panel for {@link Generator_Lissajous}
 * @author Dan Royer
 *
 */
public class Generator_Lissajous_Panel extends TurtleGeneratorPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected SelectSlider field_a;
	protected SelectSlider field_b;
	protected SelectSlider field_numSamples;
	protected SelectSlider field_delta;
	protected Generator_Lissajous generator;
	
	Generator_Lissajous_Panel(Generator_Lissajous generator) {
		super();
		this.generator = generator;

		add(field_a = new SelectSlider("a",Translator.get("LissajousA"),100,1,Generator_Lissajous.getA()));
		add(field_b = new SelectSlider("b",Translator.get("LissajousB"),100,1,Generator_Lissajous.getB()));
		add(field_delta = new SelectSlider("delta",Translator.get("LissajousDelta"),1000,0,(int)(Generator_Lissajous.getDelta()*1000.0)));
		add(field_numSamples = new SelectSlider("samples",Translator.get("SpirographNumSamples"),2000,50,Generator_Lissajous.getNumSamples()));
		add(new SelectReadOnlyText("url","<a href='https://en.wikipedia.org/wiki/Lissajous_curve'>Learn more</a>"));
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);

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
			generator.generate();
		}
	}
}
