package com.marginallyclever.makelangelo.texture;

import com.jogamp.opengl.GL;
import com.marginallyclever.convenience.FileAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides a central place to manage textures.  All texture creation should be requested here.
 */
public class TextureFactory {
    private static final Logger logger = LoggerFactory.getLogger(TextureFactory.class);
    private static final List<TextureWithMetadata> textures = new ArrayList<>();

    /**
     * Load the given file from the classpath. Make sure the size of the picture is a power of 2
     * @param name filename
     * @return a texture
     */
    public static TextureWithMetadata loadTexture(String name) {
        // look for a texture that has already been loaded.
        for(TextureWithMetadata tex : textures) {
            if(tex.getSource().equals(name)) {
                return tex;
            }
        }

        // create a new texture and remember it.
        try {
            BufferedImage image = ImageIO.read(FileAccess.open(name));
            TextureWithMetadata meta = new TextureWithMetadata(image,name);
            textures.add(meta);
            return meta;
        } catch (IOException e) {
            logger.warn("Can't load {}", name, e);
        }
        return null;
    }

    public static void unloadAll(GL gl) {
        for(TextureWithMetadata tex : textures) {
            tex.unload(gl);
        }
        textures.clear();
    }
}
