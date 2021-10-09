package com.marginallyclever.makelangelo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Locale;
import org.junit.Test;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.makelangelo.select.SelectDouble;

public class SelectDoubleTest {
	protected int testObservation;
	
	protected void testFloatField() throws Exception {
		// test contructor(s)
		SelectDouble b = new SelectDouble("test","test",0);
		assertEquals(0.0f,b.getValue(),1e-6);
		b = new SelectDouble("test2","test2",0.1f);
		assertEquals(0.1f,b.getValue(),1e-6);
		
		// test observer fires
		testObservation=0;
		b.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				++testObservation;
			}
		});
		
		b.setValue(2000.34f);
		Log.message("text="+b.getText()+" value="+b.getValue());
		assertTrue(testObservation>0);
		assertEquals(2000.34f,b.getValue(),1e-6);	
	}
	
	@Test
	public void testAllFloatFields() throws Exception {
		Log.message("testAllFloatFields() start");
		Locale original = Locale.getDefault();
		Locale [] list = Locale.getAvailableLocales();
		
		for( Locale loc : list ) {
			Log.message("Locale="+loc.toString()+" "+loc.getDisplayLanguage());
			Locale.setDefault(loc);
			testFloatField();
		}
		Locale.setDefault(original);
		Log.message("testAllFloatFields() end");
	}
}
