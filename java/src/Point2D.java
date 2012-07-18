
public class Point2D {
	float x, y;
	
	public Point2D(float xx,float yy) {
		set(xx,yy);
	}
	public Point2D(double xx,double yy) {
		set((float)xx,(float)yy);
	}
	
	public void set(float xx,float yy) {
		x=xx;
		y=yy;
	}
}