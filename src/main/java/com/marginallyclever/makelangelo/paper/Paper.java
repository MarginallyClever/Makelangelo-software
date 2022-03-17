package com.marginallyclever.makelangelo.paper;

import java.awt.geom.Rectangle2D;
import java.util.prefs.Preferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.makelangelo.preview.PreviewListener;
import com.marginallyclever.util.PreferencesHelper;

public class Paper implements PreviewListener {

	public static final int DEFAULT_WIDTH = 420; // mm
	public static final int DEFAULT_HEIGHT = 594; // mm

	private static final Logger logger = LoggerFactory.getLogger(Paper.class);

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
	private double paperMargin;
	
	// not used by renderer, ... need a coordinats reccalculation of the paper corner position and a reveiw of the render algo ?
	private double rotation;
	private double rotationRef;

	// shift apply to the center of the paper
	private double centerX=0.0d;
	private double centerY=0.0d;
	
	ColorRGB paperColor = new ColorRGB(255,255,255); // Paper #color
	
	public Paper() {
		// paper area (default values)
		setPaperSize(DEFAULT_WIDTH, DEFAULT_HEIGHT, 0, 0);		
		
		paperMargin = 0.95;

		// If prefs values exist this load the pref values using last setPaperSize(...) setted values as default.
		loadConfig();
	}
	
	@Override
	public void render(GL2 gl2) {
		renderPaper(gl2);
		renderMargin(gl2);
	}
	
	/**
	 * TODO review to use rotation ... ? but no src attached to the jar so ... ???
	 * The trick is that concept have been mix
	 * getPaper* getMargin* for distances from the center coordinate of the machine.
	 * @param gl2 
	 */
	private void renderMargin(GL2 gl2) {
		gl2.glLineWidth(1);
		gl2.glColor3f(0.9f, 0.9f, 0.9f); // Paper margin line #color
		gl2.glBegin(GL2.GL_LINE_LOOP);
		
		gl2.glVertex2d(getMarginLeft(), getMarginTop());
		gl2.glVertex2d(getMarginRight(), getMarginTop());
		gl2.glVertex2d(getMarginRight(), getMarginBottom());
		gl2.glVertex2d(getMarginLeft(), getMarginBottom());
		gl2.glEnd();
	}

	/**
	 * TODO review to take in account rotation ...
	 * The trick is that concept have been mix
	 * this.paper* for distances from the center of the paper.
	 * getPaper* getMargin* for distances from the center coordinate of the machine.
	 * @param gl2 
	 */
	private void renderPaper(GL2 gl2) {
		gl2.glColor3d(
				(double)paperColor.getRed() / 255.0, 
				(double)paperColor.getGreen() / 255.0, 
				(double)paperColor.getBlue() / 255.0);
		gl2.glBegin(GL2.GL_TRIANGLE_FAN);
		gl2.glVertex2d(getPaperLeft(), getPaperTop());
		gl2.glVertex2d(getPaperRight(), getPaperTop());
		gl2.glVertex2d(getPaperRight(), getPaperBottom());
		gl2.glVertex2d(getPaperLeft(), getPaperBottom());
		gl2.glEnd();
	}

	/**
	    for debug purpose can be modified.
	    @return description (position left,right,top,bottom and deducted width and height) of the paper.
	*/
	@Override
	public String toString() {
		return String.format(
				"Paper Width = %5.2f Height = %5.2f (left=%5.2f right=%5.2f top=%5.2f bottom=%5.2f) centerShift(X=%5.2f Y=%5.2f) color %s",
				getPaperWidth(), getPaperHeight(), paperLeft, paperRight, paperTop, paperBottom, centerX, centerY, paperColor);
	}

	/** 
	 * TODO control values consictancy ? 
	 * TODO color hase RGB hexa string value ?
	 */
	public void loadConfig() {
		logger.debug("loadConfig() before "+this.toString());
		paperLeft = Double.parseDouble(paperPreferenceNode.get(PREF_KEY_PAPER_LEFT, Double.toString(paperLeft)));
		paperRight = Double.parseDouble(paperPreferenceNode.get(PREF_KEY_PAPER_RIGHT, Double.toString(paperRight)));
		paperTop = Double.parseDouble(paperPreferenceNode.get(PREF_KEY_PAPER_TOP, Double.toString(paperTop)));
		paperBottom = Double.parseDouble(paperPreferenceNode.get(PREF_KEY_PAPER_BOTTOM, Double.toString(paperBottom)));
		paperMargin = Double.parseDouble(paperPreferenceNode.get(PREF_KEY_PAPER_MARGIN, Double.toString(paperMargin)));
		rotation = Double.parseDouble(paperPreferenceNode.get(PREF_KEY_ROTATION, Double.toString(rotation)));
		int colorFromPref = Integer.parseInt(paperPreferenceNode.get(PREF_KEY_PAPER_COLOR, Integer.toString(paperColor.toInt())));
		paperColor = new ColorRGB(colorFromPref);
		rotationRef = 0;
		centerX=Double.parseDouble(paperPreferenceNode.get(PREF_KEY_PAPER_CENTER_X, Double.toString(rotation)));
		centerY=Double.parseDouble(paperPreferenceNode.get(PREF_KEY_PAPER_CENTER_Y, Double.toString(rotation)));
		logger.debug("loadConfig() after "+this.toString());
	}

