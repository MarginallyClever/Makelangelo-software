package com.marginallyclever.makelangelo;

import java.util.Locale;

import org.junit.Test;

public class FloatFieldTest {

	@Test
	public void testFloatFieldUS() throws Exception {
		SelectFloat field = new SelectFloat(Locale.US);
		field.setText("1000.00");
		assert(field.isEditValid());
		field.commitEdit();
		System.out.println("US text="+field.getText());
		System.out.println("US value="+field.getValue());
		float f = ((Number)field.getValue()).floatValue();
		assert(f==1000.0);
	}

	@Test
	public void testFloatFieldEU() throws Exception {
		SelectFloat field = new SelectFloat(Locale.FRANCE);
		field.setText("1000,00");
		assert(field.isEditValid());
		field.commitEdit();
		System.out.println("FR text="+field.getText());
		System.out.println("FR value="+field.getValue());
		float f = ((Number)field.getValue()).floatValue();
		assert(f==1000.0);
	}
}
