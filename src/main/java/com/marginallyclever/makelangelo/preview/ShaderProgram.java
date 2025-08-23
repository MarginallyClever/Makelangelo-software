package com.marginallyclever.makelangelo.preview;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.helpers.OpenGLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.nio.FloatBuffer;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>{@link ShaderProgram} is a wrapper for vertex and fragment shader programs.  It also provides a simple interface
 * for setting uniforms.</p>
 */
public class ShaderProgram {
    private static final Logger logger = LoggerFactory.getLogger(ShaderProgram.class);
    private final int programId;
    private final int vertexShaderId;
    private final int fragmentShaderId;
    private final Map<String, Integer> uniformLocations = new HashMap<>();
    private final FloatBuffer matrixBuffer = FloatBuffer.allocate(16);

    public ShaderProgram(GL3 gl, String[] vertexCode, String[] fragmentCode) {
        super();
        if (vertexCode == null || vertexCode.length == 0) {
            throw new IllegalArgumentException("Vertex shader code cannot be null or empty.");
        }
        if (fragmentCode == null || fragmentCode.length == 0) {
            throw new IllegalArgumentException("Fragment shader code cannot be null or empty.");
        }
        vertexShaderId = loadShader(gl, GL3.GL_VERTEX_SHADER, vertexCode,"vertex");
        fragmentShaderId = loadShader(gl, GL3.GL_FRAGMENT_SHADER, fragmentCode,"fragment");

        programId = gl.glCreateProgram();

        gl.glAttachShader(programId, vertexShaderId);
        gl.glAttachShader(programId, fragmentShaderId);
        gl.glLinkProgram(programId);
        if (!checkStatus(gl, programId, GL3.GL_LINK_STATUS)) {
            throw new IllegalStateException("Failed to link shader program.");
        }
        gl.glValidateProgram(programId);
        if (!checkStatus(gl, programId, GL3.GL_VALIDATE_STATUS)) {
            throw new IllegalStateException("Failed to validate shader program.");
        }
    }

    private void showProgramError(GL3 gl, String message) {
        int[] logLength = new int[1];
        gl.glGetProgramiv(programId, GL3.GL_INFO_LOG_LENGTH, logLength, 0);
        byte[] log = new byte[logLength[0]];
        gl.glGetProgramInfoLog(programId, logLength[0], null, 0, log, 0);
        logger.error(message + new String(log));
    }

    private int loadShader(GL3 gl, int type, String[] shaderCode, String name) {
        int shaderId = gl.glCreateShader(type);
        gl.glShaderSource(shaderId, shaderCode.length, shaderCode, null, 0);
        gl.glCompileShader(shaderId);
        if (!checkStatus(gl, shaderId, GL3.GL_COMPILE_STATUS)) {
            int[] logLength = new int[1];
            gl.glGetShaderiv(shaderId, GL3.GL_INFO_LOG_LENGTH, logLength, 0);

            byte[] log = new byte[logLength[0]];
            gl.glGetShaderInfoLog(shaderId, logLength[0], null, 0, log, 0);

            logger.error("Failed to compile "+name+" shader code: " + new String(log));
        }
        return shaderId;
    }

    /**
     * Check the status of a shader or program.
     *
     * @param gl    The OpenGL context
     * @param id    The shader or program id
     * @param param The parameter to check
     * @return true if the status is OK
     */
    private boolean checkStatus(GL3 gl, int id, int param) {
        int[] result = new int[]{GL3.GL_FALSE};
        if (param == GL3.GL_COMPILE_STATUS) {
            gl.glGetShaderiv(id, param, result, 0);
        } else {
            gl.glGetProgramiv(id, param, result, 0);
        }
        return result[0] != GL3.GL_FALSE;
    }

    public void use(GL3 gl) {
        gl.glUseProgram(programId);
    }

    public void dispose(GL3 gl) {
        gl.glDetachShader(programId, vertexShaderId);
        gl.glDetachShader(programId, fragmentShaderId);
        gl.glDeleteShader(vertexShaderId);
        gl.glDeleteShader(fragmentShaderId);
        gl.glDeleteProgram(programId);
    }

    public int getProgramId() {
        return programId;
    }

    public int getUniformLocation(GL3 gl, String name) {
        Integer result = uniformLocations.get(name);
        if(result == null) {
            result = gl.glGetUniformLocation(programId, name);
            if(result==-1) throw new InvalidParameterException("Could not find uniform "+name);
            uniformLocations.put(name,result);
        }
        return result;
    }

    public void set1f(GL3 gl, String name, float v0) {
        gl.glUniform1f(getUniformLocation(gl, name), v0);
    }

    public void set2f(GL3 gl, String name, float v0, float v1) {
        gl.glUniform2f(getUniformLocation(gl, name), v0, v1);
    }

    public void set3f(GL3 gl, String name, float v0, float v1, float v2) {
        gl.glUniform3f(getUniformLocation(gl, name), v0, v1, v2);
        OpenGLHelper.checkGLError(gl,logger);
    }

    public void set4f(GL3 gl, String name, float v0, float v1, float v2, float v3) {
        gl.glUniform4f(getUniformLocation(gl, name), v0, v1, v2, v3);
        OpenGLHelper.checkGLError(gl,logger);
    }

    public void setVector3d(GL3 gl, String name, Vector3d value) {
        gl.glUniform3f(getUniformLocation(gl, name), (float) value.x, (float) value.y, (float) value.z);
        OpenGLHelper.checkGLError(gl,logger);
    }

    private FloatBuffer matrixToFloatBuffer(Matrix4d m) {
        matrixBuffer.put( (float)m.m00 );
        matrixBuffer.put( (float)m.m01 );
        matrixBuffer.put( (float)m.m02 );
        matrixBuffer.put( (float)m.m03 );

        matrixBuffer.put( (float)m.m10 );
        matrixBuffer.put( (float)m.m11 );
        matrixBuffer.put( (float)m.m12 );
        matrixBuffer.put( (float)m.m13 );

        matrixBuffer.put( (float)m.m20 );
        matrixBuffer.put( (float)m.m21 );
        matrixBuffer.put( (float)m.m22 );
        matrixBuffer.put( (float)m.m23 );

        matrixBuffer.put( (float)m.m30 );
        matrixBuffer.put( (float)m.m31 );
        matrixBuffer.put( (float)m.m32 );
        matrixBuffer.put( (float)m.m33 );
        matrixBuffer.rewind();

        return matrixBuffer;
    }

    /**
     * Set a matrix in the shader.  Java uses column-major order, where OpenGL and DirectX use row-major order.
     * Thus the thurd parameter is true to make the video card transpose the matrix from row-major to column-major.
     * @see <a href="https://registry.khronos.org/OpenGL-Refpages/gl4/html/glUniform.xhtml">glUniform</a>
     * @param gl the viewport context
     * @param name the name of the uniform variable
     * @param value the matrix to set
     */
    public void setMatrix4d(GL3 gl, String name, Matrix4d value) {
        gl.glUniformMatrix4fv(getUniformLocation(gl, name), 1, true, matrixToFloatBuffer(value));
        OpenGLHelper.checkGLError(gl,logger);
    }

    public void set1i(GL3 gl, String name, int value) {
        gl.glUniform1i(getUniformLocation(gl, name), value);
        OpenGLHelper.checkGLError(gl,logger);
    }

    public void setColor(GL3 gl3, String name, Color color) {
        set4f(gl3,name,color.getRed()/255f,color.getGreen()/255f,color.getBlue()/255f,color.getAlpha()/255f);
    }
}