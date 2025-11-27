package com.marginallyclever.makelangelo.makeart.turtlegenerator.fractal;

import com.marginallyclever.donatello.select.SelectRandomSeed;
import com.marginallyclever.donatello.select.SelectReadOnlyText;
import com.marginallyclever.donatello.select.SelectSlider;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.TurtleGenerator;
import com.marginallyclever.makelangelo.makeart.turtletool.ResizeTurtleToPaperAction;
import com.marginallyclever.makelangelo.turtle.Turtle;

import java.util.Random;

/**
 * L System fractal
 * @author Dan Royer
 */
@Deprecated(since = "7.68.0")
public class Generator_LSystemTree extends TurtleGenerator {
	private static int order = 4; // controls complexity of curve
	private static double angleSpan = 120;
	private static int numBranches = 3;
	private static int noise = 0;
	private static double orderScale = 0.76f;
	private final Random random = new Random();
	private static int seed=0xDEADBEEF;

	public Generator_LSystemTree() {
		super();

		SelectSlider field_order;
		SelectSlider field_branches;
		SelectSlider field_orderScale;
		SelectSlider field_angle;
		SelectSlider field_noise;

		SelectRandomSeed selectRandomSeed = new SelectRandomSeed("randomSeed",Translator.get("Generator.randomSeed"),seed);
		add(selectRandomSeed);
		selectRandomSeed.addSelectListener(evt->{
			seed = (int)evt.getNewValue();
			random.setSeed(seed);
			generate();
		});

		add(field_order      = new SelectSlider("order",Translator.get("HilbertCurveOrder"),10,1,getOrder()));
		field_order.addSelectListener(evt->{
			setOrder(field_order.getValue());
			generate();
		});

		add(field_branches   = new SelectSlider("branches",Translator.get("LSystemBranches"),8,1,getBranches()));
		field_branches.addSelectListener(evt->{
			setBranches(field_branches.getValue());
			generate();
		});

		add(field_orderScale = new SelectSlider("scale",Translator.get("LSystemOrderScale"),100,1,(int)(getScale()*100)));
		field_orderScale.addSelectListener(evt->{
			setScale(field_orderScale.getValue()/100.0f);
			generate();
		});

		add(field_angle      = new SelectSlider("angle",Translator.get("LSystemAngle"),360,1,(int)getAngle()));
		field_angle.addSelectListener(evt->{
			setAngle(field_angle.getValue());
			generate();
		});

		add(field_noise      = new SelectSlider("noise",Translator.get("LSystemNoise"),100,0,(int)getNoise()));
		field_noise.addSelectListener(evt->{
			setNoise(field_noise.getValue());
			generate();
		});

		add(new SelectReadOnlyText("url","<a href='https://en.wikipedia.org/wiki/L-system'>"+Translator.get("TurtleGenerators.LearnMore.Link.Text")+"</a>"));
	}

	@Override
	public String getName() {
		return Translator.get("LSystemTreeName");
	}

	@Override
	public void generate() {
		random.setSeed(seed);

        Turtle turtle = new Turtle();
		turtle.penDown();
		// do the curve
		lSystemTree(turtle,order, 10);

		// scale turtle to fit paper
		ResizeTurtleToPaperAction action = new ResizeTurtleToPaperAction(myPaper,false,null);
		turtle = action.run(turtle);

		notifyListeners(turtle);
	}


	// recursive L System tree fractal
	private void lSystemTree(Turtle turtle,int n, double distance) {
		if (n == 0) return;

		turtle.forward(distance);
		if(n>1) {
			double angleStep = angleSpan / (float)(numBranches-1);
			double oldAngle = turtle.getAngle();
			double len = distance*orderScale;
			double noiseUnit = noise/100.0;

			turtle.turn(-(angleSpan/2.0f));
			for(int i=0;i<numBranches;++i) {
				lSystemTree(turtle,n-1,len - len * noiseUnit * random.nextDouble() );
				if(noise>0) {
					turtle.turn(angleStep + angleStep * (random.nextDouble()-0.5)*noiseUnit);
				} else {
					turtle.turn(angleStep);
				}
			}
			turtle.setAngle(oldAngle);
		}
		turtle.forward(-distance);
	}


	public void setOrder(int value) {
		order=value;	
	}
	public int getOrder() {
		return order;
	}

	public void setScale(double value) {
		orderScale = value;
	}
	public double getScale() {
		return orderScale;
	}

	public void setAngle(double value) {
		angleSpan = value;
	}
	public double getAngle() {
		return angleSpan;
	}

	public void setBranches(int value) {
		numBranches = value;
	}
	public int getBranches() {
		return numBranches;
	}

	public void setNoise(int value) {
		noise = value;		
	}

	public int getNoise() {
		return noise;		
	}
}
