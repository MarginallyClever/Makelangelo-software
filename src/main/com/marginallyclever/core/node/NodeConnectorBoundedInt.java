package com.marginallyclever.core.node;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.marginallyclever.core.select.Select;
import com.marginallyclever.core.select.SelectSlider;

/**
 * Convenience class.
 * @author Dan Royer
 * @since 7.25.0
 */
public class NodeConnectorBoundedInt extends NodeConnectorInt {
	private int rangeTop;
	private int rangeBottom;
	
	
	public NodeConnectorBoundedInt(String newName) {
		super(newName);
	}
	
	public NodeConnectorBoundedInt(String newName,Integer d) {
		super(newName,d);
	}
	
	public NodeConnectorBoundedInt(String newName,int top,int bottom,Integer d) {
		super(newName,d);
		rangeTop=top;
		rangeBottom=bottom;
	}

	@Override
	public Select getSelect() {
		SelectSlider s = new SelectSlider(this.getName(),rangeTop,rangeBottom,this.getValue());
		s.addPropertyChangeListener(new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				setValue((int)evt.getNewValue());
			}
		});
		return s;
	}
	
	public int getRangeTop() {
		return rangeTop;
	}

	public void setRangeTop(int rangeTop) {
		this.rangeTop = rangeTop;
	}

	public int getRangeBottom() {
		return rangeBottom;
	}

	public void setRangeBottom(int rangeBottom) {
		this.rangeBottom = rangeBottom;
	}
}
