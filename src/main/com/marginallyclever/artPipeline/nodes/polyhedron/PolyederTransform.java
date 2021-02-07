package com.marginallyclever.artPipeline.nodes.polyhedron;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.core.Point2D;

/**
 * Helper class for making relative movements along a path to draw each solid
 * @author Guenther Sohler
 * @since 7.24.0
 */
public class PolyederTransform {
	public Point2D org = new Point2D(0,0);
	public double xabs,yabs,x_x,x_y,y_x,y_y;
	
	public PolyederTransform() {
		x_x=y_y=1;
		x_y=y_x=0;
	}

	public Point2D trans(Point2D pt)
	{
		Point2D a=new Point2D();
		a.x=org.x+pt.x*x_x+pt.y*y_x;
		a.y=org.y+pt.x*x_y+pt.y*y_y;
		return a;
	}

	public PolyederTransform dup()
	{
		PolyederTransform t1=new PolyederTransform();
		t1.org.x=org.x;
		t1.org.y=org.y;
		t1.x_x=x_x;
		t1.x_y=x_y;
		t1.y_x=y_x;
		t1.y_y=y_y;
		return t1;
	}
	
	public void walk(Point2D d)
	{
		org.x += d.x*x_x + d.y*y_x;
		org.y += d.x*x_y + d.y*y_y;
	}
	
	public void rotate(double ang)
	{
		double x_xn,x_yn,y_xn,y_yn;

		double s = Math.sin(ang);
		double c = Math.cos(ang);
		
		x_xn=x_x*c-x_y*s;
	    x_yn=x_x*s+x_y*c;
	    x_x=x_xn;
	    x_y=x_yn;

	    y_xn=y_x*c-y_y*s;
	    y_yn=y_x*s+y_y*c;
	    y_x=y_xn;
	    y_y=y_yn;

	}
	
	public void dump()
	{
		Log.message(""+org.x+"/"+org.y+" x:"+x_x+"/"+x_y+" "+" y:"+y_x+"/"+y_y+" ");
	}
}