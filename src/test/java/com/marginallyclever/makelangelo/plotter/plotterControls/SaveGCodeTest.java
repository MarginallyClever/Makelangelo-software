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
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

class SaveGCodeTest {

    @BeforeAll
    public static void beforeEach() {
        PreferencesHelper.start();
        Translator.start();
    }

    @Test
    public void saveGCode() throws Exception {
        // given
        SaveGCode saveGCode = new SaveGCode();

        File fileTemp = File.createTempFile("unit", null);

        try {
            Turtle turtle = new Turtle();
            turtle.jumpTo(-15, -7);
            turtle.moveTo(3, 4);
            turtle.moveTo(7, 8);
            turtle.jumpTo(12, 18);

            Plotter plotter = new Plotter();

            // when
            saveGCode.save(fileTemp.getAbsolutePath(), turtle, plotter);

            // then
            List<String> expected = splitAndfilterForTest(readFile("/saved/expected.gcode"));
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