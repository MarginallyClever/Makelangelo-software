package com.marginallyclever.makelangelo.makeart.io;

import com.marginallyclever.convenience.FileAccess;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.imageconverter.SelectImageConverterPanel;
import com.marginallyclever.makelangelo.makeart.io.vector.TurtleFactory;
import com.marginallyclever.util.PreferencesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.prefs.Preferences;
import java.util.stream.Stream;

/**
 * Takes care about configuring the file chooser with all the extensions the app supports
 */
public class OpenFileChooser {
    private static final Logger logger = LoggerFactory.getLogger(OpenFileChooser.class);
    private JFileChooser jFileChooser = new JFileChooser();
    private Component parent;
    private OpenListener openListener;
    private static final String KEY_PREFERENCE_LOAD_PATH = "loadPath";

    public OpenFileChooser(Component parent) {
        this.parent = parent;

        // add all supported type
        String[] extensions = Stream.concat(
                TurtleFactory.getLoadExtensions().stream()
                        .map(FileNameExtensionFilter::getExtensions)
                        .flatMap(Stream::of)
                ,
                Arrays.stream(SelectImageConverterPanel.IMAGE_FILE_EXTENSIONS.clone())
        ).toArray(String[]::new);

        jFileChooser.addChoosableFileFilter(new FileNameExtensionFilter(Translator.get("OpenFileChooser.AllSupportedFiles"), extensions));

        // add vector formats
        for (FileNameExtensionFilter ff : TurtleFactory.getLoadExtensions()) {
            jFileChooser.addChoosableFileFilter(ff);
        }

        // add image formats
        FileNameExtensionFilter images = new FileNameExtensionFilter(Translator.get("OpenFileChooser.FileTypeImage"), SelectImageConverterPanel.IMAGE_FILE_EXTENSIONS);
        jFileChooser.addChoosableFileFilter(images);

        // no wild card filter, please.
        jFileChooser.setAcceptAllFileFilterUsed(false);
    }

    public void setOpenListener(OpenListener openListener) {
        this.openListener = openListener;
    }

    public void chooseFile() {
        Preferences preferences = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.FILE);
        String lastPath = preferences.get(KEY_PREFERENCE_LOAD_PATH, FileAccess.getWorkingDirectory());
        jFileChooser.setCurrentDirectory(new File(lastPath));

        if (jFileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {

            String filename = jFileChooser.getSelectedFile().getAbsolutePath();
            preferences.put(KEY_PREFERENCE_LOAD_PATH, jFileChooser.getCurrentDirectory().toString());
            logger.debug("File selected by user: {}", filename);
            openListener.openFile(filename);
        }
    }

    public interface OpenListener {
        void openFile(String filename);
    }
}
