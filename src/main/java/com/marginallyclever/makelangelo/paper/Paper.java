package com.marginallyclever.makelangelo.paper;

import com.jogamp.opengl.GL3;
import com.marginallyclever.makelangelo.Mesh;
import com.marginallyclever.makelangelo.preview.PreviewListener;
import com.marginallyclever.makelangelo.preview.ShaderProgram;
import com.marginallyclever.util.PreferencesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.prefs.Preferences;

public class Paper implements PreviewListener {
	private static final Logger logger = LoggerFactory.getLogger(Paper.class);

	public static final int DEFAULT_WIDTH = 420; // mm
	public static final int DEFAULT_HEIGHT = 594; // mm

	private static final String PREF_KEY_ROTATION = "rotation";
	private static final String PREF_KEY_PAPER_MARGIN = "paper_margin";
	private static final String PREF_KEY_PAPER_BOTTOM = "paper_bottom";
	private static final String PREF_KEY_PAPER_TOP = "paper_top";
	private static final String PREF_KEY_PAPER_RIGHT = "paper_right";
	private static final String PREF_KEY_PAPER_LEFT = "paper_left";
	private static final String PREF_KEY_PAPER_COLOR = "paper_color";
	private static final String PREF_KEY_PAPER_CENTER_X = "paper_center_X";
	private static final String PREF_KEY_PAPER_CENTER_Y = "paper_center_Y";

