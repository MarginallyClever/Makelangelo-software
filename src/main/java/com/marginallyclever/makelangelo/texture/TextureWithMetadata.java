package com.marginallyclever.makelangelo.texture;

import com.marginallyclever.convenience.FileAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;

/**
 * Contains the raw OpenGL texture and the source filename.
 */
public class TextureWithMetadata {
    private static final Logger logger = LoggerFactory.getLogger(TextureWithMetadata.class);

    private final String source;
    private BufferedImage texture;

    public TextureWithMetadata(String source) {
        this.source = source;
    }

    public BufferedImage getTexture() {
        if (texture == null) {
            texture = loadImageFromFile(source);
        }
        return texture;
    }

    private BufferedImage loadImageFromFile(String source) {
        try (BufferedInputStream bis = FileAccess.open(source)) {
            return ImageIO.read(bis);
        } catch (IOException e) {
            logger.warn("Can't load {}", source, e);
        }
        return null;
    }

    public void dispose() {
        if(texture!=null) {
            texture=null;
        }
    }
}
