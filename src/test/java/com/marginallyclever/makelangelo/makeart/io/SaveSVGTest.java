package com.marginallyclever.makelangelo.makeart.io;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettingsManager;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.util.PreferencesHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.Objects;

import static com.marginallyclever.makelangelo.makeart.io.SaveHelper.multiColorsMoves;
import static com.marginallyclever.makelangelo.makeart.io.SaveHelper.simpleMoves;
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
    public void saveTurtle() throws Exception {
        verifySavedFile(simpleMoves(), "/svg/save_simple_move.svg");
    }

    @Test
    public void saveMultiColor() throws Exception {
        verifySavedFile(multiColorsMoves(), "/svg/save_multi_colors.svg");
    }

    private void verifySavedFile(Turtle turtle, String expectedFilename) throws Exception {
        // given
        File fileTemp = File.createTempFile("unit", null);

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(fileTemp);

            // when
            SaveSVG save = new SaveSVG();
            save.save(fileOutputStream, turtle, PlotterSettingsManager.buildMakelangelo5());
            fileOutputStream.close();

            // compare content of both files
            var resource = Objects.requireNonNull(getClass().getResource(expectedFilename)).toURI();
            Assertions.assertEquals(Files.readString(fileTemp.toPath()).replace("\n", "\r\n"),
                    Files.readString(new File(resource).toPath()),
                    "mismatch with "+expectedFilename);
        }
        finally {
            fileTemp.delete();
        }
    }
}