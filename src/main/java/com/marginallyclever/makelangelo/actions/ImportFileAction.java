package com.marginallyclever.makelangelo.actions;

import com.marginallyclever.convenience.FileAccess;
import com.marginallyclever.makelangelo.MainFrame;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.io.OpenFileChooser;
import com.marginallyclever.makelangelo.makeart.io.TurtleFactory;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.util.PreferencesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Objects;
import java.util.prefs.Preferences;

/**
 * Load a vector and add it to the existing {@link Turtle}.
 */
public class ImportFileAction extends AbstractAction {
    private static final Logger logger = LoggerFactory.getLogger(ImportFileAction.class);

    private final MainFrame frame;

    public ImportFileAction(String label, MainFrame frame) {
        super(label);
        this.frame = frame;
        putValue(Action.SMALL_ICON,new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-import-16.png"))));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JFileChooser fileChooser = TurtleFactory.getLoadFileChooser();
        // load the last path from preferences
        Preferences preferences = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.FILE);
        String lastPath = preferences.get(OpenFileChooser.KEY_PREFERENCE_LOAD_PATH, FileAccess.getWorkingDirectory());
        fileChooser.setCurrentDirectory(new File(lastPath));

        if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            String filename = fileChooser.getSelectedFile().getAbsolutePath();
            logger.debug("File selected by user: {}", filename);

            // save the path to preferences
            preferences.put(OpenFileChooser.KEY_PREFERENCE_LOAD_PATH, fileChooser.getCurrentDirectory().toString());

            // commit the load
            try {
                Turtle sum = frame.getTurtle();
                sum.add(TurtleFactory.load(filename));
                frame.setTurtle(sum);
            } catch(Exception e1) {
                logger.error("Failed to load {}", filename, e1);
                JOptionPane.showMessageDialog(frame, e1.getLocalizedMessage(), Translator.get("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
