package com.marginallyclever.makelangelo;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.helpers.OpenGLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Point3d;
import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * <p>{@link Mesh} contains the vertex data for model to be rendered with OpenGL.  It may also contain normal, color,
 * and texture data.</p>
 * <p>It uses <a href="https://www.khronos.org/opengl/wiki/Vertex_Specification">Vertex Array Objects and Vertex
 * Buffer Objects</a> to optimize rendering large collections of primitives.</p>
 */
public class Mesh {
	private static final Logger logger = LoggerFactory.getLogger(Mesh.class);
	public static final int NUM_BUFFERS=5;  // verts, normals, colors, textureCoordinates, index
	public static final int BYTES_PER_INT = Integer.SIZE/8;
	public static final int BYTES_PER_FLOAT = Float.SIZE/8;

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
	protected final AABB boundingBox = new AABB();

	public Mesh() {
		super();
		boundingBox.setShape(this);
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

		int attribIndex=0;
		setupArray(gl,0,3,numVertexes,vertexArray);
		if(hasNormals ) setupArray(gl,1,3,numVertexes,normalArray );
		if(hasColors  ) setupArray(gl,2,4,numVertexes,colorArray  );
		if(hasTextures) setupArray(gl,3,2,numVertexes,textureArray);

		if(hasIndexes) {
			IntBuffer data = IntBuffer.allocate(indexArray.size());
			for (Integer integer : indexArray) data.put(integer);
			data.rewind();

			gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, VBO[4]);
			gl.glBufferData(GL3.GL_ELEMENT_ARRAY_BUFFER, (long) indexArray.size() *BYTES_PER_INT, data, GL3.GL_STATIC_DRAW);
		}

		gl.glBindVertexArray(0);
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
		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, VBO[attribIndex]);
		gl.glVertexAttribPointer(attribIndex,size,GL3.GL_FLOAT,false,0,0);
		OpenGLHelper.checkGLError(gl,logger);
	}

	private void setupArray(GL3 gl, int attribIndex, int size, long numVertexes,List<Float> list) {
		FloatBuffer data = FloatBuffer.allocate(list.size());
		for( Float f : list ) data.put(f);
		data.rewind();
		bindArray(gl,attribIndex,size);
		gl.glBufferData(GL3.GL_ARRAY_BUFFER, numVertexes*size*BYTES_PER_FLOAT, data, GL3.GL_STATIC_DRAW);
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
	 * @param r red, 0...1
	 * @param g green, 0...1
	 * @param b blue, 0...1
	 * @param a alpha, 0...1
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
	
	/**
	 * Force recalculation of the minimum bounding box to contain this STL file.
	 * Done automatically every time updateBuffers() is called.
	 * Meaningless if there is no vertexArray of points.
	 */
	public void updateCuboid() {
		Point3d boundBottom = new Point3d(Double.MAX_VALUE,Double.MAX_VALUE,Double.MAX_VALUE);
		Point3d boundTop = new Point3d(-Double.MAX_VALUE,-Double.MAX_VALUE,-Double.MAX_VALUE);
		
		// transform and calculate
		Iterator<Float> fi = vertexArray.iterator();
		double x,y,z;
		while(fi.hasNext()) {
			x = fi.next();
			y = fi.next();
			z = fi.next();
			boundTop.x = Math.max(x, boundTop.x);
			boundTop.y = Math.max(y, boundTop.y);
			boundTop.z = Math.max(z, boundTop.z);
			boundBottom.x = Math.min(x, boundBottom.x);
			boundBottom.y = Math.min(y, boundBottom.y);
			boundBottom.z = Math.min(z, boundBottom.z);
		}
		boundingBox.setBounds(boundTop, boundBottom);
	}

	/**
	 * @return axially-aligned bounding box in the mesh's local space.
	 */
	public AABB getBoundingBox() {
		return boundingBox;
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
}
