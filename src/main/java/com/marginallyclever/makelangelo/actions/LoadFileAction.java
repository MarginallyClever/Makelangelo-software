package com.marginallyclever.makelangelo.actions;

import com.marginallyclever.makelangelo.MainFrame;
import com.marginallyclever.makelangelo.RecentFiles;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.io.LoadFilePanel;
import com.marginallyclever.makelangelo.makeart.io.OpenFileChooser;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.security.InvalidParameterException;
import java.util.Objects;

public class LoadFileAction extends AbstractAction {
    private static final Logger logger = LoggerFactory.getLogger(LoadFileAction.class);

    private final MainFrame frame;
    private final RecentFiles recentFiles;

    public LoadFileAction(String name, @Nonnull MainFrame frame, @Nullable RecentFiles recentFiles) {
        super(name);
        this.frame = frame;
        this.recentFiles = recentFiles;

        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control O"));
        putValue(Action.SMALL_ICON, new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/makelangelo/actions/icons8-load-16.png"))));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        OpenFileChooser openFileChooser = new OpenFileChooser(SwingUtilities.getWindowAncestor(frame));
        openFileChooser.setOpenListener(this::openFile);
        openFileChooser.chooseFile();
    }

    public void openFile(String filename) {
        if(filename == null || filename.trim().isEmpty()) throw new InvalidParameterException("filename cannot be empty");

        try {
            LoadFilePanel loader = new LoadFilePanel(frame.getPaper(),filename);
            loader.addActionListener(e -> frame.setTurtle((Turtle)(e.getSource())));

            if (loader.onNewFilenameChosen(filename)) {
                var previewPanel = frame.getPreviewPanel();

                previewPanel.addListener(loader);
                JDialog dialog = new JDialog(frame, Translator.get("LoadFilePanel.title"));
                dialog.add(loader);
                dialog.setMinimumSize(new Dimension(500,500));
                dialog.pack();
                dialog.setLocationRelativeTo(frame);
                loader.setParent(dialog);

                frame.enableMenuBar(false);
                dialog.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        loader.loadingFinished();
                        frame.enableMenuBar(true);
                        previewPanel.removeListener(loader);
                        if(recentFiles!=null) recentFiles.addFilename(filename);
                    }
                });

                dialog.setVisible(true);
            } else {
                if(recentFiles!=null) recentFiles.addFilename(filename);
            }

            frame.setMainTitle(new File(filename).getName());
        } catch(Exception e) {
            logger.error("Error while loading the file {}", filename, e);
            JOptionPane.showMessageDialog(frame, Translator.get("LoadError") + e.getLocalizedMessage(), Translator.get("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            if(recentFiles!=null) recentFiles.removeFilename(filename);
        }
    }
}
