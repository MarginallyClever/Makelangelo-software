package com.marginallyclever.makelangelo.texture;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import com.marginallyclever.convenience.FileAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;

/**
 * Contains the raw OpenGL texture and the source filename.
 */
public class TextureWithMetadata {
    private static final Logger logger = LoggerFactory.getLogger(TextureWithMetadata.class);

    private final String source;
    private Texture texture;

    public TextureWithMetadata(String source) {
        this.source = source;
    }

    public Texture getTexture() {
        try (BufferedInputStream bis = FileAccess.open(source)) {
            return TextureIO.newTexture(bis, false, source.substring(source.lastIndexOf('.') + 1));
        } catch (IOException e) {
            logger.warn("Can't load {}", source, e);
        }
        return null;
    }

    public void bind(GL gl) {
        if(texture==null) {
            texture = getTexture();
        }
        if(texture==null) return;

        texture.bind(gl);
    }

    public void dispose(GL gl) {
        if(texture!=null) {
            texture.destroy(gl);
            texture=null;
        }
    }
}
