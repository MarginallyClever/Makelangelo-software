package com.marginallyclever.makelangelo.makeart.imageconverter;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.helpers.DrawingHelper;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.imagefilter.Filter_Greyscale;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.select.SelectOneOfMany;
import com.marginallyclever.makelangelo.turtle.Turtle;

import java.util.ArrayList;
import java.util.List;


/**
 * Splits the image into quadrants organized in a quadtree, then draws each quadrant based on selected style.
 * @author Dan Royer
 * @since 7.41.3
 */
public class Converter_Quadtree extends ImageConverterIterative {
	static class Box {
		public int x0,y0;
		public int x1,y1;
		public float average;
		public float error;

		public Box(int x0,int y0,int x1,int y1) {
			this.x0=x0;
			this.y0=y0;
			this.x1=x1;
			this.y1=y1;
		}
	};
	private final List<Box> boxes = new ArrayList<>();
	private TransformedImage img;
	private int iterations;

	private static final String [] styles = {
		Translator.get("Converter_Quadtree.squares"),
		Translator.get("Converter_Quadtree.circles"),
		Translator.get("Converter_Quadtree.xs"),
	};
	private static int cutoff = 128;
	private static int style = 0;

	public Converter_Quadtree() {
		super();

		SelectOneOfMany fieldNoise = new SelectOneOfMany("noiseType",Translator.get("Converter_Quadtree.style"), styles,style);

		add(fieldNoise);
		fieldNoise.addPropertyChangeListener(evt -> {
			style = fieldNoise.getSelectedIndex();
			//fireRestart();
		});

	}

	@Override
	public String getName() {
		return Translator.get("Converter_Quadtree.name");
	}
	public static void setCutoff(int cutoff) {
		Converter_Quadtree.cutoff = cutoff;
	}

	@Override
	public void start(Paper paper, TransformedImage image) {
		super.start(paper, image);

		Filter_Greyscale bw = new Filter_Greyscale(255);
		img = bw.filter(myImage);

		turtle = new Turtle();
		iterations = 0;

		// your code goes here
		boxes.clear();
		Box b = new Box((int)paper.getMarginLeft(),(int)paper.getMarginBottom(),(int)paper.getMarginRight(),(int)paper.getMarginTop());
		//Box b = new Box(0,0,(int)paper.getMarginWidth(),(int)paper.getMarginHeight());
		updateBox(b);
		addBox(b);
	}


	void updateBox(Box b) {
		// get the average color of the box
		float sum=0;
		float c=0;
		for(int y=b.y0;y<b.y1;++y) {
			for (int x = b.x0; x < b.x1; ++x) {
				float p = 255f - img.sample1x1(x, y);
				c++;
				sum += p;
			}
		}
		//float div = (float)Math.log(c);
		//float div = (float)Math.log10(c);  // this log10 makes a huge difference.  also tried sqrt() and various pow()
		//float div = (float)Math.sqrt(c);
		//float div = (float)Math.pow(c,0.4);
		float div = -1f / c + 1f;

		b.average = sum / div;
		sum/=c;

		// from the average, find the error.
		float error=0;
		for(int y=b.y0;y<b.y1;++y) {
			for(int x=b.x0;x<b.x1;++x) {
				float p = 255f-img.sample1x1(x,y);
				error += Math.abs(sum - p);
			}
		}
		b.error = error/c;
	}

	/**
	 * run one "step" of an iterative image conversion process.
	 * @return true if conversion should iterate again.
	 */
	@Override
	public boolean iterate() {
		iterations++;
		lock.lock();
		try {
			// your code goes here
			iterateBox();
			System.out.println("Iteration "+iterations+" boxes="+boxes.size());
		}
		finally {
			lock.unlock();
		}
		return true;
	}

