package com.marginallyclever.makelangelo.makeart.io;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.turtletool.TrimTurtle;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.util.PreferencesHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import static com.marginallyclever.makelangelo.makeart.io.LoadHelper.readFile;
import static com.marginallyclever.makelangelo.makeart.io.SaveHelper.multiColorsMoves;
import static com.marginallyclever.makelangelo.makeart.io.SaveHelper.simpleMoves;

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
    public void saveMultiColorOneFile() throws Exception {
        verifySavedFile(multiColorsMoves(), "/gcode/save_multi_colors.gcode");
    }

    private void verifySavedFile(Turtle turtle, String expectedFilename) throws Exception {
        // given
        SaveGCode saveGCode = new SaveGCode();

        File fileTemp = File.createTempFile("unit", null);

        try {
            Plotter plotter = new Plotter();
            PlotterSettings settings = plotter.getSettings();
            settings.load("Makelangelo 5");
            settings.setString(PlotterSettings.START_GCODE,"M300\nM200");
            settings.setString(PlotterSettings.END_GCODE,"M400\nM200");
            settings.setDouble(PlotterSettings.PEN_ANGLE_UP_TIME,50);
            settings.setDouble(PlotterSettings.PEN_ANGLE_DOWN_TIME,50);

            // when
            saveGCode.saveOneFile(fileTemp.getAbsolutePath(), turtle, plotter);
            // then
            compareExpectedToActual(expectedFilename, fileTemp);
        } finally {
            fileTemp.delete();
        }
    }

    private void compareExpectedToActual(String expectedFilename, File fileTemp) throws IOException {
        List<String> expected1 = splitAndFilterForTest(readFile(expectedFilename));
        List<String> actual1 = splitAndFilterForTest(
                new Scanner(new FileInputStream(fileTemp), StandardCharsets.UTF_8)
                        .useDelimiter("\\A")
                        .next());
        //actual.forEach(System.out::println);
        //assertIterableEquals(expected, actual);*/
        // merge contents of expected1 into a single string
        String actual = String.join("\n", actual1);
        String expected = String.join("\n", expected1);
        Assertions.assertEquals(expected,actual);
    }

    @Test
    public void saveMultiColorManyFile() throws Exception {
        // given
        Turtle turtle = multiColorsMoves();
        SaveGCode saveGCode = new SaveGCode();

        List<String> files = null;

        File fileTemp = File.createTempFile("unit", null);

        try {
            Plotter plotter = new Plotter();
            PlotterSettings settings = plotter.getSettings();
            settings.load("Makelangelo 5");
            settings.setString(PlotterSettings.START_GCODE,"M300\nM200");
            settings.setString(PlotterSettings.END_GCODE,"M400\nM200");
            settings.setDouble(PlotterSettings.PEN_ANGLE_UP_TIME,50);
            settings.setDouble(PlotterSettings.PEN_ANGLE_DOWN_TIME,50);

            // when
            files = saveGCode.saveSeparateFiles(fileTemp.getAbsolutePath(), turtle, plotter);
            // then
            compareExpectedToActual("/gcode/save_multi_colors-1.gcode", new File(files.get(0)));
            compareExpectedToActual("/gcode/save_multi_colors-2.gcode", new File(files.get(1)));
        } finally {
            fileTemp.delete();
            if (files != null) {
                for (String absolutePath : files) {
                    (new File(absolutePath)).delete();
                }
            }
        }
    }

    private List<String> splitAndFilterForTest(String fileContent) {
        return Arrays.stream(fileContent.split("\\r?\\n"))
                .filter(line -> !line.matches("; 2.* at ..:.*") && !line.matches(";Generated with.*"))
                .collect(Collectors.toList());
    }

    @Test
    public void testSaveSubsectionOfFile() throws Exception {
        // given
        Turtle before = multiColorsMoves();

        SaveGCode saveGCode = new SaveGCode();
        var after = TrimTurtle.run(before, 9, 20);

        File fileTemp = File.createTempFile("unit", ".gcode");
        Plotter plotter = new Plotter();
        PlotterSettings settings = plotter.getSettings();
        settings.setDouble(PlotterSettings.PEN_ANGLE_UP_TIME,50);
        settings.setDouble(PlotterSettings.PEN_ANGLE_DOWN_TIME,50);

        saveGCode.saveOneFile(fileTemp.getAbsolutePath(), after, plotter);
        compareExpectedToActual("/gcode/save_subsection.gcode", fileTemp);
    }
}