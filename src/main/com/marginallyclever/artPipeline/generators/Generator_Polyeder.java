package com.marginallyclever.artPipeline.generators;


import java.util.ArrayList;

import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.convenience.Turtle;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.makelangelo.Translator;

/**
 * Draws a Polyeder
 * @author Guenther Sohler
 *
 */


public class Generator_Polyeder extends ImageGenerator {
	int size=100;
	int flap=10;
	int modelid=0;

	@Override
	public String getName() {
		return Translator.get("Polyeder");
	}

	public class Model {
		public String name;
		public int []instructions;
	}

	ArrayList<Model> models=null;

	void addModel(String name,int [] instructions)
	{
		Model m = new Model();
		m.name=name;
		m.instructions=instructions;
		models.add(m);
	}

	public String [] getModelNames()
	{
		String [] result = new String[models.size()];
		for(int i=0;i<models.size();i++)
		{
			result[i]=models.get(i).name;
		}
		return result;

	}

	@Override
	public ImageGeneratorPanel getPanel() {
		if(models == null)
		{
			models=new ArrayList<Model>();
			addModel("Cube",new int[] {4,4,1,1,1,4,0,1,0,4,1,1,1,4,0,4,0,0,0,0});
			addModel("Tetrahedron", new int [] {3,3,1,0,3,1,0,3,1,0});
			addModel("Octahedron",new int[] {3,3,0,3,0,1,3,0,1,3,3,1,3,1,1,3,0,1});
			addModel("Dodecahedron",new int[] {5,5,5,0,1,1,1,5,0,1,1,1,5,0,1,1,1,5,0,1,1,1,0,1,5,1,5,5,0,0,0,1,5,0,0,0,1,5,0,0,0,1,5,0,0,0,1,0,0,1});
			addModel("Icosaedron",new int[] {3,3,3,0,1,3,3,3,0,1,3,3,3,0,1,3,3,3,0,1,3,3,3,0,1,1,3,0,1,3,0,1,3,0,1,3,0,1,3,0,1,0});
			addModel("Cuboctahedron",new int[] {4,3,4,0,3,0,1,1,1,3,4,0,3,0,1,1,1,3,4,0,3,0,4,0,0,0,1,1,3,4,0,3,0,1,1,1});
			addModel("Rhombicosidodecahedron",new int[] {5,4,3,0,4,0,3,0,1,1,5,0,4,0,5,0,4,0,1,3,0,1,4,0,1,1,1,1,4,3,0,4,0,3,0,1,1,5,0,4,0,5,0,1,1,1,1,4,3,0,1,1,1,0,3,1,0,4,1,1,0,3,1,0
					,4,1,5,1,4,0,1,3,1,0,4,1,5,1,4,1,1,3,1,0,4,1,1,1,1,3,1,1,4,0,1,1,3,0,1
					,4,1,5,1,4,0,1,3,1,0,4,1,5,1,4,1,1,3,1,0,4,1,1,1,1,3,1,1,4,0,1,1,3,0,1
					,4,0,5,0,4,0,1,3,1,0,4,0,5,0,4,0,1,1,1,1,1,4,3,0,1,1,1,3,0,1
					,4,0,5,0,4,0,1,3,0,1,4,0,1,1,1,1
					});
			addModel("Football",new int[] {6,6,5,0,0,0,0,6,1,1,1,1,1,1,6,1,6,5,0,0,0,0,6,1,1,1,1,1,1
					,6,1,1,5,0,0,0,0,6,1,1,1,1,1,1,1,5,0,0,0,0,6,1,1,1,1,1,1,1,5,0,0,0,0,6,1,1,5,0,0,0,0,1,1,1
					,6,1,6,5,0,0,0,0,6,1,1,1,1,1,1,6,1,6,5,0,0,0,0,6,1,1,1,1,1,1
					,6,1,1,5,0,0,0,0,6,1,1,1,1,1,1,1,5,0,0,0,0,6,1,1,1,1,1,1,1,5,0,0,0,0,6,1,1,5,0,0,0,0,1,1,1,1});
			addModel("Icosidodecahedron",new int[] {5,3,5,1,3,0,5,1,3,0,0,1,1,3,0,0,3,5,1,3,0,5,1,1,1,1,3,0,0,1,0,5,1,3,0,5,1,3,0,0,1,1,3,0,0,1,3,0,5,1,3,0,5,1,3,0,0,1,1,3,0,0,1,3,0,5,1,3,0,5,1,1,1,1,3,0,0,1,3,0,5,1,3,0,0,1,1,3,0,0});
			addModel("Truncated Octahedron",new int[] {4,6,1,1,6,4,0,0,0,0,1,1,4,0,0,0,1,1,6,0,1,6,1,0,1,1,1,1,0,6,1,1,6,4,0,0,0,0,4,0,0,0,1,4,0,0,0,1,1,6,0,1,6,1,0,1,1,1,1,0});
			addModel("Rhombicuboctahedron",new int[] {4,4,3,0,0,4,4,1,1,1,4,3,0,0,1,3,0,0,4,1,1,1,3,0,0,4,1,4,0,0,0,1,4,3,0,0,4,4,1,1,1,4,3,0,0,4,4,1,1,1,1,4,1,1,1,3,0,0,4,1,1,1,3,0,0,4,1,4,0,0,0,1});
		}

		return new Generator_Polyeder_Panel(this);
	}


