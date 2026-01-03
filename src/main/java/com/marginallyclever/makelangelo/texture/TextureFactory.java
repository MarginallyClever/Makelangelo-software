package com.marginallyclever.makelangelo.texture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * By default OpenGL uses a Texture class.  this class can become invalidated if the OpenGL context is lost.
 * This class is a wrapper around Texture that can be reloaded if the context is lost.
 */
public class TextureFactory {
    private static final Logger logger = LoggerFactory.getLogger(TextureFactory.class);

    private static final List<TextureWithMetadata> textures = new ArrayList<>();

    /**
     * The OpenGL context has just died.  release all Textures.
     */
    public static void dispose() {
        for (TextureWithMetadata tex : textures) {
            tex.dispose();
        }
    }

    /**
     * Load the given file from the classpath. Make sure the size of the picture is a power of 2
     * @param name filename
     * @return a texture
     */
    public static TextureWithMetadata loadTexture(String name) {
        TextureWithMetadata tex = new TextureWithMetadata(name);
        textures.add(tex);
        return tex;
    }
}
