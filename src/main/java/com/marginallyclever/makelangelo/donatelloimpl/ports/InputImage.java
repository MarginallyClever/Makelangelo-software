package com.marginallyclever.makelangelo.donatelloimpl.ports;

import com.marginallyclever.nodegraphcore.port.Input;

import java.awt.image.BufferedImage;

public class InputImage extends Input<BufferedImage> {
    public InputImage(String name) throws IllegalArgumentException {
        super(name, BufferedImage.class, new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB));
    }
}
