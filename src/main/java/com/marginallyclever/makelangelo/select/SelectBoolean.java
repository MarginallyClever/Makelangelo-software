package com.marginallyclever.makelangelo.select;

import java.awt.BorderLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

/**
 * A JCheckBox that sets itself up to format true/false. 
 * @author Dan Royer
 * @since 7.24.0
 */
public class SelectBoolean extends Select {
	/**
	 * 
	 */
	private static final long serialVersionUID = 598928132833398652L;
	private JLabel label;
	private JCheckBox field;
	
	public SelectBoolean(String internalName,String labelKey,boolean arg0) {
		super(internalName);
		
		label = new JLabel(labelKey,JLabel.LEADING);
		
		field = new JCheckBox();
		field.setSelected(arg0);
		field.setBorder(new EmptyBorder(0,0,0,0));
		field.addItemListener((e)-> {
			boolean newValue = field.isSelected();
			boolean oldValue = !newValue;
			notifyPropertyChangeListeners(oldValue, newValue);
		});

		this.add(label,BorderLayout.LINE_START);
		this.add(field,BorderLayout.LINE_END);
	}
	
	public boolean isSelected() {
		return field.isSelected();
	}

	/**
	 * This is to discuss ... 
	 * Maybe on some JRE this have to be done.
	 * But for the implementation i have done,
	 * a doClick() will make an infinit events call loop.
	 * 
	 */
	static boolean DEV_IF_NO_PPAC_CHANGE_DO_CLICK_CREATE_A_LOOP = true;
	
	public void setSelected(boolean b) {	    
	    if ( !DEV_IF_NO_PPAC_CHANGE_DO_CLICK_CREATE_A_LOOP ){
		// calling setSelected() does not fire the itemListener, which means the observer would not fire.
		if(field.isSelected()!=b) {
			// causes the observer to fire.
			field.doClick();//PPAC37 this cause my some events inifint loop for Papper Setting ( check box and open the comboBox = loop !!!!! 
		}
	    }else{
		//This work for me ... 
		field.setSelected(b);
	    }
	}
}
