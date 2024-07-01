package com.marginallyclever.makelangelo.apps.previewpanel;

import com.jogamp.opengl.GL3;

public class RenderContext {
    public GL3 gl;
    public ShaderProgram shader;

    public RenderContext(GL3 gl, ShaderProgram shader) {
        this.gl = gl;
        this.shader = shader;
    }
}
