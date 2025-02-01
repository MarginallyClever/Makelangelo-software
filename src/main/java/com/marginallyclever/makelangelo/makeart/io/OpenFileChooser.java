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
 * Configuring a {@link JFileChooser} with all the extensions the app supports
 */
public class OpenFileChooser {
    private static final Logger logger = LoggerFactory.getLogger(OpenFileChooser.class);
    public static final String KEY_PREFERENCE_LOAD_PATH = "loadPath";
    private final JFileChooser fileChooser = new JFileChooser();
    private final JPanel previewPanel = new JPanel();
    private final Component parent;
    private OpenListener openListener;

    public OpenFileChooser(Component parent) {
        super();

        this.parent = parent;

        // add all supported type
        String[] allSupportedTypes = Stream.concat(
                TurtleFactory.getLoadExtensions().stream()
                        .map(FileNameExtensionFilter::getExtensions)
                        .flatMap(Stream::of)
                ,
                Arrays.stream(SelectImageConverterPanel.IMAGE_FILE_EXTENSIONS.clone())
        ).toArray(String[]::new);

        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter(Translator.get("OpenFileChooser.AllSupportedFiles"), allSupportedTypes));

        // add vector formats
        for (FileNameExtensionFilter ff : TurtleFactory.getLoadExtensions()) {
            fileChooser.addChoosableFileFilter(ff);
        }

        // add image formats
        String names = String.join(", ",SelectImageConverterPanel.IMAGE_FILE_EXTENSIONS);
        FileNameExtensionFilter images = new FileNameExtensionFilter(Translator.get("OpenFileChooser.FileTypeImage",new String[]{names}), SelectImageConverterPanel.IMAGE_FILE_EXTENSIONS);
        fileChooser.addChoosableFileFilter(images);

        // no wild card filter, please.
        fileChooser.setAcceptAllFileFilterUsed(false);

        // display a preview
        fileChooser.addPropertyChangeListener(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY, (evt) ->{
            // no file selected.
            previewPanel.removeAll();

            File file = (File) evt.getNewValue();
            if (file != null && file.isFile()) {
                // ImageIcon may silently fail to load the image.  That's ok.
                ImageIcon icon = new ImageIcon(file.getAbsolutePath());
                // Scale the image to fit the label
                Image scaledImage = icon.getImage().getScaledInstance(previewPanel.getWidth(), previewPanel.getHeight(), Image.SCALE_DEFAULT);
                previewPanel.add(new JLabel(new ImageIcon(scaledImage)));
            }
        });

        // Set a preferred size for the preview image
        previewPanel.setPreferredSize(new Dimension(200, 200));
        // glue the preview image to the right side of the dialog
        fileChooser.setAccessory(previewPanel);
    }

    public void setOpenListener(OpenListener openListener) {
        this.openListener = openListener;
    }

    public void chooseFile() {
        Preferences preferences = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.FILE);
        String lastPath = preferences.get(KEY_PREFERENCE_LOAD_PATH, FileAccess.getWorkingDirectory());
        fileChooser.setCurrentDirectory(new File(lastPath));

        if (fileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            String filename = fileChooser.getSelectedFile().getAbsolutePath();
            preferences.put(KEY_PREFERENCE_LOAD_PATH, fileChooser.getCurrentDirectory().toString());
            logger.debug("File selected by user: {}", filename);
            openListener.openFile(filename);
        }
    }

    public interface OpenListener {
        void openFile(String filename);
    }
}
