package com.marginallyclever.gcode;

import java.awt.Color;

/*
 * GCodeNodes are used to optimize the graphics rendering pipeline.
 */
public class GCodeNode {
	public double x1;
	public double y1;
	public double z1;
	public double x2;
	public double y2;
	public double z2;
	public Color color;
	public int lineNumber;
	public GCodeNodeType type;

	public enum GCodeNodeType {
		POS, TOOL
	}
	
	public GCodeNode() {}
	
	public GCodeNode(int lineNumber, GCodeNodeType type, double x1, double y1, double x2, double y2, Color color) {
		this.lineNumber = lineNumber;
		this.x1 = x1;
		this.y1 = y1;
		this.color=color;
		this.x2 = x2;
		this.y2 = y2;
		this.type = type;
	}
}