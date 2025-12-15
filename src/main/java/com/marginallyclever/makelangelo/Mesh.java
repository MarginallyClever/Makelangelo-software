package com.marginallyclever.makelangelo;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.helpers.OpenGLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.event.EventListenerList;
import javax.vecmath.Point3d;
import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>{@link Mesh} contains the vertex data for a 3D model.  It may also contain normal, color, and texture data.</p>
 * <p>It uses <a href="https://www.khronos.org/opengl/wiki/Vertex_Specification">Vertex Array Objects and Vertex
 * Buffer Objects</a> to optimize rendering large collections of triangles.</p>
 */
public class Mesh {
	private static final Logger logger = LoggerFactory.getLogger(Mesh.class);
	public static final int NUM_BUFFERS=5;  // verts, normals, colors, textureCoordinates, index

	public final transient List<Float> vertexArray = new ArrayList<>();
	public final transient List<Float> normalArray = new ArrayList<>();
	public final transient List<Float> colorArray = new ArrayList<>();
	public final transient List<Float> textureArray = new ArrayList<>();
	public final transient List<Integer> indexArray = new ArrayList<>();

	private transient boolean hasNormals = false;
	private transient boolean hasColors = false;
	private transient boolean isTransparent = false;
	private transient boolean hasTextures = false;
	private transient boolean hasIndexes = false;
	private transient boolean isDirty = false;
	private transient boolean isLoaded = false;

	private transient int[] VAO;
	private transient int[] VBO;

	public int renderStyle = GL3.GL_TRIANGLES;
	private String fileName = "";

	// bounding limits
	private final EventListenerList listeners = new EventListenerList();

	public Mesh() {
		super();
	}

	public Mesh(int renderStyle) {
		this();
		this.renderStyle = renderStyle;
	}
	
	/**
	 * Remove all vertexes, normals, colors, texture coordinates, etc.
	 * on the next call to {@link Mesh#render(GL3)} the mesh will be rebuilt to nothing.
	 * See also {@link Mesh#unload(GL3)}
	 */
	public void clear() {
		vertexArray.clear();
		normalArray.clear();
		colorArray.clear();
		textureArray.clear();
		indexArray.clear();
		isDirty=true;
	}

	public void setSourceName(String filename) {
		this.fileName = filename;
	}
	
	public String getSourceName() {
		return fileName;
	}

	public boolean isLoaded() {
		return isLoaded;
	}
	
	public void setLoaded(boolean loaded) {
		isLoaded=loaded;
	}

	public boolean isTransparent() {
		return isTransparent;
	}

	/**
	 * Destroy the optimized rendering buffers for the fixed function pipeline.
	 * This does not free the memory used by the mesh.  See also {@link Mesh#clear()}
	 * @param gl the OpenGL context
	 */
	public void unload(GL3 gl) {
		if(!isLoaded) return;
		isLoaded=false;
		destroyBuffers(gl);
	}
	
	private void createBuffers(GL3 gl) {
		VAO = new int[1];
		gl.glGenVertexArrays(1, VAO, 0);
		OpenGLHelper.checkGLError(gl,logger);

		VBO = new int[NUM_BUFFERS];
		gl.glGenBuffers(NUM_BUFFERS, VBO, 0);
		OpenGLHelper.checkGLError(gl,logger);
	}

	private void destroyBuffers(GL3 gl) {
		if(VBO != null) {
			gl.glDeleteBuffers(NUM_BUFFERS, VBO, 0);
			VBO = null;
		}
		if(VAO != null) {
			gl.glDeleteVertexArrays(1, VAO, 0);
			VAO = null;
		}
	}
	
	/**
	 * Regenerate the optimized rendering buffers for the fixed function pipeline.
	 * Also recalculate the bounding box.
	 * @param gl the OpenGL context
	 */
	private void updateBuffers(GL3 gl) {
		long numVertexes = getNumVertices();

		gl.glBindVertexArray(VAO[0]);
		OpenGLHelper.checkGLError(gl,logger);

		checkBufferSizes();

		setupArray(gl,0,3,numVertexes,vertexArray);
		if(hasNormals ) setupArray(gl,1,3,numVertexes,normalArray );
		if(hasColors  ) setupArray(gl,2,4,numVertexes,colorArray  );
		if(hasTextures) setupArray(gl,3,2,numVertexes,textureArray);

		if(hasIndexes) {
            ByteBuffer bb = ByteBuffer.allocateDirect(indexArray.size() * Integer.BYTES).order(ByteOrder.nativeOrder());
			IntBuffer data = bb.asIntBuffer();
			for (Integer integer : indexArray) data.put(integer);
			data.rewind();

			gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, VBO[4]);
			OpenGLHelper.checkGLError(gl,logger);
			gl.glBufferData(GL3.GL_ELEMENT_ARRAY_BUFFER, (long) indexArray.size() * Integer.BYTES, data, GL3.GL_STATIC_DRAW);
			OpenGLHelper.checkGLError(gl,logger);
		}

