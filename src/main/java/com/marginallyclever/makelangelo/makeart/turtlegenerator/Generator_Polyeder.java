package com.marginallyclever.makelangelo.makeart.turtlegenerator;


import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.donatello.select.SelectInteger;
import com.marginallyclever.donatello.select.SelectOneOfMany;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Point2d;
import java.util.ArrayList;

/**
 * Draws a papercraft <a href="https://en.wikipedia.org/wiki/Archimedean_solid">Achimedean Solid</a>.  Cut, crease, fold, and glue to assemble.
 * Model descriptions are recursive path instructions.  for example, {3,3,1,0,3,1,0,3,1,0}.  The first '3' means draw a triangle.  
 * then for each side of that triangle, {3,1,0}, or draw another triangle and add a flap on the first side and nothing on the second.
 * this recursively creates the shapes needed.
 * 
 * @author Guenther Sohler
 * @since 7.24.0
 *
 */
public class Generator_Polyeder extends TurtleGenerator {

	private static final Logger logger = LoggerFactory.getLogger(Generator_Polyeder.class);
	/**
	 * Helper class that describe a solid
	 * @author Guenther Sohler
	 * @since 7.24.0
	 */
	public static class Model {
		public String name;
		public int []instructions;
	}

	/**
	 * Helper class for making relative movements along a path to draw each solid
	 * @author Guenther Sohler
	 * @since 7.24.0
	 */
	public static class Transform {
		public Point2d org;
		public double x_x,x_y,y_x,y_y;
		
		public Transform()
		{
			org = new Point2d();
			org.x=0;
			org.y=0;
			x_x=y_y=1;
			x_y=y_x=0;
		}

		public Point2d trans(Point2d pt)
		{
			Point2d a=new Point2d();
			a.x=org.x+pt.x*x_x+pt.y*y_x;
			a.y=org.y+pt.x*x_y+pt.y*y_y;
			return a;
		}

		public Transform dup()
		{
			Transform t1= new Transform();
			t1.org.x=org.x;
			t1.org.y=org.y;
			t1.x_x=x_x;
			t1.x_y=x_y;
			t1.y_x=y_x;
			t1.y_y=y_y;
			return t1;
		}
		
		public void walk(Point2d d)
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
			logger.debug("{}/{} x:{}/{} y:{}/{}", org.x, org.y, x_x, x_y, y_x, y_y);
		}
	}
	
	// length of a long side
	protected int size=100;
	// size of fold + glue flap 
	protected int flap=10;
	// selected model
	protected int modelid=0;
	// list of all available model shapes
	protected final ArrayList<Model> models = new ArrayList<>();

	public int instructionPtr;

	public Generator_Polyeder() {
		super();

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

		SelectInteger selectSize;
		SelectInteger selectFlap;
		SelectOneOfMany selectModel;
		String [] models=getModelNames();

		add(selectSize = new SelectInteger("size",Translator.get("Size"),getLastSize()));
		selectSize.addSelectListener(evt->{
			setSize(((Number)selectSize.getValue()).intValue());
			generate();
		});
		add(selectFlap = new SelectInteger("flap",Translator.get("Flap"),getLastFlap()));
		selectFlap.addSelectListener(evt->{
			setFlap(((Number)selectFlap.getValue()).intValue());
			generate();
		});
		add(selectModel = new SelectOneOfMany("model",Translator.get("Model"),models,getLastModel()));
		selectModel.addSelectListener(evt->{
			setModel(selectModel.getSelectedIndex());
			generate();
		});
	}
	
	@Override
	public String getName() {
		return Translator.get("Polyeder");
	}

	void addModel(String name,int [] instructions) {
		Model m = new Model();
		m.name=name;
		m.instructions=instructions;
		models.add(m);
	}

	public String [] getModelNames() {
		String [] result = new String[models.size()];
		for(int i=0;i<models.size();i++)
		{
			result[i]=models.get(i).name;
		}
		return result;

	}

	private void geneneratePolygonStep(Turtle turtle,Transform t) {
		int i;
		if(modelid < 0 || modelid >= models.size()) return;
		if(instructionPtr >= models.get(modelid).instructions.length) return;
		int code=models.get(modelid).instructions[instructionPtr++];

		if(code == 1)
		{
			// draw a flap
			Point2d pos=new Point2d();
			Point2d abspos;

			pos.x=0;
			pos.y=size/2f;
			abspos=t.trans(pos);
			turtle.moveTo(abspos.x,abspos.y);

			turtle.penDown();

			pos.x=-flap;
			pos.y=size/2f-flap;
			abspos=t.trans(pos);
			turtle.moveTo(abspos.x,abspos.y);

			pos.y=-pos.y;
			abspos=t.trans(pos);
			turtle.moveTo(abspos.x,abspos.y);

			pos.x=0;
			pos.y=-size/2f;
			abspos=t.trans(pos);
			turtle.moveTo(abspos.x,abspos.y);

			turtle.penUp();
		}

		if(code >= 3 && code <= 8)
		{
			// polygon with `code` number of points.
			
			t.walk(new Point2d(-size/(2*Math.tan(Math.PI/code)),0));
			double r=size/(2*Math.sin(Math.PI/code));
			for(i=0;i<=code;i++)
			{
				double ang=2*Math.PI*(i-0.5)/(double)code;
				Point2d pos=new Point2d();
				pos.x=r*Math.cos(ang);
				pos.y=r*Math.sin(ang);

				Point2d abspos=t.trans(pos);
				turtle.moveTo(abspos.x,abspos.y);
				if(i == 0) turtle.penDown();
			}
			turtle.penUp();
			// consider a 3 sided shape (code=3).  the very first shape will repeat this 3 times.  all others will repeat this twice.
			// this way following with '1,0' would mean "add a flap on the first side and nothing on the second".
			for(i=(instructionPtr>1)?1:0;i<code;i++)
			{
				logger.debug("turn {}", i);
				Transform t1=t.dup();
				t1.rotate(2*Math.PI*i/(double)code);
				t1.walk(new Point2d(size/(2*Math.tan(Math.PI/code)),0));
				t1.rotate(Math.PI);
				// recursion.
				geneneratePolygonStep(turtle,t1);
			}
		}
	}

	@Override
	public void generate() {
		instructionPtr=0;

		Turtle turtle = new Turtle();
		turtle.penUp();

		Transform t = new Transform();
		logger.debug("start");
		geneneratePolygonStep(turtle,t);
		logger.debug("end");

		turtle.translate(myPaper.getCenterX(),myPaper.getCenterY());

		notifyListeners(turtle);
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
