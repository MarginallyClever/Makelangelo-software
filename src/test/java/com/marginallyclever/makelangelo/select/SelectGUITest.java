package com.marginallyclever.makelangelo.select;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

public class SelectGUITest {
    private SelectPanel panel;
    private int testObservation;

    @BeforeEach
    public void beforeEach() {
        panel = new SelectPanel();
    }

    @Test
    public void testBoolean() {
        // test contructor(s)
        SelectBoolean b = new SelectBoolean("test", "test", true);
        assertTrue(b.isSelected());
        // test constructor works
        b = new SelectBoolean("test2", "test2", false);
        assertFalse(b.isSelected());

        panel.add(b);

        // test observer fires
        testObservation = 0;
        b.addSelectListener(evt -> ++testObservation);

        b.setSelected(true);
        assertTrue(b.isSelected());
        assertTrue(testObservation > 0);
        testObservation = 0;
        b.setSelected(false);
        assertFalse(b.isSelected());
        assertTrue(testObservation > 0);
    }

    @Test
    public void testButton() {
        // test contructor(s)
        SelectButton b = new SelectButton("test", "test");
        panel.add(b);

        // test observer fires
        testObservation = 0;
        b.addActionListener(evt -> ++testObservation);

        b.doClick();
        assertTrue(testObservation > 0);
    }

    @Test
    public void testColor() {
        // test contructor(s)
        SelectColor b = new SelectColor("test", "test", new Color(0, 0, 0), panel);
        Color c = b.getColor();
        assertEquals(0, c.getRed());
        assertEquals(0, c.getGreen());
        assertEquals(0, c.getBlue());

        // test constructor sets value ok.
        b = new SelectColor("test2", "test2", new Color(1, 2, 3), panel);
        c = b.getColor();
        assertEquals(1, c.getRed());
        assertEquals(2, c.getGreen());
        assertEquals(3, c.getBlue());

        panel.add(b);
        // test setValue
        b.setColor(new Color(255, 128, 64));
        c = b.getColor();
        assertEquals(255, c.getRed());
        assertEquals(128, c.getGreen());
        assertEquals(64 , c.getBlue());
    }

    @Test
    public void testFile() {
        // test contructor(s)
        SelectFile b = new SelectFile("test", "test", null);
        assertTrue(b.getText().isEmpty());
        b = new SelectFile("test2", "test2", "something");
        assertEquals("something", b.getText());

        panel.add(b);

        // test setText
        b.setText("some path");
    }

    @Test
    public void testFloat() {
        // test contructor(s)
        SelectDouble b = new SelectDouble("test", "test", 0);
        assertEquals(0.0f, b.getValue(), 1e-6);
        b = new SelectDouble("test2", "test2", 0.1f);
        assertEquals(0.1f, b.getValue(), 1e-6);

        panel.add(b);

        b.setValue(0.2f);
        assertEquals(0.2f, b.getValue(), 1e-6);
    }

    @Test
    public void testInteger() {
        // test contructor(s)
        SelectInteger b = new SelectInteger("test", "test", 0);
        assertEquals(0, b.getValue());
        b = new SelectInteger("test2", "test2", 1);
        assertEquals(1, b.getValue());

        panel.add(b);

        b.setValue(2);
        assertEquals(2, b.getValue());
    }

    @Test
    public void testOneOfMany() {
        String[] list = {"a", "b", "c", "d"};

        // test contructor(s)
        SelectOneOfMany b = new SelectOneOfMany("test", "test", list, 0);
        assertEquals(0, b.getSelectedIndex());
        assertEquals("a", b.getSelectedItem());
        b = new SelectOneOfMany("test2", "test2", list, 1);
        assertEquals(1, b.getSelectedIndex());
        assertEquals("b", b.getSelectedItem());

        panel.add(b);

        // test observer fires
        testObservation = 0;
        b.addSelectListener(evt -> ++testObservation);

        b.setSelectedIndex(2);
        assertTrue(testObservation > 0);
        assertEquals(2, b.getSelectedIndex());
        assertEquals("c", b.getSelectedItem());
    }

    @Test
    public void testSlider() {
        // test contructor(s)
        SelectSlider b = new SelectSlider("test", "test", 100, 0, 10);
        assertEquals(10, b.getValue());
        b = new SelectSlider("test2", "test2", 100, 0, 20);
        assertEquals(20, b.getValue());

        panel.add(b);

        // test observer fires
        testObservation = 0;
        b.addSelectListener(evt -> ++testObservation);

        b.setValue(30);
        assertEquals(30, b.getValue());
        assertTrue(testObservation > 0);
        b.setValue(110);
        assertNotEquals(110, b.getValue());
        b.setValue(-10);
        assertNotEquals(-10, b.getValue());
    }

    @Test
    public void testTextArea() {
        // test contructor(s)
        SelectTextArea b = new SelectTextArea("test", "test", "first test");
        assertEquals("first test", b.getText());
        b = new SelectTextArea("test2", "test2", "second test");
        assertEquals("second test", b.getText());

        panel.add(b);

        // test observer fires
        testObservation = 0;
        b.addSelectListener(evt -> ++testObservation);

        b.setText("third test");
        assertEquals("third test", b.getText());
        assertTrue(testObservation > 0);
    }
}