	public class Transform {
		Point2D org;
		double xabs,yabs,x_x,x_y,y_x,y_y;
		Transform()
		{
			org = new Point2D();
			org.x=0;
			org.y=0;
			x_x=y_y=1;
			x_y=y_x=0;
		}

		Point2D trans(Point2D pt)
		{
			Point2D a=new Point2D();
			a.x=org.x+pt.x*x_x+pt.y*y_x;
			a.y=org.y+pt.x*x_y+pt.y*y_y;
			return a;
		}

		Transform dup()
		{
			Transform t1=new Transform();
			t1.org.x=org.x;
			t1.org.y=org.y;
			t1.x_x=x_x;
			t1.x_y=x_y;
			t1.y_x=y_x;
			t1.y_y=y_y;
			return t1;
		}
		void walk(Point2D d)
		{
			org.x += d.x*x_x + d.y*y_x;
			org.y += d.x*x_y + d.y*y_y;
		}
		void rotate(double ang)
		{
			double x_xn,x_yn,y_xn,y_yn;

			x_xn=x_x*Math.cos(ang)-x_y*Math.sin(ang);
		    x_yn=x_x*Math.sin(ang)+x_y*Math.cos(ang);
		    x_x=x_xn;
		    x_y=x_yn;

		    y_xn=y_x*Math.cos(ang)-y_y*Math.sin(ang);
		    y_yn=y_x*Math.sin(ang)+y_y*Math.cos(ang);
		    y_x=y_xn;
		    y_y=y_yn;

		}
		void dump()
		{
			Log.message(""+org.x+"/"+org.y+" x:"+x_x+"/"+x_y+" "+" y:"+y_x+"/"+y_y+" ");
		}

	}

	int instructionptr;

	void gen_poly(Transform t)
	{
		int i;
		if(models == null) return;
		if(modelid < 0 || modelid >= models.size()) return;
		if(instructionptr >= models.get(modelid).instructions.length) return;
		int code=models.get(modelid).instructions[instructionptr++];

		if(code == 1)
		{
			Point2D pos=new Point2D();
			Point2D abspos;

			pos.x=0;
			pos.y=size/2;
			abspos=t.trans(pos);
			turtle.moveTo(abspos.x,abspos.y);

			turtle.penDown();

			pos.x=-flap;
			pos.y=size/2-flap;
			abspos=t.trans(pos);
			turtle.moveTo(abspos.x,abspos.y);

			pos.y=-pos.y;
			abspos=t.trans(pos);
			turtle.moveTo(abspos.x,abspos.y);

			pos.x=0;
			pos.y=-size/2;
			abspos=t.trans(pos);
			turtle.moveTo(abspos.x,abspos.y);

			turtle.penUp();


		}

		if(code >= 3 && code <= 8)
		{
			t.walk(new Point2D(-size/(2*Math.tan(Math.PI/code)),0));
			double r=size/(2*Math.sin(Math.PI/code));
			for(i=0;i<=code;i++)
			{
				double ang=2*Math.PI*(i-0.5)/(double)code;
				Point2D pos=new Point2D();
				pos.x=r*Math.cos(ang);
				pos.y=r*Math.sin(ang);

				Point2D abspos=t.trans(pos);
				turtle.moveTo(abspos.x,abspos.y);
				if( i == 0) turtle.penDown();
			}
			turtle.penUp();
			for(i=(instructionptr>1)?1:0;i<code;i++)
			{
				Log.message("turn "+i);
				Transform t1=t.dup();
				t1.rotate(2*Math.PI*i/(double)code);
				t1.walk(new Point2D(size/(2*Math.tan(Math.PI/code)),0));
				t1.rotate(Math.PI);
				gen_poly(t1);
			}


		}

	}

	@Override
	public boolean generate() {

		instructionptr=0;

		turtle = new Turtle();
		turtle.penUp();

		Transform t = new Transform();
		Log.message("start");
		gen_poly(t);



	    return true;
	}


	public int getLastSize() {
		return size;
	}

	public int getLastFlap() {
		return flap;
	}

	public int getLastModel() {
		return modelid;
	}

	public void setSize(int intValue) {
		this.size=intValue;

	}

	public void setFlap(int intValue) {
		this.flap=intValue;

	}
	public void setModel(int intValue) {
		this.modelid=intValue;

	}
}
