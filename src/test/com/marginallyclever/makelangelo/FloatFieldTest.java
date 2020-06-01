package com.marginallyclever.makelangelo;

import java.util.Locale;

import org.junit.Test;

import com.marginallyclever.makelangelo.log.Log;
import com.marginallyclever.makelangelo.select.SelectFloat;

public class FloatFieldTest {

	@Test
	public void testFloatFieldUS() throws Exception {
		SelectFloat field = new SelectFloat(Locale.US);
		field.setText("1000.00");
		assert(field.isEditValid());
		field.commitEdit();
		Log.message("US text="+field.getText());
		Log.message("US value="+field.getValue());
		float f = ((Number)field.getValue()).floatValue();
		assert(f==1000.0);
	}

	@Test
	public void testFloatFieldEU() throws Exception {
		SelectFloat field = new SelectFloat(Locale.FRANCE);
		field.setText("1000,00");
		assert(field.isEditValid());
		field.commitEdit();
		Log.message("FR text="+field.getText());
		Log.message("FR value="+field.getValue());
		float f = ((Number)field.getValue()).floatValue();
		assert(f==1000.0);
	}
}