		gl.glBindVertexArray(0);
		OpenGLHelper.checkGLError(gl,logger);
	}

	private void checkBufferSizes() {
		var va = vertexArray.size();
		var na = normalArray.size();
		var ca = colorArray.size();
		var ta = textureArray.size();
		if(na>0 && na!=va) {
			throw new IllegalStateException("normalArray.size() != vertexArray.size()");
		}
		if(ca>0 && ca*3!=va*4) {
			throw new IllegalStateException("colorArray.size() != vertexArray.size()");
		}
		if(ta>0 && ta*3!=va*2) {
			throw new IllegalStateException("textureArray.size() != vertexArray.size()");
		}
	}

	private void bindArray(GL3 gl, int attribIndex, int size) {
		gl.glEnableVertexAttribArray(attribIndex);
		OpenGLHelper.checkGLError(gl,logger);
		gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, VBO[attribIndex]);
		OpenGLHelper.checkGLError(gl,logger);
		gl.glVertexAttribPointer(attribIndex,size,GL3.GL_FLOAT,false,0,0);
		OpenGLHelper.checkGLError(gl,logger);
	}

	private void setupArray(GL3 gl, int attribIndex, int size, long numVertexes,List<Float> list) {
        ByteBuffer bb = ByteBuffer.allocateDirect(list.size()*Float.BYTES).order(ByteOrder.nativeOrder());
		FloatBuffer data = bb.asFloatBuffer();
		for( Float f : list ) data.put(f);
		data.rewind();
		bindArray(gl,attribIndex,size);
		gl.glBufferData(GL3.GL_ARRAY_BUFFER, numVertexes*size*Float.BYTES, data, GL3.GL_STATIC_DRAW);
		OpenGLHelper.checkGLError(gl,logger);
	}

	/**
	 * Render the entire mesh.
	 * @param gl the OpenGL context
	 */
	public void render(GL3 gl) {
		if (hasIndexes) {
			render(gl,0,indexArray.size());
		} else {
			render(gl,0,getNumVertices());
		}
	}

	/**
	 * Render a portion of the mesh.
	 * @param gl the OpenGL context
	 * @param startIndex index of the first vertex to viewport
	 * @param count number of vertices to viewport
	 */
	public void render(GL3 gl,int startIndex,int count) {
		if(!isLoaded) {
			isLoaded=true;
			isDirty=true;
		}
		if(isDirty) {
			createBuffers(gl);
			updateBuffers(gl);
			isDirty=false;
		}

		gl.glBindVertexArray(VAO[0]);
		OpenGLHelper.checkGLError(gl,logger);

		if (hasIndexes) {
			gl.glDrawElements(renderStyle, indexArray.size(), GL3.GL_UNSIGNED_INT, 0);
		} else {
			gl.glDrawArrays(renderStyle, startIndex, count);
		}
		OpenGLHelper.checkGLError(gl,logger);
		gl.glBindVertexArray(0);
		OpenGLHelper.checkGLError(gl,logger);
	}
	
	public void addNormal(float x,float y,float z) {
		normalArray.add(x);
		normalArray.add(y);
		normalArray.add(z);
		hasNormals=true;
	}
	
	public void addVertex(float x,float y,float z) {
		vertexArray.add(x);
		vertexArray.add(y);
		vertexArray.add(z);
	}

	/**
	 * Add a color to the mesh.
	 * @param r red, 0-1
	 * @param g green, 0-1
	 * @param b blue, 0-1
	 * @param a alpha, 0-1
	 */
	public void addColor(float r,float g,float b,float a) {
		colorArray.add(r);
		colorArray.add(g);
		colorArray.add(b);
		colorArray.add(a);
		if(a!=1) isTransparent=true;
		hasColors=true;
	}

	/**
	 * Add a texture coordinate to the mesh.
	 * @param u 0-1
	 * @param v 0-1
	 */
	public void addTexCoord(float u,float v) {
		textureArray.add(u);
		textureArray.add(v);
		hasTextures =true;
	}
	
	public void addIndex(int n) {
		indexArray.add(n);
		hasIndexes=true;
	}

	public int getNumTriangles() {
		return vertexArray.size()/9;
	}

	public int getNumVertices() {
		return (vertexArray==null) ? 0 : vertexArray.size()/3;
	}

	public Vector3d getVertex(int t) {
		t*=3;
		double x = vertexArray.get(t++); 
		double y = vertexArray.get(t++); 
		double z = vertexArray.get(t++); 
		return new Vector3d(x,y,z);
	}

	public Vector3d getNormal(int t) {
		t*=3;
		double x = normalArray.get(t++);
		double y = normalArray.get(t++);
		double z = normalArray.get(t++);
		return new Vector3d(x,y,z);
	}

	public Vector2d getTexCoord(int t) {
		t*=2;
		double u = textureArray.get(t++);
		double v = textureArray.get(t++);
		return new Vector2d(u,v);
	}

	public boolean isDirty() {
		return isDirty;
	}

	public void setDirty(boolean isDirty) {
		this.isDirty = isDirty;
	}

	public boolean getHasNormals() {
		return hasNormals;
	}

	public boolean getHasColors() {
		return hasColors;
	}

	public boolean getHasTextures() {
		return hasTextures;
	}

	public boolean getHasIndexes() {
		return hasIndexes;
	}

	public void setRenderStyle(int style) {
		renderStyle = style;
	}

	public int getRenderStyle() {
		return renderStyle;
	}

	public void setVertex(int i, double x, double y, double z) {
		i*=3;
		vertexArray.set(i++, (float)x);
		vertexArray.set(i++, (float)y);
		vertexArray.set(i++, (float)z);
	}

	public void setTexCoord(int i, double u, double v) {
		i*=2;
		textureArray.set(i++, (float)u);
		textureArray.set(i++, (float)v);
	}

    public void updateVertexBuffers(GL3 gl3) {
		if(VBO==null) return;
		setupArray(gl3,0,3,getNumVertices(),vertexArray);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
		listeners.remove(PropertyChangeListener.class,listener);
    }

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		listeners.add(PropertyChangeListener.class,listener);
	}

	public void fireMeshChanged() {
		PropertyChangeEvent p = null;
		for( var v : listeners.getListeners(PropertyChangeListener.class)) {
			if(p==null) p = new PropertyChangeEvent(this,"mesh",null,this);
			v.propertyChange(p);
		}
	}

	/**
	 * <p>Assumes that all triangles are outward facing and part of a closed, convex, non-intersecting mesh.</p>
	 * <p>This is a very rough approximation.  The most correct way to do this is to find the area of every triangle,
	 * then pick a random triangle weighted by area, then pick a random point on that triangle.</p>
	 * <p>The actual algorithm picks a random triangle, then a random point inside that triangle.</p>
	 * @return a random point on the surface of this mesh.
	 */
    public Point3d getRandomPointOnSurface() {
		int triangle = (int)(Math.random()*getNumTriangles());
		return getRandomPointOnTriangle(triangle);
    }

	public Point3d getRandomPointOnTriangle(int triangleIndex) {
		Vector3d v0 = getVertex(triangleIndex*3);
		Vector3d v1 = getVertex(triangleIndex*3+1);
		Vector3d v2 = getVertex(triangleIndex*3+2);
		double a = Math.random();
		double b = Math.random();
		if(a+b>1) {
			a=1-a;
			b=1-b;
		}
		// this is a weighted average of the three points.
		double x = v0.x + a * (v1.x - v0.x) + b * (v2.x - v0.x);
		double y = v0.y + a * (v1.y - v0.y) + b * (v2.y - v0.y);
		double z = v0.z + a * (v1.z - v0.z) + b * (v2.z - v0.z);

		return new Point3d(x,y,z);
	}

	public double getTriangleArea(int triangleIndex) {
		Vector3d v0 = getVertex(triangleIndex*3);
		Vector3d v1 = getVertex(triangleIndex*3+1);
		Vector3d v2 = getVertex(triangleIndex*3+2);
		var v20 = new Vector3d(v2);
		v20.sub(v0);
		var v10 = new Vector3d(v1);
		v10.sub(v0);
		var cross = new Vector3d();
		cross.cross(v20,v10);
		return cross.length()/2;
	}
}
