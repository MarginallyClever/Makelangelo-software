package com.marginallyclever.convenience.helpers;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.glu.GLU;

/**
 * A collection of static methods to help with OpenGL.
 *
 */
public class OpenGLHelper {
	public static void checkGLError(GL3 gl3,org.slf4j.Logger logger) {
		int err = gl3.glGetError();
		if(err != GL.GL_NO_ERROR) {
			GLU glu = new GLU();
			logger.error("GL error {}: {}", err, glu.gluErrorString(err));
		}
	}
}
