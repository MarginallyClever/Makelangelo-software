package com.marginallyclever.artPipeline.generators;

import java.util.Observable;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectReadOnlyText;
import com.marginallyclever.makelangelo.select.SelectSlider;

public class Generator_FibonacciSpiral_Panel extends ImageGeneratorPanel {
	private SelectSlider fieldOrder;
	private Generator_FibonacciSpiral generator;
	
	Generator_FibonacciSpiral_Panel(Generator_FibonacciSpiral generator_FibonacciSpiral) {
		super();
		this.generator = generator_FibonacciSpiral;
		
		add(fieldOrder = new SelectSlider(Translator.get("HilbertCurveOrder"),16,0,Generator_Dragon.getOrder()));
		add(new SelectReadOnlyText("<a href='https://en.wikipedia.org/wiki/Fibonacci_number'>Learn more</a>"));
		finish();
	}

	@Override
	public void update(Observable o, Object arg) {
		super.update(o, arg);
		
		int newOrder = ((Number)fieldOrder.getValue()).intValue();
		if(newOrder<3) newOrder=1;
		
		if(newOrder != Generator_FibonacciSpiral.getOrder()) {
			Generator_FibonacciSpiral.setOrder(newOrder);
			makelangeloRobotPanel.regenerate(generator);
		}
	}
}
