package com.marginallyclever.makelangelo.preview;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.helpers.OpenGLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.awt.*;
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
    private final float [] matrixBuffer = new float[16];

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
        ensureStatus(gl, programId, GL3.GL_LINK_STATUS, "program link", false);

        gl.glValidateProgram(programId);
        ensureStatus(gl, programId, GL3.GL_VALIDATE_STATUS, "program validate", false);
    }

    private int loadShader(GL3 gl, int type, String[] shaderCode, String name) {
        int shaderId = gl.glCreateShader(type);
        gl.glShaderSource(shaderId, shaderCode.length, shaderCode, null, 0);
        gl.glCompileShader(shaderId);
        // ensure compile succeeded or throw with the shader log
        ensureStatus(gl, shaderId, GL3.GL_COMPILE_STATUS, "shader compile: " + name, true);
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

    /**
     * Ensure the given shader/program status is OK.  If not, retrieve and log the info log and throw an exception.
     * @param gl GL context
     * @param id shader or program id
     * @param param status to check (e.g. GL_COMPILE_STATUS, GL_LINK_STATUS)
     * @param name human friendly name to use in the log/exception
     * @param isShader whether id is a shader (true) or a program (false)
     */
    private void ensureStatus(GL3 gl, int id, int param, String name, boolean isShader) {
        if (checkStatus(gl,id,param)) return;
        int[] logLength = new int[1];
        if (isShader) {
            gl.glGetShaderiv(id, GL3.GL_INFO_LOG_LENGTH, logLength, 0);
            if(logLength[0] > 0) {
                byte[] log = new byte[logLength[0]];
                gl.glGetShaderInfoLog(id, logLength[0], null, 0, log, 0);
                logger.error("{} failed: {}", name, new String(log));
            }
        } else {
            gl.glGetProgramiv(id, GL3.GL_INFO_LOG_LENGTH, logLength, 0);
            if(logLength[0] > 0) {
                byte[] log = new byte[logLength[0]];
                gl.glGetProgramInfoLog(id, logLength[0], null, 0, log, 0);
                logger.error("{} failed: {}", name, new String(log));
            }
        }
        throw new IllegalStateException(name + " failed");
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

    private float [] matrixToFloatArray(Matrix4d m) {
        // Fill explicitly by index to avoid analyzer warnings about post-increment usage
        matrixBuffer[0] = (float)m.m00;
        matrixBuffer[1] = (float)m.m01;
        matrixBuffer[2] = (float)m.m02;
        matrixBuffer[3] = (float)m.m03;

        matrixBuffer[4] = (float)m.m10;
        matrixBuffer[5] = (float)m.m11;
        matrixBuffer[6] = (float)m.m12;
        matrixBuffer[7] = (float)m.m13;

        matrixBuffer[8] = (float)m.m20;
        matrixBuffer[9] = (float)m.m21;
        matrixBuffer[10] = (float)m.m22;
        matrixBuffer[11] = (float)m.m23;

        matrixBuffer[12] = (float)m.m30;
        matrixBuffer[13] = (float)m.m31;
        matrixBuffer[14] = (float)m.m32;
        matrixBuffer[15] = (float)m.m33;
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
        gl.glUniformMatrix4fv(getUniformLocation(gl, name), 1, true, matrixToFloatArray(value),0);
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