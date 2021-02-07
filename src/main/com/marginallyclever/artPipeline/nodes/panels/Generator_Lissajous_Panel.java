package com.marginallyclever.artPipeline.nodes.panels;

import java.beans.PropertyChangeEvent;

import com.marginallyclever.artPipeline.nodes.Generator_Lissajous;
import com.marginallyclever.convenience.nodes.NodePanel;
import com.marginallyclever.convenience.select.SelectReadOnlyText;
import com.marginallyclever.convenience.select.SelectSlider;
import com.marginallyclever.makelangelo.Translator;

/**
 * Panel for {@link Generator_Lissajous}
 * @author Dan Royer
 *
 */
public class Generator_Lissajous_Panel extends NodePanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected SelectSlider field_a;
	protected SelectSlider field_b;
	protected SelectSlider field_numSamples;
	protected SelectSlider field_delta;
	protected Generator_Lissajous generator;
	
	public Generator_Lissajous_Panel(Generator_Lissajous generator) {
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
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);

		Generator_Lissajous.setB(field_b.getValue());
		Generator_Lissajous.setA(field_a.getValue());
		Generator_Lissajous.setDelta(field_delta.getValue()/1000.0f);
		Generator_Lissajous.setNumSamples(field_numSamples.getValue());
		generator.restart();
	}
}
