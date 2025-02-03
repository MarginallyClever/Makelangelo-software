package com.marginallyclever.makelangelo.donatelloimpl.ports;

import com.marginallyclever.donatello.Filename;
import com.marginallyclever.donatello.SwingProvider;
import com.marginallyclever.makelangelo.select.SelectFile;
import com.marginallyclever.nodegraphcore.port.Input;

import javax.swing.*;
import java.awt.*;

/**
 * A port that accepts a filename.
 */
public class InputFilename extends Input<Filename> implements SwingProvider {
    private SelectFile selectFile;
    private JFileChooser fileChooser;

    public InputFilename(String name, String startingValue) throws IllegalArgumentException {
        super(name, Filename.class, new Filename(startingValue));
    }

    @Override
    public Component getSwingComponent(Component parent) {
        if(selectFile==null) {
            selectFile = new SelectFile(name,name,getValue().get(),parent);
            if(fileChooser!=null) {
                selectFile.setFileChooser(fileChooser);
            }
            selectFile.addSelectListener( evt -> {
                setValue(evt.getNewValue());
            });
        }
        return selectFile;
    }

    /**
     * Set the file chooser to use when selecting a file.
     * @param fileChooser the file chooser to use.  cannot be null.
     */
    public void setFileChooser(JFileChooser fileChooser) {
        this.fileChooser = fileChooser;
    }
}
