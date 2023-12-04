package com.marginallyclever.makelangelo.makeart.io;

import com.marginallyclever.convenience.FileAccess;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.imageconverter.SelectImageConverterPanel;
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
    private static final String KEY_PREFERENCE_LOAD_PATH = "loadPath";
    private final JFileChooser jFileChooser = new JFileChooser();
    private final JLabel previewLabel = new JLabel();
    private final Component parent;
    private OpenListener openListener;

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


        // display a preview
        jFileChooser.addPropertyChangeListener(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY, (evt) ->{
            // no file selected.
            previewLabel.setIcon(null);

            File file = (File) evt.getNewValue();
            if (file != null && file.isFile()) {
                // ImageIcon may silently fail to load the image.  That's ok.
                ImageIcon icon = new ImageIcon(file.getAbsolutePath());
                // Scale the image to fit the label
                Image scaledImage = icon.getImage().getScaledInstance(previewLabel.getWidth(), previewLabel.getHeight(), Image.SCALE_DEFAULT);
                previewLabel.setIcon(new ImageIcon(scaledImage));
            }
        });

        // Set a preferred size for the preview image
        previewLabel.setPreferredSize(new Dimension(200, 200));
        // glue the preview image to the right side of the dialog
        JPanel previewPanel = new JPanel();
        previewPanel.add(previewLabel);
        jFileChooser.setAccessory(previewPanel);
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
