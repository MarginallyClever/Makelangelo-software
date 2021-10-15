package com.marginallyclever.makelangelo.paper;

import java.awt.geom.Rectangle2D;
import java.util.prefs.Preferences;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.makelangelo.preview.PreviewListener;
import com.marginallyclever.util.PreferencesHelper;

public class Paper implements PreviewListener {
	private static final int DEFAULT_WIDTH=420; // mm
	private static final int DEFAULT_HEIGHT=594; // mm
	
	// paper area, in mm
	private double paperLeft;
	private double paperRight;
	private double paperBottom;
	private double paperTop;
	// % from edge of paper.
	private double paperMargin;
	
	private double rotation;
	private double rotationRef;
	
	ColorRGB paperColor = new ColorRGB(255,255,255); // Paper #color
	
	public Paper() {
		// paper area
		double pw = DEFAULT_WIDTH;
		double ph = DEFAULT_HEIGHT;

		paperTop = ph / 2;
		paperBottom = -ph / 2;
		paperLeft = -pw / 2;
		paperRight = pw / 2;
		paperMargin = 0.95;
	}
	
	@Override
	public void render(GL2 gl2) {
		renderPaper(gl2);
		renderMargin(gl2);
	}
	
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

	public void loadConfig() {
		Preferences paperPreferenceNode = 
		PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.PAPER);
		paperLeft = Double.parseDouble(paperPreferenceNode.get("paper_left", Double.toString(paperLeft)));
		paperRight = Double.parseDouble(paperPreferenceNode.get("paper_right", Double.toString(paperRight)));
		paperTop = Double.parseDouble(paperPreferenceNode.get("paper_top", Double.toString(paperTop)));
		paperBottom = Double.parseDouble(paperPreferenceNode.get("paper_bottom", Double.toString(paperBottom)));
		paperMargin = Double.valueOf(paperPreferenceNode.get("paper_margin", Double.toString(paperMargin)));
		rotation = Double.parseDouble(paperPreferenceNode.get("rotation", Double.toString(rotation)));
		rotationRef = 0;
	}
	public void saveConfig() {
		Preferences paperPreferenceNode = 
		PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.PAPER);
		paperPreferenceNode.putDouble("paper_left", paperLeft);
		paperPreferenceNode.putDouble("paper_right", paperRight);
		paperPreferenceNode.putDouble("paper_top", paperTop);
		paperPreferenceNode.putDouble("paper_bottom", paperBottom);
		paperPreferenceNode.put("paper_margin", Double.toString(paperMargin));
		paperPreferenceNode.putDouble("rotation", rotation);
	}

	public void setPaperMargin(double paperMargin) {
		this.paperMargin = paperMargin;
	}

	public void setPaperSize(double width, double height, double shiftx, double shifty) {
		this.paperLeft = -width / 2 + shiftx;
		this.paperRight = width / 2 + shiftx;
		this.paperTop = height / 2 + shifty;
		this.paperBottom = -height / 2 + shifty;
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
		return paperLeft;
	}

	/**
	 * @return paper right edge in mm.
	 */
	// TODO clean up this name
	public double getPaperRight() {
		return paperRight;
	}

	/**
	 * @return paper top edge in mm.
	 */
	// TODO clean up this name
	public double getPaperTop() {
		return paperTop;
	}

	/**
	 * @return paper bottom edge in mm.
	 */
	// TODO clean up this name
	public double getPaperBottom() {
		return paperBottom;
	}

	/**
	 * @return paper left edge in mm.
	 */
	public double getMarginLeft() {
		return getPaperLeft() * getPaperMargin();
	}

	/**
	 * @return paper right edge in mm.
	 */
	public double getMarginRight() {
		return getPaperRight() * getPaperMargin();
	}

	/**
	 * @return paper top edge in mm.
	 */
	public double getMarginTop() {
		return getPaperTop() * getPaperMargin();
	}

	/**
	 * @return paper bottom edge in mm.
	 */
	public double getMarginBottom() {
		return getPaperBottom() * getPaperMargin();
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
	}

	public void setRotationRef(double ang) {
		this.rotationRef = ang;
	}

	public double getRotationRef() {
		return this.rotationRef;
	}
}
