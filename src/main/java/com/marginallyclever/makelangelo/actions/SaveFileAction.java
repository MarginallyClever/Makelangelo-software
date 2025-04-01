package com.marginallyclever.makelangelo.actions;

import com.marginallyclever.makelangelo.MainFrame;
import com.marginallyclever.makelangelo.SaveDialog;
import com.marginallyclever.makelangelo.Translator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Objects;

public class SaveFileAction extends AbstractAction {
    private static final Logger logger = LoggerFactory.getLogger(SaveFileAction.class);

    private final MainFrame frame;
    private final SaveDialog saveDialog = new SaveDialog();

    public SaveFileAction(String label, MainFrame frame) {
        super(label);
        this.frame = frame;
        putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke("Control S"));
        putValue(Action.SMALL_ICON, new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-save-16.png"))));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        var ancestor = SwingUtilities.getWindowAncestor(frame);
        try {
            saveDialog.run(frame.getTurtle(), ancestor,frame.getPlotter().getSettings());
        } catch(Exception e1) {
            logger.error("Error while saving the vector file", e1);
            JOptionPane.showMessageDialog(ancestor,
                    Translator.get("SaveError") + e1.getLocalizedMessage(),
                    Translator.get("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
        }
    }
}
