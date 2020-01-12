package com.marginallyclever.artPipeline;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.marginallyclever.makelangelo.CollapsiblePanel;
import com.marginallyclever.makelangelo.Translator;

public class ArtPipelinePanel extends CollapsiblePanel implements ActionListener, ItemListener { 
	/**
	 * 
	 */
	private static final long serialVersionUID = 7525669710478140175L;

	transient protected JFrame parentFrame;
	transient public ArtPipeline myPipeline;
	
	private final String [] resizeOptions = { "Don't resize", "Fit inside the margins","Fill the margins" };
	protected JComboBox<String> resizeOptionsComboBox;
	// TODO save this in a preference
	static int resizeOptionsComboBoxIndex=0;

	protected JCheckBox shouldReorderCheckbox;
	protected JCheckBox shouldSimplifyCheckbox;
	protected JCheckBox shouldCropCheckbox;

	private final String [] flipOptions = { "Don't flip", "Flip horizontal","Flip vertical" };
	protected JComboBox<String> flipOptionsComboBox;
	// TODO save this in a preference
	static int flipOptionsComboBoxIndex=0;
	
	
	public ArtPipelinePanel(JFrame parentFrame0) {
		super(Translator.get("Art Pipeline"));

		parentFrame=parentFrame0;
		
		// create and arrange the elements of this panel.
		
		//if(shouldResizeFill()) checkResizeFill(turtle,settings);
		//if(shouldResizeFit()) checkResizeFit(turtle,settings);
		// TODO translate these strings
		resizeOptionsComboBox = new JComboBox<String>(resizeOptions);
		resizeOptionsComboBox.setSelectedIndex(resizeOptionsComboBoxIndex);

		flipOptionsComboBox = new JComboBox<String>(flipOptions);
		flipOptionsComboBox.setSelectedIndex(flipOptionsComboBoxIndex);
		
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
	}
	
	@Override
	public void itemStateChanged(ItemEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
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
