
public class Point3D {
	float x, y, z;
	
	public Point3D(float xx,float yy,float zz) {
		set(xx,yy,zz);
	}
	public Point3D(double xx,double yy,double zz) {
		set((float)xx,(float)yy,(float)zz);
	}
	
	public void set(float xx,float yy,float zz) {
		x=xx;
		y=yy;
		z=zz;
	}
}