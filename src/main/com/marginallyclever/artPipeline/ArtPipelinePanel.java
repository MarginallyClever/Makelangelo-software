package com.marginallyclever.artPipeline;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.marginallyclever.makelangelo.CollapsiblePanel;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.util.PreferencesHelper;

public class ArtPipelinePanel extends CollapsiblePanel { 
	/**
	 * 
	 */
	private static final long serialVersionUID = 7525669710478140175L;

	@SuppressWarnings("deprecation")
	transient private Preferences prefs = PreferencesHelper
			.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.LEGACY_MAKELANGELO_ROOT);

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
	
	transient protected JFrame parentFrame;
	transient public ArtPipeline myPipeline;
	
	protected JComboBox<String> resizeOptionsComboBox;
	protected JComboBox<String> flipOptionsComboBox;
	protected JCheckBox shouldReorderCheckbox;
	protected JCheckBox shouldSimplifyCheckbox;
	protected JCheckBox shouldCropCheckbox;

	
	
	public ArtPipelinePanel(JFrame parentFrame0) {
		super(Translator.get("Art Pipeline"));

		parentFrame=parentFrame0;
		
		// create and arrange the elements of this panel.
		
		//if(shouldResizeFill()) checkResizeFill(turtle,settings);
		//if(shouldResizeFit()) checkResizeFit(turtle,settings);
		// TODO translate these strings
		String[] resizeOptions = new String[3];
		resizeOptions[0] = Translator.get("ConvertImagePaperOriginal");
		resizeOptions[1] = Translator.get("ConvertImagePaperFit");
		resizeOptions[2] = Translator.get("ConvertImagePaperFill");
		resizeOptionsComboBox = new JComboBox<String>(resizeOptions);
		resizeOptionsComboBox.setSelectedIndex(getPreferredFillStyle());

		String[] flipOptions = new String[3];
		flipOptions[1] = Translator.get("FlipNone");
		flipOptions[1] = Translator.get("FlipH");
		flipOptions[2] = Translator.get("FlipV");
		flipOptionsComboBox = new JComboBox<String>(flipOptions);
		flipOptionsComboBox.setSelectedIndex(getPreferredFlipStyle());
		
		//if(shouldReorder()) checkReorder(turtle,settings);
		shouldReorderCheckbox = new JCheckBox("Reorder");
		//if(shouldSimplify()) checkSimplify(turtle,settings);
		shouldSimplifyCheckbox = new JCheckBox("Simplify");
		//if(shouldCrop()) cropTurtleToPageMargin(turtle,settings);
		shouldCropCheckbox = new JCheckBox("Crop to margins");
		
		JPanel panel = getContentPane();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.add(resizeOptionsComboBox);
		panel.add(flipOptionsComboBox);
		panel.add(shouldReorderCheckbox);
		panel.add(shouldSimplifyCheckbox);
		panel.add(shouldCropCheckbox);
		
		resizeOptionsComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				setPreferredResizeStyle(resizeOptionsComboBox.getSelectedIndex());
				myPipeline.processTurtle(null,null);
			}
		});
		flipOptionsComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				setPreferredFlipStyle(flipOptionsComboBox.getSelectedIndex());
			}
		});
		shouldReorderCheckbox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				myPipeline.processTurtle(null,null);
			}
		});
		shouldSimplifyCheckbox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				myPipeline.processTurtle(null,null);
			}
		});
		shouldCropCheckbox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				myPipeline.processTurtle(null,null);
			}
		});
	}
	
	public boolean shouldResizeFill() {
		return resizeOptionsComboBox.getSelectedIndex()==2;
	}
	public boolean shouldResizeFit() {
		return resizeOptionsComboBox.getSelectedIndex()==1;
	}
	public boolean shouldReorder() {
		return shouldReorderCheckbox.isSelected();
	}
	public boolean shouldFlipV() {
		return flipOptionsComboBox.getSelectedIndex()==2;
	}
	public boolean shouldFlipH() {
		return flipOptionsComboBox.getSelectedIndex()==1;
	}
	public boolean shouldSimplify() {
		return shouldSimplifyCheckbox.isSelected();
	}
	public boolean shouldCrop() {
		return shouldCropCheckbox.isSelected();
	}

	public void setPipeline(ArtPipeline pipeline) {
		myPipeline = pipeline;
		if(myPipeline!=null) {
			myPipeline.myPanel = this;
		}
	}
}
