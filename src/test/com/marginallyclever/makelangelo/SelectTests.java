package com.marginallyclever.makelangelo;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JFrame;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.makelangelo.select.*;

public class SelectTests {
	private static JFrame frame;
	private SelectPanel panel;
	private int testObservation; 
	
	@BeforeAll
	static public void beforeAll() {
		frame = new JFrame("Select Test");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
	
	@AfterAll
	static public void afterAll() {
		frame.dispose();
	}
	
	@BeforeEach
	public void beforeEach() {
		panel = new SelectPanel();
		frame.getContentPane().add(panel.getPanel());
	}
	
	@AfterEach
	public void afterEach() {
		frame.getContentPane().removeAll();
	}
	
	@Test
	public void testBoolean() {
		// test contructor(s)
		SelectBoolean b = new SelectBoolean("test","test",true);
		assertTrue(b.isSelected());
		// test constructor works
		b = new SelectBoolean("test2","test2",false);
		assertFalse(b.isSelected());
		
		panel.add(b);

		// test observer fires
		testObservation=0;
		b.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				++testObservation;
				
			}
		});
		
		b.setSelected(true);
		assertTrue(b.isSelected());
		assertTrue(testObservation>0);
		testObservation=0;
		b.setSelected(false);
		assertFalse(b.isSelected());
		assertTrue(testObservation>0);
	}
	
	@Test
	public void testButton() {
		// test contructor(s)
		SelectButton b = new SelectButton("test","test");
		panel.add(b);

		// test observer fires
		testObservation=0;
		b.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				++testObservation;
			}
		});
		
		b.doClick();
		assertTrue(testObservation>0);
	}
	
	@Test
	public void testColor() {
		// test contructor(s)
		SelectColor b = new SelectColor("test","test",new ColorRGB(0,0,0),frame);
		ColorRGB c = b.getColor();
		assertTrue(c.red  ==0);
		assertTrue(c.green==0);
		assertTrue(c.blue ==0);
		
		// test constructor sets value ok.
		b = new SelectColor("test2","test2",new ColorRGB(1,2,3),frame);
		c = b.getColor();
		assertTrue(c.red  ==1);
		assertTrue(c.green==2);
		assertTrue(c.blue ==3);
		
		panel.add(b);
		
		// test observer fires
		testObservation=0;
		b.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				++testObservation;
			}
		});
		
		// test setValue
		b.setColor(new ColorRGB(255,128,64));
		c = b.getColor();
		assertTrue(c.red  ==255);
		assertTrue(c.green==128);
		assertTrue(c.blue == 64);
		
		assertTrue(testObservation>0);
	}
	
	@Test
	public void testFile() {
		// test contructor(s)
		SelectFile b = new SelectFile("test","test",null);
		assert(b.getText().isEmpty());
		b = new SelectFile("test2","test2","something");
		assert(b.getText().contentEquals("something"));
		
		panel.add(b);

		// test observer fires
		testObservation=0;
		b.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				++testObservation;
			}
		});

		// test setText
		b.setText("some path");
		assertTrue(testObservation>0);
	}
	
	@Test
	public void testFloat() {
		// test contructor(s)
		SelectDouble b = new SelectDouble("test","test",0);
		assertEquals(0.0f,b.getValue(),1e-6);
		b = new SelectDouble("test2","test2",0.1f);
		assertEquals(0.1f,b.getValue(),1e-6);
		
		panel.add(b);
		
		// test observer fires
		testObservation=0;
		b.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				++testObservation;
			}
		});
		
		b.setValue(0.2f);
		assertTrue(testObservation>0);
		assertEquals(0.2f,b.getValue(),1e-6);	
	}
	
	@Test
	public void testInteger() {
		// test contructor(s)
		SelectInteger b = new SelectInteger("test","test",0);
		assertTrue(b.getValue()==0);
		b = new SelectInteger("test2","test2",1);
		assertTrue(b.getValue()==1);
		
		panel.add(b);
		
		// test observer fires
		testObservation=0;
		b.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				++testObservation;
			}
		});
		
		b.setValue(2);
		assertTrue(testObservation>0);
		assertTrue(b.getValue()==2);	
	}
	
	@Test
	public void testOneOfMany() {
		String [] list = {"a","b","c","d"};
		
		// test contructor(s)
		SelectOneOfMany b = new SelectOneOfMany("test","test",list,0);
		assertTrue(b.getSelectedIndex()==0);
		assertTrue(b.getSelectedItem().contentEquals("a"));
		b = new SelectOneOfMany("test2","test2",list,1);
		assertTrue(b.getSelectedIndex()==1);
		assertTrue(b.getSelectedItem().contentEquals("b"));
		
		panel.add(b);
		
		// test observer fires
		testObservation=0;
		b.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				++testObservation;
			}
		});
		
		b.setSelectedIndex(2);
		assertTrue(testObservation>0);
		assertTrue(b.getSelectedIndex()==2);	
		assertTrue(b.getSelectedItem().contentEquals("c"));	
	}
	
	@Test
	public void testSlider() {
		// test contructor(s)
		SelectSlider b = new SelectSlider("test","test",100,0,10);
		assertTrue(b.getValue()==10);
		b = new SelectSlider("test2","test2",100,0,20);
		assertTrue(b.getValue()==20);
		
		panel.add(b);
		
		// test observer fires
		testObservation=0;
		b.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				++testObservation;
			}
		});

		b.setValue(30);
		assertTrue(b.getValue()==30);
		assertTrue(testObservation>0);
		b.setValue(110);	assertFalse(b.getValue()==110);
		b.setValue(-10);	assertFalse(b.getValue()==-10);
	}
	
	@Test
	public void testTextArea() {
		// test contructor(s)
		SelectTextArea b = new SelectTextArea("test","test","first test");
		assertTrue(b.getText().contentEquals("first test"));
		b = new SelectTextArea("test2","test2","second test");
		assertTrue(b.getText().contentEquals("second test"));
		
		panel.add(b);
		
		// test observer fires
		testObservation=0;
		b.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				++testObservation;
			}
		});

		b.setText("third test");
		assertTrue(b.getText().contentEquals("third test"));
		assertTrue(testObservation>0);
	}
}
