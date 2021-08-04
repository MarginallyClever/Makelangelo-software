package com.marginallyclever.artPipeline;

import java.awt.Dimension;
import java.util.prefs.Preferences;

import javax.swing.JFrame;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.makelangelo.CollapsiblePanel;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectBoolean;
import com.marginallyclever.makelangelo.select.SelectOneOfMany;
import com.marginallyclever.makelangelo.select.SelectPanel;
import com.marginallyclever.util.PreferencesHelper;

/**
 * @author Dan Royer
 */
public class ArtPipelinePanel extends CollapsiblePanel { 
	/**
	 * 
	 */
	private static final long serialVersionUID = 7525669710478140175L;
	
	transient protected JFrame parentFrame;
	transient public ArtPipeline myPipeline;
	
	protected SelectOneOfMany resize;
	protected SelectOneOfMany flip;
	protected SelectBoolean reorder;
	protected SelectBoolean simplify;
	protected SelectBoolean crop;
	
	@SuppressWarnings("deprecation")
	transient private Preferences prefs = PreferencesHelper
			.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.LEGACY_MAKELANGELO_ROOT);

	public ArtPipelinePanel(JFrame parentFrame0,ArtPipeline pipeline) {
		super(Translator.get("Art Pipeline"));

		parentFrame=parentFrame0;
		myPipeline = pipeline;
		
		// create and arrange the elements of this panel.
		
		//if(shouldResizeFill()) checkResizeFill(turtle,settings);
		//if(shouldResizeFit()) checkResizeFit(turtle,settings);
		String[] resizeOptions = {
				Translator.get("ConvertImagePaperOriginal"),
				Translator.get("ConvertImagePaperFit"),
				Translator.get("ConvertImagePaperFill")
		};
		resize = new SelectOneOfMany(Translator.get("Resize"),resizeOptions,getPreferredFillStyle());
		
		String[] flipOptions = {
			Translator.get("FlipNone"),
			Translator.get("FlipH"),
			Translator.get("FlipV"),
			Translator.get("FlipH") +" + "+ Translator.get("FlipV")
		};
		flip = new SelectOneOfMany(Translator.get("Flip"),flipOptions,getPreferredFlipStyle());
		
		//if(shouldReorder()) checkReorder(turtle,settings);
		reorder = new SelectBoolean(Translator.get("Reorder"),true);
		
		//if(shouldSimplify()) checkSimplify(turtle,settings);
		simplify = new SelectBoolean(Translator.get("Simplify"),true);
		
		//if(shouldCrop()) cropTurtleToPageMargin(turtle,settings);
		crop = new SelectBoolean(Translator.get("Crop to margins"),true);
		
		SelectPanel panel = getContentPane();
		panel.add(resize);
		panel.add(flip);
		panel.add(reorder);
		panel.add(simplify);
		panel.add(crop);
		panel.invalidate();
		
		resize.addPropertyChangeListener((evt)-> {
			setPreferredResizeStyle(resize.getSelectedIndex());
			myPipeline.reprocessTurtle();
		});
		flip.addPropertyChangeListener((evt)-> {
			setPreferredFlipStyle(flip.getSelectedIndex());
			myPipeline.reprocessTurtle();
		});

		reorder.addPropertyChangeListener((evt)-> {
			myPipeline.setShouldReorder(reorder.isSelected());
			myPipeline.reprocessTurtle();
		});
		simplify.addPropertyChangeListener((evt)-> {
			myPipeline.setShouldSimplify(simplify.isSelected());
			myPipeline.reprocessTurtle();
		});
		crop.addPropertyChangeListener((evt)-> {
			myPipeline.setShouldCrop(crop.isSelected());
			myPipeline.reprocessTurtle();
		});
	}
	
	private void setPreferredResizeStyle(int style) {
		prefs.putInt("Fill Style", style);
	}
	
	private int getPreferredFillStyle() {
		return prefs.getInt("Fill Style", 0);
	}
	
	private void setPreferredFlipStyle(int style) {
		prefs.putInt("Flip Style", style);
	}
	
	private int getPreferredFlipStyle() {
		return prefs.getInt("Flip Style", 0);
	}
	
	public void setPipeline(ArtPipeline pipeline) {
	}
	
	/**
	 * Run this to visually examine every panel element and how they look in next to each other.
	 * @param args ignored
	 */
	public static void main(String[] args) {
		Log.start();
		Translator.start();
		JFrame frame = new JFrame("Art Pipeline Panel");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		ArtPipelinePanel p = new ArtPipelinePanel(frame,new ArtPipeline());
		p.getContentPane().getPanel().setPreferredSize(new Dimension(400,600));
		frame.add(p);
		
		frame.pack();
		frame.setVisible(true);
	} 
}
