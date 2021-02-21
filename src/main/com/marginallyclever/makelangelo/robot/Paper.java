package com.marginallyclever.makelangelo.robot;

import java.util.prefs.Preferences;

import com.jogamp.opengl.GL2;
import com.marginallyclever.core.ColorRGB;
import com.marginallyclever.util.PreferencesHelper;

/**
 * A model of the paper on which the robot will draw.
 * The controller knows about the paper.  The {@link Plotter} does not.
 * @author Dan Royer
 * @since 7.25.0
 */
public class Paper {
	private final double DEFAULT_PAPER_WIDTH = 420.0;  // mm
	private final double DEFAULT_PAPER_HEIGHT = 594.0;  // mm

	public static final PaperSize commonPaperSizes [] = {
		new PaperSize("4A0",1682,2378),
		new PaperSize("2A0",1189,1682),
		new PaperSize("A0",841,1189),
		new PaperSize("A1",594,841),
		new PaperSize("A2",420,594),
		new PaperSize("A3",297,420),
		new PaperSize("A4",210,297),
		new PaperSize("A5",148,210),
		new PaperSize("A6",105,148),
		new PaperSize("A7",74,105),
		new PaperSize("US Half Letter",140,216),
		new PaperSize("US Letter",216,279),
		new PaperSize("US Legal",216,356),
		new PaperSize("US Junior Legal",127,203),
		new PaperSize("US Ledger / Tabloid",279,432),
		new PaperSize("ANSI A",216,279),
		new PaperSize("ANSI B",279,432),
		new PaperSize("ANSI C",432,559),
		new PaperSize("ANSI D",559,864),
		new PaperSize("ANSI E",864,1118),
		new PaperSize("Arch A",229,305),
		new PaperSize("Arch B",305,457),
		new PaperSize("Arch C",457,610),
		new PaperSize("Arch D",610,914),
		new PaperSize("Arch E",914,1219),
		new PaperSize("Arch E1",762,1067)
	};
	
	// dimensions, in mm
	private double left;
	private double right;
	private double bottom;
	private double top;
	
	// landscape/portrait?
	private double rotation;
	
	// % from edge of paper.
	private double margin;
	
	private ColorRGB color;

	public Paper() {
		// default paper A2 = 420 x 594mm
		top = DEFAULT_PAPER_HEIGHT/2;
		bottom = -DEFAULT_PAPER_HEIGHT/2;
		left = -DEFAULT_PAPER_WIDTH/2;
		right = DEFAULT_PAPER_WIDTH/2;
		margin = 0.9;
		color = new ColorRGB(255,255,255);
	}

	/**
	 * @return paper height in mm.
	 */
	public double getHeight() {
		return top - bottom;
	}

	/**
	 * @return paper width in mm.
	 */
	public double getWidth() {
		return right - left;
	}

	/**
	 * @return paper left edge in mm.
	 */
	public double getLeft() {
		return left;
	}

	/**
	 * @return paper right edge in mm.
	 */
	public double getRight() {
		return right;
	}

	/**
	 * @return paper top edge in mm.
	 */
	public double getTop() {
		return top;
	}

	/**
	 * @return paper bottom edge in mm.
	 */
	public double getBottom() {
		return bottom;
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
	 * @return paper left edge in mm.
	 */
	public double getMarginLeft() {
		return getLeft() * getMarginPercent();
	}

	/**
	 * @return paper right edge in mm.
	 */
	public double getMarginRight() {
		return getRight() * getMarginPercent();
	}

	/**
	 * @return paper top edge in mm.
	 */
	public double getMarginTop() {
		return getTop() * getMarginPercent();
	}

	/**
	 * @return paper bottom edge in mm.
	 */
	public double getMarginBottom() {
		return getBottom() * getMarginPercent();
	}

	/**
	 * @return paper margin %.
	 */
	public double getMarginPercent() {
		return margin;
	}

	public boolean isConfigured() {
		return (top > bottom && right > left);
	}
	
	public void loadConfigFromLocal(long robotUID) {
		Preferences topLevelMachinesPreferenceNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);
		Preferences uniqueMachinePreferencesNode = topLevelMachinesPreferenceNode.node(Long.toString(robotUID));
		
		left   = Double.parseDouble(uniqueMachinePreferencesNode.get("paper_left",Double.toString(left)));
		right  = Double.parseDouble(uniqueMachinePreferencesNode.get("paper_right",Double.toString(right)));
		top    = Double.parseDouble(uniqueMachinePreferencesNode.get("paper_top",Double.toString(top)));
		bottom = Double.parseDouble(uniqueMachinePreferencesNode.get("paper_bottom",Double.toString(bottom)));
		rotation    = Double.parseDouble(uniqueMachinePreferencesNode.get("rotation",Double.toString(rotation)));

		int r = uniqueMachinePreferencesNode.getInt("paperColorR", color.getRed());
		int g = uniqueMachinePreferencesNode.getInt("paperColorG", color.getGreen());
		int b = uniqueMachinePreferencesNode.getInt("paperColorB", color.getBlue());
		color = new ColorRGB(r,g,b);

		margin = Double.valueOf(uniqueMachinePreferencesNode.get("paper_margin", Double.toString(margin)));
	}
	
