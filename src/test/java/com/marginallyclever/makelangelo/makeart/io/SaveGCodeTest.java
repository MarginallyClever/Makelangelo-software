package com.marginallyclever.makelangelo.makeart.io;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.util.PreferencesHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import static com.marginallyclever.makelangelo.makeart.io.LoadHelper.readFile;
import static com.marginallyclever.makelangelo.makeart.io.SaveHelper.multiColorsMoves;
import static com.marginallyclever.makelangelo.makeart.io.SaveHelper.simpleMoves;
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
    public void saveMultiColorOneFile() throws Exception {
        verifySavedFile(multiColorsMoves(), "/gcode/save_multi_colors.gcode");
    }

    @Test
    private void verifySavedFile(Turtle turtle, String expectedFilename) throws Exception {
        // given
        SaveGCode saveGCode = new SaveGCode();

        File fileTemp = File.createTempFile("unit", null);

        try {
            Plotter plotter = new Plotter();
            PlotterSettings settings = plotter.getSettings();
            settings.load("Makelangelo 5");
            settings.setString(PlotterSettings.USER_GENERAL_START_GCODE,"M300\nM200");
            settings.setString(PlotterSettings.USER_GENERAL_END_GCODE,"M400\nM200");
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

    private void compareExpectedToActual(String expectedFilename, File fileTemp) throws FileNotFoundException {
        List<String> expected = splitAndFilterForTest(readFile(expectedFilename));
        List<String> actual = splitAndFilterForTest(
                new Scanner(new FileInputStream(fileTemp), StandardCharsets.UTF_8)
                        .useDelimiter("\\A")
                        .next());
        actual.forEach(System.out::println);
        assertIterableEquals(expected, actual);
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
            settings.setString(PlotterSettings.USER_GENERAL_START_GCODE,"M300\nM200");
            settings.setString(PlotterSettings.USER_GENERAL_END_GCODE,"M400\nM200");
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
                .filter(line -> !line.matches("; 20.* at ..:.*") && !line.matches(";Generated with.*"))
                .collect(Collectors.toList());
    }

    @Test
    public void testSaveSubsectionOfFile() throws Exception {
        // given
        Turtle turtle = multiColorsMoves();

        SaveGCode saveGCode = new SaveGCode();
        turtle = saveGCode.trimTurtle(turtle, 10, 20);

        File fileTemp = File.createTempFile("unit", null);
        Plotter plotter = new Plotter();
        PlotterSettings settings = plotter.getSettings();
        settings.setDouble(PlotterSettings.PEN_ANGLE_UP_TIME,50);
        settings.setDouble(PlotterSettings.PEN_ANGLE_DOWN_TIME,50);

        saveGCode.saveOneFile(fileTemp.getAbsolutePath(), turtle, plotter);
        compareExpectedToActual("/gcode/save_subsection.gcode", fileTemp);
    }
}