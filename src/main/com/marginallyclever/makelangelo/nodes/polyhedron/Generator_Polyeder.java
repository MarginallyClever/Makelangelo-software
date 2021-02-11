package com.marginallyclever.makelangelo.nodes.polyhedron;

import java.util.ArrayList;

import com.marginallyclever.core.Point2D;
import com.marginallyclever.core.log.Log;
import com.marginallyclever.core.turtle.Turtle;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.nodes.TurtleGenerator;

/**
 * Draws a papercraft Achimedean Solid (https://en.wikipedia.org/wiki/Archimedean_solid).  Cut, crease, fold, and glue to assemble.
 * Model descriptions are recursive path instructions.  for example, {3,3,1,0,3,1,0,3,1,0}.  The first '3' means draw a triangle.  
 * then for each side of that triangle, {3,1,0}, or draw another triangle and add a flap on the first side and nothing on the second.
 * this recursively creates the shapes needed.
 * 
 * @author Guenther Sohler
 * @since 7.24.0
 *
 */
public class Generator_Polyeder extends TurtleGenerator {
	// length of a long side
	protected int size=100;
	
	// size of fold + glue flap 
	protected int flap=10;

	// list of all available model shapes
	protected ArrayList<PolyederModel> models=null;
	// selected model
	protected int modelid=0;

	public int instructionPtr;

	
	public Generator_Polyeder() {
		super();
		
		models=new ArrayList<PolyederModel>();
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
	
	@Override
	public String getName() {
		return Translator.get("Generator_Polyeder.name");
	}

	void addModel(String name,int [] instructions)
	{
		PolyederModel m = new PolyederModel();
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

	void geneneratePolygonStep(Turtle turtle,PolyederTransform t) {
		int i;
		if(models == null) return;
		if(modelid < 0 || modelid >= models.size()) return;
		if(instructionPtr >= models.get(modelid).instructions.length) return;
		int code=models.get(modelid).instructions[instructionPtr++];

		if(code == 1)
		{
			// draw a flap
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
			// polygon with `code` number of points.
			
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
				if(i == 0) turtle.penDown();
			}
			turtle.penUp();
			// consider a 3 sided shape (code=3).  the very first shape will repeat this 3 times.  all others will repeat this twice.
			// this way following with '1,0' would mean "add a flap on the first side and nothing on the second".
			for(i=(instructionPtr>1)?1:0;i<code;i++)
			{
				Log.message("turn "+i);
				PolyederTransform t1=t.dup();
				t1.rotate(2*Math.PI*i/(double)code);
				t1.walk(new Point2D(size/(2*Math.tan(Math.PI/code)),0));
				t1.rotate(Math.PI);
				// recursion.
				geneneratePolygonStep(turtle,t1);
			}
		}
	}

	@Override
	public boolean iterate() {
		instructionPtr=0;

		Turtle turtle = new Turtle();
		turtle.penUp();

		PolyederTransform t = new PolyederTransform();
		Log.message("start");
		geneneratePolygonStep(turtle,t);
		Log.message("end");

		outputTurtle.setValue(turtle);
		
	    return false;
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
