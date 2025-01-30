package com.marginallyclever.makelangelo.donatelloimpl.ports;

import com.marginallyclever.nodegraphcore.port.Output;

import java.awt.image.BufferedImage;

public class OutputImage extends Output<BufferedImage> {
    public OutputImage(String _name) throws IllegalArgumentException {
        super(_name, BufferedImage.class, new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB));
    }
}
