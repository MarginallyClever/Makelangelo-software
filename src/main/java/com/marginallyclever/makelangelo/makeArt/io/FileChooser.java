package com.marginallyclever.makelangelo.makeArt.io;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeArt.io.image.ConvertImagePanel;
import com.marginallyclever.makelangelo.makeArt.io.vector.TurtleFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Takes care about configuring the file chooser with all the extensions the app supports
 */
public class FileChooser {
    private static final Logger logger = LoggerFactory.getLogger(FileChooser.class);
    private JFileChooser jFileChooser = new JFileChooser();
    private Component parent;
    private OpenListener openListener;

    public FileChooser(Component parent) {
        this.parent = parent;

        // add all supported type
        String[] extensions = Stream.concat(
                TurtleFactory.getLoadExtensions().stream()
                        .map(FileNameExtensionFilter::getExtensions)
                        .flatMap(Stream::of)
                ,
                Arrays.stream(ConvertImagePanel.IMAGE_FILE_EXTENSIONS.clone())
        ).toArray(String[]::new);

        jFileChooser.addChoosableFileFilter(new FileNameExtensionFilter(Translator.get("FileChooser.AllSupportedFiles"), extensions));

        // add vector formats
        for (FileNameExtensionFilter ff : TurtleFactory.getLoadExtensions()) {
            jFileChooser.addChoosableFileFilter(ff);
        }

        // add image formats
        FileNameExtensionFilter images = new FileNameExtensionFilter(Translator.get("FileChooser.FileTypeImage"), ConvertImagePanel.IMAGE_FILE_EXTENSIONS);
        jFileChooser.addChoosableFileFilter(images);

        // no wild card filter, please.
        jFileChooser.setAcceptAllFileFilterUsed(false);
    }

    public void setOpenListener(OpenListener openListener) {
        this.openListener = openListener;
    }

    public void chooseFile() {
        if (jFileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {

            String filename = jFileChooser.getSelectedFile().getAbsolutePath();
            logger.debug("File selected by user: {}", filename);
            openListener.openFile(filename);
        }
    }

    public String getLastPath() {
        return jFileChooser.getCurrentDirectory().toString();
    }

    public void setLastPath(String lastPath) {
        jFileChooser.setCurrentDirectory((lastPath==null?null : new File(lastPath)));
    }

    public interface OpenListener {
        void openFile(String filename);
    }
}
