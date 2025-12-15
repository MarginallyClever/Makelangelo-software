package com.marginallyclever.convenience.helpers;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.glu.GLU;

import java.util.ArrayList;
import java.util.List;

/**
 * A collection of static methods to help with OpenGL.
 *
 */
public class OpenGLHelper {
	private static final List<Integer> errorCodes = new ArrayList<>();

	public static void checkGLError(GL3 gl3,org.slf4j.Logger logger) {
		int err = gl3.glGetError();
		if(err != GL.GL_NO_ERROR) {
			GLU glu = GLU.createGLU(gl3);
			logger.error("GL error {}: {}", err, glu.gluErrorString(err));
			if(!errorCodes.contains(err)) {
				errorCodes.add(err);
				var error = new Exception("GL error %s: %s".formatted(err, glu.gluErrorString(err)));
				logger.error(error.getMessage(), error);
			}
		}
	}
}
