package com.marginallyclever.makelangelo;

import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.makelangelo.select.*;

public class SelectTests {
	private static JFrame frame;
	private SelectPanel panel;
	private int testObservation; 
	
	@BeforeAll
	static public void beforeAll() {
		frame = new JFrame("Select Test");
		//2. Optional: What happens when the frame closes?
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//4. Size the frame.
		frame.pack();
		//5. Show it.
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
		SelectBoolean b = new SelectBoolean("test",true);
		assertTrue(b.isSelected());
		// test constructor works
		b = new SelectBoolean("test2",false);
		assertFalse(b.isSelected());
		
		panel.add(b);

		// test observer fires
		testObservation=0;
		b.addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
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
		SelectButton b = new SelectButton("test");
		panel.add(b);

		// test observer fires
		testObservation=0;
		b.addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				++testObservation;
			}
		});
		
		b.doClick();
		assertTrue(testObservation>0);
	}
	
	@Test
	public void testColor() {
		// test contructor(s)
		SelectColor b = new SelectColor(frame,"test",new ColorRGB(0,0,0));
		ColorRGB c = b.getColor();
		assertTrue(c.red  ==0);
		assertTrue(c.green==0);
		assertTrue(c.blue ==0);
		
		// test constructor sets value ok.
		b = new SelectColor(frame,"test2",new ColorRGB(1,2,3));
		c = b.getColor();
		assertTrue(c.red  ==1);
		assertTrue(c.green==2);
		assertTrue(c.blue ==3);
		
		panel.add(b);
		
		// test observer fires
		testObservation=0;
		b.addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
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
		SelectFile b = new SelectFile("test",null);
		assert(b.getText().isEmpty());
		b = new SelectFile("test2","something");
		assert(b.getText().contentEquals("something"));
		
		panel.add(b);

		// test observer fires
		testObservation=0;
		b.addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
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
		SelectFloat b = new SelectFloat("test",0);
		assertEquals(0.0f,b.getValue(),1e-6);
		b = new SelectFloat("test2",0.1f);
		assertEquals(0.1f,b.getValue(),1e-6);
		
		panel.add(b);
		
		// test observer fires
		testObservation=0;
		b.addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
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
		SelectInteger b = new SelectInteger("test",0);
		assertTrue(b.getValue()==0);
		b = new SelectInteger("test2",1);
		assertTrue(b.getValue()==1);
		
		panel.add(b);
		
		// test observer fires
		testObservation=0;
		b.addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
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
		SelectOneOfMany b = new SelectOneOfMany("test",list,0);
		assertTrue(b.getSelectedIndex()==0);
		assertTrue(b.getSelectedItem().contentEquals("a"));
		b = new SelectOneOfMany("test2",list,1);
		assertTrue(b.getSelectedIndex()==1);
		assertTrue(b.getSelectedItem().contentEquals("b"));
		
		panel.add(b);
		
		// test observer fires
		testObservation=0;
		b.addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
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
		SelectSlider b = new SelectSlider("test",100,0,10);
		assertTrue(b.getValue()==10);
		b = new SelectSlider("test2",100,0,20);
		assertTrue(b.getValue()==20);
		
		panel.add(b);
		
		// test observer fires
		testObservation=0;
		b.addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
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
		SelectTextArea b = new SelectTextArea("test","first test");
		assertTrue(b.getText().contentEquals("first test"));
		b = new SelectTextArea("test2","second test");
		assertTrue(b.getText().contentEquals("second test"));
		
		panel.add(b);
		
		// test observer fires
		testObservation=0;
		b.addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				++testObservation;
			}
		});

		b.setText("third test");
		assertTrue(b.getText().contentEquals("third test"));
		assertTrue(testObservation>0);
	}
}
