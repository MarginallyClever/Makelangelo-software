package com.marginallyclever.makelangelo.makeArt.io.vector;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.util.PreferencesHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;

import static com.marginallyclever.makelangelo.makeArt.io.vector.LoadHelper.readFile;
import static org.assertj.core.util.Arrays.array;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SaveSVGTest {

    @BeforeEach
    public void beforeEach() {
        PreferencesHelper.start();
        Translator.start();
    }

    @Test
    void getFileNameFilter() {
        // given
        SaveSVG save = new SaveSVG();

        // then
        assertArrayEquals(array("svg"), save.getFileNameFilter().getExtensions());
    }

    @Test
    void saveTurtle() throws Exception {
        // given
        SaveSVG save = new SaveSVG();

        File fileTemp = File.createTempFile("unit", null);

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(fileTemp);
            Turtle turtle = new Turtle();
            turtle.jumpTo(-15, -7);
            turtle.moveTo(3, 4);
            turtle.moveTo(7, 8);
            turtle.jumpTo(12, 18);

            // when
            save.save(fileOutputStream, turtle);
            fileOutputStream.close();

            // then
            String actual = readFile(fileTemp);
            String expected = readFile("/saved/expected.svg");
            assertEquals(expected, actual);
        } finally {
            fileTemp.delete();
        }
    }
}