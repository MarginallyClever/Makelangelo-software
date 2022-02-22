package com.marginallyclever.nodeBasedEditor.model.builtInNodes.images;

import com.marginallyclever.nodeBasedEditor.model.Node;
import com.marginallyclever.nodeBasedEditor.model.NodeVariable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class LoadImage extends Node {
    private final NodeVariable<String> filename = NodeVariable.newInstance("filename",String.class,null,true,false);
    private final NodeVariable<BufferedImage> contents = NodeVariable.newInstance("contents", BufferedImage.class, null,false,true);
    private final NodeVariable<Number> width = NodeVariable.newInstance("width",Number.class,0,false,true);
    private final NodeVariable<Number> height = NodeVariable.newInstance("height",Number.class,0,false,true);

    public LoadImage() {
        super("LoadImage");
        addVariable(filename);
        addVariable(contents);
        addVariable(width);
        addVariable(height);
    }

    public LoadImage(String filename) {
        this();
        this.filename.setValue(filename);
    }

    @Override
    public Node create() {
        return new LoadImage();
    }

    @Override
    public void update() {
        if(!isDirty()) return;
        try {
            BufferedImage image = ImageIO.read(new File(filename.getValue()));
            contents.setValue(image);
            width.setValue(image.getWidth());
            height.setValue(image.getHeight());
            cleanAllInputs();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
