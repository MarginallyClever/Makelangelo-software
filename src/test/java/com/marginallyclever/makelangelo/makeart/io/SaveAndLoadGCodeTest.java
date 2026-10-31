package com.marginallyclever.makelangelo.makeart.io;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.turtletool.ReorderTurtleAction;
import com.marginallyclever.makelangelo.makeart.turtletool.SimplifyTurtleAction;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.Generator_TruchetTiles;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettingsManager;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.util.PreferencesHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;

/**
 * Confirm saving and loading GCode is lossless.
 * @author Dan Royer
 * @since 7.52.1
 */
public class SaveAndLoadGCodeTest {
    @BeforeAll
    public static void setup() {
        PreferencesHelper.start();
        Translator.start();
    }

    @Test
    public void test() throws Exception {
        Generator_TruchetTiles g = new Generator_TruchetTiles();
        g.setPaper(new Paper());
        g.addListener(generatedTurtle-> {
            try {
                saveAndLoad(generatedTurtle);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        g.generate();
    }

    @Test
    public void profileAwareFactoryLoadRestoresApplicationCoordinates() throws Exception {
        PlotterSettings settings = new PlotterSettings();
        settings.setDouble(PlotterSettings.LIMIT_LEFT, -100);
        settings.setDouble(PlotterSettings.LIMIT_RIGHT, 100);
        settings.setDouble(PlotterSettings.LIMIT_BOTTOM, -200);
        settings.setDouble(PlotterSettings.LIMIT_TOP, 200);
        settings.setInteger(PlotterSettings.STARTING_POS_INDEX, 0);
        Paper paper = new Paper();
        paper.setPaperSize(200, 400, 0, 0);

        Turtle original = new Turtle();
        original.jumpTo(10, 20);
        original.moveTo(30, 40);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new SaveGCode().save(output, original, settings, paper);

        File file = File.createTempFile("origin", ".gcode");
        try {
            Files.write(file.toPath(), output.toByteArray());
            Turtle profileFree = TurtleFactory.load(file.getAbsolutePath());
            Turtle profileAware = TurtleFactory.load(file.getAbsolutePath(), settings, paper);

            Assertions.assertEquals(original.generateHistory(), profileAware.generateHistory());
            Assertions.assertEquals(130, profileFree.getX());
            Assertions.assertEquals(-160, profileFree.getY());
            Assertions.assertEquals(original.getX(), profileAware.getX());
            Assertions.assertEquals(original.getY(), profileAware.getY());
        } finally {
            Files.deleteIfExists(file.toPath());
        }
    }

    private void saveAndLoad(Turtle original) throws Exception {
        // given
        File fileTemp = File.createTempFile("unit", null);

        try {
            SimplifyTurtleAction simplify = new SimplifyTurtleAction();
            Turtle b2 = simplify.run(original);
            ReorderTurtleAction reorder = new ReorderTurtleAction();
            Turtle before = reorder.run(b2);

            SaveGCode save = new SaveGCode();
            FileOutputStream fileOutputStream = new FileOutputStream(fileTemp);
            save.save(fileOutputStream, before, PlotterSettingsManager.buildMakelangelo5());
            fileOutputStream.close();

            LoadGCode load = new LoadGCode();
            FileInputStream input = new FileInputStream(fileTemp);
            Turtle after = load.load(input);
            input.close();

            Assertions.assertEquals(before.generateHistory(),after.generateHistory(),"Different history");
        } finally {
            fileTemp.delete();
        }
    }
}
