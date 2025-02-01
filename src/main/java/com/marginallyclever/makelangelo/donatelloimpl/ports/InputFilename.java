package com.marginallyclever.makelangelo.donatelloimpl.ports;

import com.marginallyclever.donatello.SwingProvider;
import com.marginallyclever.makelangelo.select.SelectFile;

import javax.swing.*;
import java.awt.*;

public class InputFilename extends InputString implements SwingProvider {
    private SelectFile selectFile;
    private JFileChooser fileChooser;

    public InputFilename(String name, String startingValue) throws IllegalArgumentException {
        super(name, startingValue);
    }

    @Override
    public Component getSwingComponent(Component parent) {
        if(selectFile==null) {
            selectFile = new SelectFile(name,name,value,parent);
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
