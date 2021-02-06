package com.marginallyclever.artPipeline.generators.panels;

import java.beans.PropertyChangeEvent;

import com.marginallyclever.artPipeline.TurtleNodePanel;
import com.marginallyclever.artPipeline.generators.Generator_Spirograph;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectBoolean;
import com.marginallyclever.makelangelo.select.SelectFloat;
import com.marginallyclever.makelangelo.select.SelectInteger;
import com.marginallyclever.makelangelo.select.SelectReadOnlyText;

/**
 * Panel for {@link Generator_Spirograph}
 * @author Dan Royer
 *
 */
public class Generator_Spirograph_Panel extends TurtleNodePanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected SelectBoolean field_isEpitrochoid;
	protected SelectInteger field_majorRadius;
	protected SelectInteger field_minorRadius;
	protected SelectFloat field_pScale;
	protected SelectInteger field_numSamples;
	protected Generator_Spirograph generator;
	
	public Generator_Spirograph_Panel(Generator_Spirograph generator) {
		super();
		
		this.generator = generator;
		
		add(field_isEpitrochoid = new SelectBoolean(Translator.get("SpirographEpitrochoid"),Generator_Spirograph.getEpitrochoid()));
		add(field_majorRadius = new SelectInteger(Translator.get("SpirographMajorRadius"),Generator_Spirograph.getMajorRadius()));
		add(field_minorRadius = new SelectInteger(Translator.get("SpirographMinorRadius"),Generator_Spirograph.getMinorRadius()));
		add(field_pScale = new SelectFloat(Translator.get("SpirographPScale"),Generator_Spirograph.getPScale()));
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
