package com.marginallyclever.artPipeline.nodes.panels;

import java.beans.PropertyChangeEvent;

import com.marginallyclever.artPipeline.nodes.Generator_Spirograph;
import com.marginallyclever.convenience.nodes.NodePanel;
import com.marginallyclever.convenience.select.SelectBoolean;
import com.marginallyclever.convenience.select.SelectDouble;
import com.marginallyclever.convenience.select.SelectInteger;
import com.marginallyclever.convenience.select.SelectReadOnlyText;
import com.marginallyclever.makelangelo.Translator;

/**
 * Panel for {@link Generator_Spirograph}
 * @author Dan Royer
 *
 */
public class Generator_Spirograph_Panel extends NodePanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected SelectBoolean field_isEpitrochoid;
	protected SelectInteger field_majorRadius;
	protected SelectInteger field_minorRadius;
	protected SelectDouble field_pScale;
	protected SelectInteger field_numSamples;
	protected Generator_Spirograph generator;
	
	public Generator_Spirograph_Panel(Generator_Spirograph generator) {
		super();
		
		this.generator = generator;
		
		add(field_isEpitrochoid = new SelectBoolean(Translator.get("SpirographEpitrochoid"),Generator_Spirograph.getEpitrochoid()));
		add(field_majorRadius = new SelectInteger(Translator.get("SpirographMajorRadius"),Generator_Spirograph.getMajorRadius()));
		add(field_minorRadius = new SelectInteger(Translator.get("SpirographMinorRadius"),Generator_Spirograph.getMinorRadius()));
		add(field_pScale = new SelectDouble(Translator.get("SpirographPScale"),Generator_Spirograph.getPScale()));
		add(field_numSamples = new SelectInteger(Translator.get("SpirographNumSamples"),Generator_Spirograph.getNumSamples()));
		add(new SelectReadOnlyText("<a href='https://en.wikipedia.org/wiki/Spirograph'>Learn more</a>"));
		finish();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		
		Generator_Spirograph.setMajorRadius(field_majorRadius.getValue());
		Generator_Spirograph.setMinorRadius(field_minorRadius.getValue());
		Generator_Spirograph.setPScale(field_pScale.getValue());
		Generator_Spirograph.setNumSamples(field_numSamples.getValue());
		Generator_Spirograph.setEpitrochoid( field_isEpitrochoid.isSelected());
		generator.restart();
	}
}
