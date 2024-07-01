package com.marginallyclever.makelangelo;

import com.jogamp.opengl.GL3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * A factory for creating and maintaining Mesh objects.
 */
public class MeshFactory {
    private static final Logger logger = LoggerFactory.getLogger(MeshFactory.class);

    // the pool of all mesh loaded
    private static final List<Mesh> meshPool = new ArrayList<>();

    /**
     * Create a new mesh and add it to the pool.  This should be the only way to create a mesh.
     * @return a new mesh
     */
    public static Mesh createMesh() {
        Mesh m = new Mesh();
        addMesh(m);
        return m;
    }

    public static void addMesh(Mesh mesh) {
        if(meshPool.contains(mesh)) return;
        meshPool.add(mesh);
    }

    public static void removeMesh(Mesh mesh) {
        meshPool.remove(mesh);
    }

    public static void unloadAll(GL3 gl) {
        for(Mesh m : meshPool) {
            m.unload(gl);
        }
    }
}
