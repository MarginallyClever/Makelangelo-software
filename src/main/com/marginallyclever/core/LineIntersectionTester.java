package com.marginallyclever.core;

/**
 * @see <a href='https://www.geeksforgeeks.org/check-if-two-given-line-segments-intersect/'>reference</a>
 * @author Dan Royer
 * @since 7.25.0
 */
public class LineIntersectionTester {
	// Given three colinear Point2Ds p, q, r, the function checks if q lies on line segment 'pr'
	static private boolean onSegment(Point2D p, Point2D q, Point2D r) {
	    if (q.x <= Math.max(p.x, r.x) && q.x >= Math.min(p.x, r.x) &&
	        q.y <= Math.max(p.y, r.y) && q.y >= Math.min(p.y, r.y))
	    return true;
	  
	    return false;
	}
	
	// To find orientation of ordered triplet (p, q, r).
	// The function returns following values
	// 0 --> p, q and r are colinear
	// 1 --> Clockwise
	// 2 --> Counterclockwise
	static private int orientation(Point2D p, Point2D q, Point2D r) {
	    // See https://www.geeksforgeeks.org/orientation-3-ordered-Points/
	    // for details of below formula.
	    double val = (q.y - p.y) * (r.x - q.x) -
	                 (q.x - p.x) * (r.y - q.y);
	  
	    if (val < 0.01) return 0; // colinear
	  
	    return (val > 0)? 1: 2; // clock or counterclock wise
	} 
	
	// The main function that returns true if line segment 'p1q1' and 'p2q2' intersect.
	static public boolean doIntersect(Point2D p1, Point2D q1, Point2D p2, Point2D q2) {
	    // Find the four orientations needed for general and special cases
	    int o1 = orientation(p1, q1, p2);
	    int o2 = orientation(p1, q1, q2);
	    int o3 = orientation(p2, q2, p1);
	    int o4 = orientation(p2, q2, q1);
	  
	    // General case
	    if (o1 != o2 && o3 != o4)
	        return true;
	  
	    // Special Cases
	    // p1, q1 and p2 are colinear and p2 lies on segment p1q1
	    if (o1 == 0 && onSegment(p1, p2, q1)) return true;
	  
	    // p1, q1 and q2 are colinear and q2 lies on segment p1q1
	    if (o2 == 0 && onSegment(p1, q2, q1)) return true;
	  
	    // p2, q2 and p1 are colinear and p1 lies on segment p2q2
	    if (o3 == 0 && onSegment(p2, p1, q2)) return true;
	  
	    // p2, q2 and q1 are colinear and q1 lies on segment p2q2
	    if (o4 == 0 && onSegment(p2, q1, q2)) return true;
	  
	    return false; // Doesn't fall in any of the above cases
	}
}
