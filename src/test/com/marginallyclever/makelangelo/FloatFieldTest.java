package com.marginallyclever.makelangelo;

import java.util.Locale;

import org.junit.Test;

import com.marginallyclever.makelangelo.log.Log;
import com.marginallyclever.makelangelo.select.SelectFloat;

public class FloatFieldTest {

	@Test
	public void testFloatFieldUS() throws Exception {
		SelectFloat field = new SelectFloat("value",Locale.US);
		//field.setText("1000.00");
		field.setValue(2000.26f);
		//assert(field.isEditValid());
		//field.commitEdit();
		Log.message("US text="+field.getValue()+" value="+field.getValue());
		float f = field.getValue();
		assert(0==Float.compare(f, 2000.26f));
	}

	@Test
	public void testFloatFieldEU() throws Exception {
		SelectFloat field = new SelectFloat("value",Locale.FRANCE);
		//field.setText("1000,00");
		field.setValue(2000.26f);
		//assert(field.isEditValid());
		//field.commitEdit();
		Log.message("FR text="+field.getValue()+" value="+field.getValue());
		float f = field.getValue();
		assert(0==Float.compare(f, 2000.26f));
	}
}