	private static final Preferences paperPreferenceNode
		= PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.PAPER);
	
	// paper border position ( from the center of the paper)
	private double paperLeft;
	private double paperRight;
	private double paperBottom;
	private double paperTop;
	// % from edge of paper.
	private double paperMargin = 0.95;
	
	private double rotation;
	private double rotationRef;

	// shift apply to the center of the paper
	private double centerX=0.0d;
	private double centerY=0.0d;
	private Color paperColor = Color.WHITE;

	private final Mesh paperMesh = new Mesh();
	private final Mesh marginMesh = new Mesh();

	public Paper() {
		super();
		setPaperSize(DEFAULT_WIDTH, DEFAULT_HEIGHT, 0, 0);
	}
	
	@Override
	public void render(ShaderProgram shader, GL3 gl) {
		renderPaper(gl);
		renderMargin(gl);
	}
	
	/**
	 * Render the paper margin rectangle.
	 * @param gl the render context
	 */
	private void renderMargin(GL3 gl) {
		gl.glLineWidth(1);
		Rectangle2D.Double rect = getMarginRectangle();
		double yMin = rect.getMinY();
		double yMax = rect.getMaxY();
		double xMin = rect.getMinX();
		double xMax = rect.getMaxX();

		marginMesh.clear();
		marginMesh.setRenderStyle(GL3.GL_LINE_LOOP);
		marginMesh.addColor(0.9f, 0.9f, 0.9f,1.0f);  marginMesh.addVertex( (float)(centerX+xMin), (float)(centerY+yMax), 0);
		marginMesh.addColor(0.9f, 0.9f, 0.9f,1.0f);  marginMesh.addVertex( (float)(centerX+xMax), (float)(centerY+yMax), 0);
		marginMesh.addColor(0.9f, 0.9f, 0.9f,1.0f);  marginMesh.addVertex( (float)(centerX+xMax), (float)(centerY+yMin), 0);
		marginMesh.addColor(0.9f, 0.9f, 0.9f,1.0f);  marginMesh.addVertex( (float)(centerX+xMin), (float)(centerY+yMin), 0);

		marginMesh.render(gl);
	}

	/**
	 * Draw paper as a rectangle.
	 * @param gl the render context
	 */
	private void renderPaper(GL3 gl) {
		float r = (float)paperColor.getRed() / 255.0f;
		float g = (float)paperColor.getGreen() / 255.0f;
		float b = (float)paperColor.getBlue() / 255.0f;

		marginMesh.clear();
		marginMesh.setRenderStyle(GL3.GL_QUADS);
		marginMesh.addColor(r, g, b,1.0f);  marginMesh.addVertex( (float)(centerX+getPaperLeft()), (float)(centerY+getPaperTop()), 0);
		marginMesh.addColor(r, g, b,1.0f);  marginMesh.addVertex( (float)(centerX+getPaperRight()), (float)(centerY+getPaperTop()), 0);
		marginMesh.addColor(r, g, b,1.0f);  marginMesh.addVertex( (float)(centerX+getPaperRight()), (float)(centerY+getPaperBottom()), 0);
		marginMesh.addColor(r, g, b,1.0f);  marginMesh.addVertex( (float)(centerX+getPaperLeft()), (float)(centerY+getPaperBottom()), 0);

		marginMesh.render(gl);
	}

	/**
	 * @return description of the paper.
	 */
	@Override
	public String toString() {
		return String.format(
				"Paper Width=%5.2f Height=%5.2f center(%5.2f,%5.2f) color %s",
				getPaperWidth(), getPaperHeight(), centerX, centerY, paperColor);
	}

	/** 
	 * TODO control values consistency ?
	 * TODO color hase RGB hex string value ?
	 */
	public void loadConfig() {
		logger.debug("loading...");
		paperLeft = Double.parseDouble(paperPreferenceNode.get(PREF_KEY_PAPER_LEFT, Double.toString(paperLeft)));
		paperRight = Double.parseDouble(paperPreferenceNode.get(PREF_KEY_PAPER_RIGHT, Double.toString(paperRight)));
		paperTop = Double.parseDouble(paperPreferenceNode.get(PREF_KEY_PAPER_TOP, Double.toString(paperTop)));
		paperBottom = Double.parseDouble(paperPreferenceNode.get(PREF_KEY_PAPER_BOTTOM, Double.toString(paperBottom)));
		paperMargin = Double.parseDouble(paperPreferenceNode.get(PREF_KEY_PAPER_MARGIN, Double.toString(paperMargin)));
		rotation = Double.parseDouble(paperPreferenceNode.get(PREF_KEY_ROTATION, Double.toString(rotation)));
		int colorFromPref = Integer.parseInt(paperPreferenceNode.get(PREF_KEY_PAPER_COLOR, Integer.toString(paperColor.hashCode())));
		paperColor = new Color(colorFromPref);
		rotationRef = 0;
		centerX=Double.parseDouble(paperPreferenceNode.get(PREF_KEY_PAPER_CENTER_X, Double.toString(rotation)));
		centerY=Double.parseDouble(paperPreferenceNode.get(PREF_KEY_PAPER_CENTER_Y, Double.toString(rotation)));
		logger.debug(this.toString());
	}

	public void saveConfig() {
		logger.debug("saving "+this+"...");
		paperPreferenceNode.putDouble(PREF_KEY_PAPER_LEFT, paperLeft);
		paperPreferenceNode.putDouble(PREF_KEY_PAPER_RIGHT, paperRight);
		paperPreferenceNode.putDouble(PREF_KEY_PAPER_TOP, paperTop);
		paperPreferenceNode.putDouble(PREF_KEY_PAPER_BOTTOM, paperBottom);
		paperPreferenceNode.put(PREF_KEY_PAPER_MARGIN, Double.toString(paperMargin));
		paperPreferenceNode.putDouble(PREF_KEY_ROTATION, rotation);
		paperPreferenceNode.putInt(PREF_KEY_PAPER_COLOR, paperColor.hashCode());
		paperPreferenceNode.putDouble(PREF_KEY_PAPER_CENTER_X, centerX);
		paperPreferenceNode.putDouble(PREF_KEY_PAPER_CENTER_Y, centerY);
	}

	public void setPaperSize(double width, double height, double shiftx, double shifty) {
		this.centerX = shiftx;
		this.centerY = shifty;
		this.paperLeft = -width / 2;
		this.paperRight = width / 2;
		this.paperTop = height / 2;
		this.paperBottom = -height / 2;		
	}

	public Color getPaperColor() {
		return paperColor;
	}

	public void setPaperColor(Color arg0) {
		paperColor = arg0;
	}

	public double getCenterX() {
		return centerX;
	}

	public double getCenterY() {
		return centerY;
	}

	/**
	 * @return paper height in mm.
	 */
	public double getPaperHeight() {
		return paperTop - paperBottom;
	}

	/**
	 * @return paper width in mm.
	 */
	public double getPaperWidth() {
		return paperRight - paperLeft;
	}

	/**
	 * @return absolute paper left edge in mm.
	 */
	public double getPaperLeft() {
		return paperLeft;
	}

	/**
	 * @return absolute paper right edge in mm.
	 */
	public double getPaperRight() {
		return paperRight;
	}


	/**
	 * @return absolute paper top edge in mm.
	 */
	public double getPaperTop() {
		return paperTop;
	}


	/**
	 * @return absolute paper bottom edge in mm.
	 */
	public double getPaperBottom() {
		return paperBottom;
	}

	/**
	 * @param paperMargin 0...1
	 */
	public void setPaperMargin(double paperMargin) {
		if( paperMargin<0 ) paperMargin = 0;
		if( paperMargin>1 ) paperMargin = 1;
		this.paperMargin = paperMargin;
	}

	/**
	 * @return paper margin as a value 0...1.
	 */
	public double getPaperMargin() {
		return paperMargin;
	}

	public Rectangle2D.Double getMarginRectangle() {
		Rectangle2D.Double rectangle = new Rectangle2D.Double();
		rectangle.x = paperLeft * paperMargin;
		rectangle.y = paperBottom * paperMargin;
		rectangle.width = (paperRight-paperLeft) * paperMargin;
		rectangle.height = (paperTop-paperBottom) * paperMargin;
		return rectangle;
	}

	public double getRotation() {
		return this.rotation;
	}

	public void setRotation(double rot) {
		this.rotation = rot;
	}

	public void setRotationRef(double ang) {
		this.rotationRef = ang;
	}
}