	public void saveConfigToLocal(long robotUID) {
		Preferences topLevelMachinesPreferenceNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);
		Preferences uniqueMachinePreferencesNode = topLevelMachinesPreferenceNode.node(Long.toString(robotUID));
		
		uniqueMachinePreferencesNode.putDouble("paper_left", left);
		uniqueMachinePreferencesNode.putDouble("paper_right", right);
		uniqueMachinePreferencesNode.putDouble("paper_top", top);
		uniqueMachinePreferencesNode.putDouble("paper_bottom", bottom);
		uniqueMachinePreferencesNode.putDouble("rotation", rotation);
		
		uniqueMachinePreferencesNode.putInt("paperColorR", color.getRed());
		uniqueMachinePreferencesNode.putInt("paperColorG", color.getGreen());
		uniqueMachinePreferencesNode.putInt("paperColorB", color.getBlue());

		uniqueMachinePreferencesNode.put("paper_margin", Double.toString(margin));
	}
	
	public void setPaperMargin(double paperMargin) {
		this.margin = paperMargin;	
	}

	public void setPaperSize(double width, double height, double shiftx, double shifty) {
		this.left = -width/2 + shiftx;
		this.right = width/2 + shiftx;
		this.top = height/2 + shifty;
		this.bottom = -height/2+shifty;
	}
	
	public ColorRGB getPaperColor() {
		return color;
	}
	
	public void setPaperColor(ColorRGB arg0) {
		color = arg0;
	}

	public double getRotation() {
		return this.rotation;
	}

	public void setRotation(double rot) {
		this.rotation=rot;
	}
	
	public void render(GL2 gl2) {
		// paper rectangle
		ColorRGB c = getPaperColor();
		gl2.glColor3d(
				(double)c.getRed() / 255.0, 
				(double)c.getGreen() / 255.0, 
				(double)c.getBlue() / 255.0);
		gl2.glBegin(GL2.GL_TRIANGLE_FAN);
		gl2.glVertex2d(getLeft(),  getTop());
		gl2.glVertex2d(getRight(), getTop());
		gl2.glVertex2d(getRight(), getBottom());
		gl2.glVertex2d(getLeft(),  getBottom());
		gl2.glEnd();

		// margins
		gl2.glColor3f(0.9f, 0.9f, 0.9f);
		gl2.glBegin(GL2.GL_LINE_LOOP);
		gl2.glVertex2d(getMarginLeft(),  getMarginTop());
		gl2.glVertex2d(getMarginRight(), getMarginTop());
		gl2.glVertex2d(getMarginRight(), getMarginBottom());
		gl2.glVertex2d(getMarginLeft(),  getMarginBottom());
		gl2.glEnd();
	}

	/**
	 * Must match commonPaperSizes
	 * @param paperWidth mm
	 * @param paperHeight mm
	 * @return the index from the commonPaperSizes list.
	 */
	public int getCurrentPaperSizeChoice(double paperWidth,double paperHeight) {
		int i;
		for(i=0;i<commonPaperSizes.length;++i) {
			if(paperWidth == commonPaperSizes[i].width && 
				paperHeight == commonPaperSizes[i].height)
				return i+1;
		}

		return 0;
	}
}
