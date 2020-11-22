package com.marginallyclever.artPipeline.generators;


import com.marginallyclever.convenience.Turtle;
import com.marginallyclever.makelangelo.Translator;

/**
 * Draws a border around the paper.  Uses current paper settings.
 * @author Dan Royer
 *
 */
public class Generator_Package extends ImageGenerator {
	
	int width=200;
	int length=200;
	int height=100;
	
	@Override
	public String getName() {
		return Translator.get("Package");
	}

	@Override
	public ImageGeneratorPanel getPanel() {
		return new Generator_Package_Panel(this);
	}

	
	void drawRect(int x1,int y1,int x2,int y2)
	{
		turtle.moveTo(x1,y1);
		turtle.penDown();
		turtle.moveTo(x2,y1);
		turtle.moveTo(x2,y2);
		turtle.moveTo(x1,y2);
		turtle.moveTo(x1,y1);
		turtle.penUp();
	}
	
	void drawLine(int x1,int y1,int x2,int y2)
	{
		turtle.moveTo(x1,y1);
		turtle.penDown();
		turtle.moveTo(x2,y2);
		turtle.penUp();

	}

	@Override
	public boolean generate() {
		int ytot=2*width+3*height+20;
		int xtot=length+2*height;
		
		int x1=-xtot/2;
		int x2=x1+height;
		int x3=x2+length;
		int x4=x3+height;
		
		int y1=-ytot/2;
		int y2=y1+20;
		int y3=y2+width;
		int y4=y3+height;
		int y5=y4+width;
		int y6=y5+height;
		int y7=y6+height+5;
		

		turtle = new Turtle();
		turtle.penUp();
		// show extent
		turtle.moveTo(x1,y1);
		turtle.moveTo(x4,y1);
		turtle.moveTo(x4,y7);
		turtle.moveTo(x1,y7);
		turtle.moveTo(x1,y1);
		// start drawing

		drawRect(x2,y2,x3,y7);
		drawRect(x1,y3,x4,y6);

		drawLine(x1,y4,x4,y4);
		drawLine(x1,y5,x4,y5);
		
		drawLine(x1,y3,x2,y4); // 4 diags
		drawLine(x3,y5,x4,y6);
		drawLine(x1,y6,x2,y5);
		drawLine(x3,y4,x4,y3);

		drawRect(x2+20,y6-1,x3-20,y6+1); // lasche
		drawRect(x2,y5-1,x3,y5+1); // lasche
		drawRect(x2+20,y1,x3-20,y2); // lasche


		
	    return true;
	}

	
	public int getLastWidth() {
		return width;
	}

	public int getLastHeight() {
		return height;
	}

	public int getLastLength() {
		return length;
	}

	public void setWidth(int intValue) {
		this.width=intValue;
		
	}

	public void setHeight(int intValue) {
		this.height=intValue;
		
	}
	public void setLength(int intValue) {
		this.length=intValue;
		
	}
}
