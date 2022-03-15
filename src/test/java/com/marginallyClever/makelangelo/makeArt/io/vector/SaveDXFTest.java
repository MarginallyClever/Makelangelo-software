package com.marginallyClever.makelangelo.makeArt.io.vector;

import com.marginallyClever.makelangelo.Translator;
import com.marginallyClever.makelangelo.turtle.Turtle;
import com.marginallyClever.util.PreferencesHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;

import static com.marginallyClever.makelangelo.makeArt.io.vector.SaveHelper.multiColorsMoves;
import static com.marginallyClever.makelangelo.makeArt.io.vector.SaveHelper.simpleMoves;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Arrays.array;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class SaveDXFTest {

    @BeforeAll
    public static void beforeAll() {
        PreferencesHelper.start();
        Translator.start();
    }

    @Test
    void getFileNameFilter() {
        // given
        SaveDXF save = new SaveDXF();

        // then
        assertArrayEquals(array("dxf"), save.getFileNameFilter().getExtensions());
    }

    @Test
    public void saveTurtle() throws Exception {
        verifySavedFile(simpleMoves(), "/dxf/save_simple_move.dxf");
    }

    @Test
    public void saveMultiColor() throws Exception {
        verifySavedFile(multiColorsMoves(), "/dxf/save_multi_colors.dxf");
    }

    @Test
    private void verifySavedFile(Turtle turtle, String expectedFilename) throws Exception {
        // given
        File fileTemp = File.createTempFile("unit", null);

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(fileTemp);

            // when
            SaveDXF save = new SaveDXF();
            save.save(fileOutputStream, turtle);
            fileOutputStream.close();

            // then
            assertThat(fileTemp).hasSameContentAs(new File(SaveDXFTest.class.getResource(expectedFilename).toURI()));
        } finally {
            fileTemp.delete();
        }
    }
}