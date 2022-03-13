package com.marginallyclever.makelangelo.makeArt.io.vector;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.util.PreferencesHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;

import static com.marginallyclever.makelangelo.makeArt.io.vector.SaveHelper.multiColorsMoves;
import static com.marginallyclever.makelangelo.makeArt.io.vector.SaveHelper.simpleMoves;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Arrays.array;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class SaveSVGTest {

    @BeforeAll
    public static void beforeAll() {
        PreferencesHelper.start();
        Translator.start();
    }

    @Test
    public void getFileNameFilter() {
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

            // when
            save.save(fileOutputStream, simpleMoves());
            fileOutputStream.close();

            // then
            assertThat(fileTemp).hasSameContentAs(new File(SaveDXFTest.class.getResource("/svg/save_simple_move.svg").toURI()));
        } finally {
            fileTemp.delete();
        }
    }

    @Test
    void saveMultiColor() throws Exception {
        // given
        SaveSVG save = new SaveSVG();

        File fileTemp = File.createTempFile("unit", null);

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(fileTemp);

            // when
            save.save(fileOutputStream, multiColorsMoves());
            fileOutputStream.close();

            // then
            assertThat(fileTemp).hasSameContentAs(new File(SaveDXFTest.class.getResource("/svg/save_multi_colors.svg").toURI()));
        } finally {
            fileTemp.delete();
        }
    }
}