	public void saveConfig() {
		logger.debug("saveConfig() "+this.toString() );
		paperPreferenceNode.putDouble(PREF_KEY_PAPER_LEFT, paperLeft);
		paperPreferenceNode.putDouble(PREF_KEY_PAPER_RIGHT, paperRight);
		paperPreferenceNode.putDouble(PREF_KEY_PAPER_TOP, paperTop);
		paperPreferenceNode.putDouble(PREF_KEY_PAPER_BOTTOM, paperBottom);
		paperPreferenceNode.put(PREF_KEY_PAPER_MARGIN, Double.toString(paperMargin));
		paperPreferenceNode.putDouble(PREF_KEY_ROTATION, rotation);
		paperPreferenceNode.putInt(PREF_KEY_PAPER_COLOR, paperColor.toInt());
		paperPreferenceNode.putDouble(PREF_KEY_PAPER_CENTER_X, centerX);
		paperPreferenceNode.putDouble(PREF_KEY_PAPER_CENTER_Y, centerY);
	}

	public void setPaperMargin(double paperMargin) {
		this.paperMargin = paperMargin;
		saveConfig();
	}

	public void setPaperSize(double width, double height, double shiftx, double shifty) {
		this.centerX=shiftx;
		this.centerY=shifty;
		this.paperLeft = -width / 2;
		this.paperRight = width / 2;
		this.paperTop = height / 2;
		this.paperBottom = -height / 2;		
	}
	
	public Rectangle2D.Double getMarginRectangle() {
		Rectangle2D.Double rectangle = new Rectangle2D.Double();
		rectangle.x = getMarginLeft();
		rectangle.y = getMarginBottom();
		rectangle.width = getMarginRight() - rectangle.x;
		rectangle.height = getMarginTop() - rectangle.y;
		return rectangle;
	}

	// TODO clean up this name
	public ColorRGB getPaperColor() {
		return paperColor;
	}

	// TODO clean up this name
	public void setPaperColor(ColorRGB arg0) {
		paperColor = arg0;
		saveConfig();
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
	// TODO clean up this name
	public double getPaperHeight() {
		return paperTop - paperBottom;
	}

	/**
	 * @return paper width in mm.
	 */
	// TODO clean up this name
	public double getPaperWidth() {
		return paperRight - paperLeft;
	}

	/**
	 * @return paper left edge in mm.
	 */
	// TODO clean up this name
	public double getPaperLeft() {
		return paperLeft + getCenterX();
	}

	/**
	 * @return paper right edge in mm.
	 */
	// TODO clean up this name
	public double getPaperRight() {
		return paperRight + getCenterX();
	}

	/**
	 * @return paper top edge in mm.
	 */
	// TODO clean up this name
	public double getPaperTop() {
		return paperTop + getCenterY();
	}

	/**
	 * @return paper bottom edge in mm.
	 */
	// TODO clean up this name
	public double getPaperBottom() {
		return paperBottom + getCenterY();
	}

	/**
	 * @return paper left edge in mm.
	 */
	public double getMarginLeft() {
		return getPaperMargin() * paperLeft + getCenterX();
	}

	/**
	 * @return paper right edge in mm.
	 */
	public double getMarginRight() {
		return getPaperMargin() * paperRight + getCenterX();
	}

	/**
	 * @return paper top edge in mm.
	 */
	public double getMarginTop() {
		return getPaperMargin() * paperTop + getCenterY();
	}

	/**
	 * @return paper bottom edge in mm.
	 */
	public double getMarginBottom() {
		return getPaperMargin() * paperBottom + getCenterY();
	}

	/**
	 * @return paper width in mm.
	 */
	public double getMarginHeight() {
		return getMarginTop() - getMarginBottom();
	}

	/**
	 * @return paper width in mm.
	 */
	public double getMarginWidth() {
		return getMarginRight() - getMarginLeft();
	}

	/**
	 * @return paper margin %.
	 */
	// TODO clean up this name
	public double getPaperMargin() {
		return paperMargin;
	}

	// TODO clean up this name
	public boolean isPaperConfigured() {
		return (paperTop > paperBottom && paperRight > paperLeft);
	}

	// TODO clean up this name
	public boolean isInsidePaperMargins(double x,double y) {
		if( x < getMarginLeft()  ) return false;
		if( x > getMarginRight() ) return false;
		if( y < getMarginBottom()) return false;
		if( y > getMarginTop()   ) return false;
		return true;
	}

	public double getRotation() {
		return this.rotation;
	}

	public void setRotation(double rot) {
		this.rotation = rot;
		saveConfig();
	}

	public void setRotationRef(double ang) {
		this.rotationRef = ang;
		saveConfig();
	}

	public double getRotationRef() {
		return this.rotationRef;
	}
}
