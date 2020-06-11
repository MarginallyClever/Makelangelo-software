package com.marginallyclever.artPipeline.generators;

import java.util.Observable;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectBoolean;
import com.marginallyclever.makelangelo.select.SelectFloat;
import com.marginallyclever.makelangelo.select.SelectInteger;
import com.marginallyclever.makelangelo.select.SelectReadOnlyText;

public class Generator_Spirograph_Panel extends ImageGeneratorPanel {
	protected SelectBoolean field_isEpitrochoid;
	protected SelectInteger field_majorRadius;
	protected SelectInteger field_minorRadius;
	protected SelectFloat field_pScale;
	protected SelectInteger field_numSamples;
	protected Generator_Spirograph generator;
	
	Generator_Spirograph_Panel(Generator_Spirograph generator) {
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
	public void update(Observable o, Object arg) {
		super.update(o, arg);
		
		int newMajorRadius = field_majorRadius.getValue();
		int newMinorRadius = field_minorRadius.getValue();
		float newPScale = field_pScale.getValue();
		int newNumSamples = field_numSamples.getValue();
		boolean newEpitrochoid = field_isEpitrochoid.isSelected();

		if(newMajorRadius != Generator_Spirograph.getMajorRadius() ||
			newMinorRadius != Generator_Spirograph.getMinorRadius() ||
			newPScale != Generator_Spirograph.getPScale() ||
			newNumSamples != Generator_Spirograph.getNumSamples() ||
			newEpitrochoid != Generator_Spirograph.getEpitrochoid() ) {
			Generator_Spirograph.setMajorRadius(newMajorRadius);
			Generator_Spirograph.setMinorRadius(newMinorRadius);
			Generator_Spirograph.setPScale(newPScale);
			Generator_Spirograph.setNumSamples(newNumSamples);
			Generator_Spirograph.setEpitrochoid(newEpitrochoid);
			makelangeloRobotPanel.regenerate(generator);
		}
	}
}
