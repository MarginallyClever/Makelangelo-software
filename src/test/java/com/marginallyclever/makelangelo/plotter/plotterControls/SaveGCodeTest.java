package com.marginallyclever.makelangelo.plotter.plotterControls;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.util.PreferencesHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import static com.marginallyclever.makelangelo.makeArt.io.vector.LoadHelper.readFile;
import static com.marginallyclever.makelangelo.makeArt.io.vector.SaveHelper.multiColorsMoves;
import static com.marginallyclever.makelangelo.makeArt.io.vector.SaveHelper.simpleMoves;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

class SaveGCodeTest {

    @BeforeAll
    public static void beforeAll() {
        PreferencesHelper.start();
        Translator.start();
    }

    @Test
    public void saveTurtle() throws Exception {
        verifySavedFile(simpleMoves(), "/gcode/save_simple_move.gcode");
    }

    @Test
    public void saveMultiColor() throws Exception {
        verifySavedFile(multiColorsMoves(), "/gcode/save_multi_colors.gcode");
    }

    @Test
    private void verifySavedFile(Turtle turtle, String expectedFilename) throws Exception {
        // given
        SaveGCode saveGCode = new SaveGCode();

        File fileTemp = File.createTempFile("unit", null);

        try {
            Plotter plotter = new Plotter();

            // when
            saveGCode.save(fileTemp.getAbsolutePath(), turtle, plotter);

            // then
            List<String> expected = splitAndfilterForTest(readFile(expectedFilename));
            List<String> actual = splitAndfilterForTest(
                    new Scanner(new FileInputStream(fileTemp), StandardCharsets.UTF_8)
                            .useDelimiter("\\A")
                            .next());

            assertIterableEquals(expected, actual);
        } finally {
            fileTemp.delete();
        }
    }

    private List<String> splitAndfilterForTest(String fileContent) {
        return Arrays.stream(fileContent.split("\\r?\\n"))
                .filter(line -> !line.matches("; 20.* at ..:.*") && !line.matches("Generated with.*"))
                .collect(Collectors.toList());
    }
}