package com.marginallyclever.makelangelo.makeart.io;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettingsManager;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.util.PreferencesHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import static com.marginallyclever.makelangelo.makeart.io.SaveHelper.multiColorsMoves;
import static com.marginallyclever.makelangelo.makeart.io.SaveHelper.simpleMoves;
import static org.assertj.core.util.Arrays.array;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SaveBitmapTest {
    @BeforeAll
    public static void beforeAll() {
        PreferencesHelper.start();
        Translator.start();
    }

    @Test
    public void getFileNameFilter() {
        // given
        SaveBitmap png = new SaveBitmap("png", true);
        // given
        SaveBitmap bmp = new SaveBitmap("bmp", true);
        // given
        SaveBitmap jpg = new SaveBitmap("jpg", true);
        SaveBitmap gif = new SaveBitmap("gif", true);

        // then
        assertArrayEquals(array("png"), png.getFileNameFilter().getExtensions());
        assertArrayEquals(array("bmp"), bmp.getFileNameFilter().getExtensions());
        assertArrayEquals(array("jpg"), jpg.getFileNameFilter().getExtensions());
        assertArrayEquals(array("gif"), gif.getFileNameFilter().getExtensions());
    }

    @Test
    public void savePNG() throws Exception {
        verifySavedFile(simpleMoves(), "png", true, "/save_simple_move.png");
    }

    @Test
    public void saveBMP() throws Exception {
        verifySavedFile(simpleMoves(),"bmp",false,"/save_simple_move.bmp");
    }

    @Test
    public void saveJPG() throws Exception {
        verifySavedFile(simpleMoves(),"jpg", false,"/save_simple_move.jpg");
    }

    @Test
    public void saveGIF() throws Exception {
        verifySavedFile(simpleMoves(), "gif", false, "/save_simple_move.gif");
    }

    @Test
    public void saveMulticolorPNG() throws Exception {
        verifySavedFile(multiColorsMoves(), "png",true,"/save_multi_colors.png");
    }

    @Test
    public void saveMulticolorBMP() throws Exception {
        verifySavedFile(multiColorsMoves(), "bmp",false,"/save_multi_colors.bmp");
    }

    @Test
    public void saveMulticolorJPG() throws Exception {
        verifySavedFile(multiColorsMoves(), "jpg",false,"/save_multi_colors.jpg");
    }

    @Test
    public void saveMulticolorGIF() throws Exception {
        verifySavedFile(multiColorsMoves(), "gif",false,"/save_multi_colors.gif");
    }

    private void verifySavedFile(Turtle turtle, String extension, boolean supportAlpha,String expectedFilename) throws Exception {
        // given
        File fileTemp = File.createTempFile("unit", "."+extension);

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(fileTemp);
            // when
            SaveBitmap save = new SaveBitmap(extension, supportAlpha);
            save.save(fileOutputStream, turtle, PlotterSettingsManager.buildMakelangelo5());
            fileOutputStream.close();

            // then
            assertEquals(-1,
                    Files.mismatch(fileTemp.toPath(),
                        Paths.get(Objects.requireNonNull(SaveBitmapTest.class.getResource(expectedFilename)).toURI())
                    )
            );
        } finally {
            fileTemp.delete();
        }
    }
}