	private void iterateBox() {
		if(boxes.isEmpty()) return;

		// find box with biggest error term
		Box worstBox=boxes.get(0);

		// replace the box with four smaller boxes
		boxes.remove(worstBox);
		int x0=worstBox.x0;
		int y0=worstBox.y0;
		int x2=worstBox.x1;
		int y2=worstBox.y1;
		int x1=(x0+x2)/2;
		int y1=(y0+y2)/2;

		if(x2==x0 && y2==x0)
			return;

		Box [] b4 = {
				new Box(x0, y0, x1, y1),  // nw
				new Box(x1, y0, x2, y1),  // ne
				new Box(x0, y1, x1, y2),  // sw
				new Box(x1, y1, x2, y2),  // se
		};

		for( Box bn : b4 ) {
			updateBox(bn);
			addBox(bn);
		}
	}

	// add this box to the list such that the list stays sorted with biggest average first.
	private void addBox(Box b) {
		for(int i=0;i<boxes.size();++i) {
			if(boxes.get(i).average<b.average) {
				boxes.add(i,b);
				return;
			}
		}
		boxes.add(b);
	}

	/**
	 * called when the user pauses the conversion.  Should generate the {@link Turtle} output.
	 */
	@Override
	public void generateOutput() {
		writeOutTurtle();
		fireConversionFinished();
	}

	@Override
	public void stop() {
		super.stop();
		lock.lock();
		try {
			writeOutTurtle();
		}
		finally {
			lock.unlock();
		}
		fireConversionFinished();
	}

	private void writeOutTurtle() {
		switch (style) {
			case 0 -> writeBoxes();
			case 1 -> writeCircles();
			case 2 -> writeXs();
		}
	}

	@Override
	public void resume() {}


	/**
	 * Callback from {@link com.marginallyclever.makelangelo.preview.PreviewPanel} that it is time to render to the WYSIWYG display.
	 * @param gl2 the render context
	 */
	@Override
	public void render(GL2 gl2) {
		ImageConverterThread thread = getThread();
		if(thread==null || thread.getPaused()) return;

		lock.lock();
		try {
            switch (style) {
                case 0 -> renderBoxes(gl2);
                case 1 -> renderCircles(gl2);
                case 2 -> renderXs(gl2);
            }
		}
		finally {
			lock.unlock();
		}
	}

	private void renderBoxes(GL2 gl2) {
		gl2.glColor3f(0, 0, 0);
		for(Box b : boxes) {
			gl2.glBegin(GL2.GL_LINE_LOOP);
			gl2.glVertex2d(b.x0,b.y0);
			gl2.glVertex2d(b.x1,b.y0);
			gl2.glVertex2d(b.x1,b.y1);
			gl2.glVertex2d(b.x0,b.y1);
			gl2.glEnd();
		}
	}

	private void renderCircles(GL2 gl2) {
		gl2.glColor3f(0, 0, 0);
		for(Box b : boxes) {
			float r= (b.x1-b.x0)/2f;
			DrawingHelper.drawCircle(gl2,(b.x0+b.x1)/2f,(b.y0+b.y1)/2f,r);
		}
	}

	private void renderXs(GL2 gl2) {
		gl2.glBegin(GL2.GL_LINES);
		gl2.glColor3f(0, 0, 0);
		for(Box b : boxes) {
			gl2.glVertex2d(b.x0,b.y0);
			gl2.glVertex2d(b.x1,b.y1);
			gl2.glVertex2d(b.x1,b.y0);
			gl2.glVertex2d(b.x0,b.y1);
		}
		gl2.glEnd();
	}

	private void writeBoxes() {
		for(Box b : boxes) {
			turtle.jumpTo(b.x0,b.y0);
			turtle.moveTo(b.x1,b.y0);
			turtle.moveTo(b.x1,b.y1);
			turtle.moveTo(b.x0,b.y1);
			turtle.moveTo(b.x0,b.y0);
		}
	}

	private void writeCircles() {

	}

	private void writeXs() {
		for(Box b : boxes) {
			turtle.jumpTo(b.x0,b.y0);
			turtle.moveTo(b.x1,b.y1);
			turtle.jumpTo(b.x0,b.y1);
			turtle.moveTo(b.x1,b.y0);
		}
	}
}
