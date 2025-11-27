package com.marginallyclever.makelangelo.makeart.turtlegenerator.fractal;

import com.marginallyclever.donatello.select.SelectOneOfMany;
import com.marginallyclever.donatello.select.SelectReadOnlyText;
import com.marginallyclever.donatello.select.SelectSlider;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.TurtleGenerator;
import com.marginallyclever.makelangelo.makeart.turtletool.ResizeTurtleToPaperAction;
import com.marginallyclever.makelangelo.turtle.Turtle;

/**
 * <p>Lists all LSystem implementations.  A drop down lets the user select which one to use.  Also displays the order
 * field.  A change in selection or order triggers regeneration.  Some systems are very complex at higher orders, so
 * the order is capped at 12 and the order is reset to 3 on every curve type change.</p>
 * <p>See also <a href='https://fedimser.github.io/l-systems.html'>L Systems</a> and
 * <a href='https://en.wikipedia.org/wiki/L-system'>Wikipedia</a></p>
 * @author Dan Royer
 */
public class Generator_LSystem extends TurtleGenerator {
    private static int order = 3; // controls complexity of curve
    private final SelectSlider fieldOrder;

    private final String [] choices = {
            "Dragon",
            "Gosper",
            "Hilbert",
            "Ice fractal",
            "Koch",
            "Koch snowflake",
            "Peano",
            "Sierpinski",
            "Sierpinski 2" };
    private static int choice = 0;
    private final SelectOneOfMany fieldChoice;

    public Generator_LSystem() {
        super();

        fieldOrder = new SelectSlider("order", Translator.get("HilbertCurveOrder"),12,0,Generator_LSystem.order);
        fieldChoice = new SelectOneOfMany("curveType", Translator.get("LSystemCurveType"), choices, Generator_LSystem.choice);

        add(fieldChoice);
        fieldChoice.addSelectListener(evt->{
            choice = fieldChoice.getSelectedIndex();
            fieldOrder.setValue(3);  // fires generate().
            generate();
        });

        add(fieldOrder);
        fieldOrder.addSelectListener(evt->{
            order = Math.max(1,fieldOrder.getValue());
            generate();
        });

        add(new SelectReadOnlyText("url","<a href='https://en.wikipedia.org/wiki/L-system'>"+Translator.get("TurtleGenerators.LearnMore.Link.Text")+"</a>"));
    }

    @Override
    public String getName() {
        return Translator.get("LSystemName");
    }

    @Override
    public void generate() {
        Turtle turtle = new Turtle();
        turtle.penDown();

        switch(choices[choice]) {
            case "Dragon":          dragon(turtle);            break;
            case "Gosper":          gosper(turtle);            break;
            case "Hilbert":         hilbert(turtle);           break;
            case "Ice fractal":     ice(turtle);               break;
            case "Koch":            koch(turtle);              break;
            case "Koch snowflake":  kochSnowflake(turtle);     break;
            case "Peano":           peano(turtle);             break;
            case "Sierpinski":      sierpinski(turtle);        break;
            case "Sierpinski 2":    sierpinski2(turtle);       break;
            default:                dragon(turtle);            break;
        }

        // scale turtle to fit paper
        ResizeTurtleToPaperAction action = new ResizeTurtleToPaperAction(myPaper,false,null);
        turtle = action.run(turtle);

        notifyListeners(turtle);
    }

    private void dragon(Turtle turtle) {
        LSystem system = new LSystem();
        system.addRule("F","F+G");
        system.addRule("G","F-G");
        String result = system.generate("F",order);

        for(char command : result.toCharArray()) {
            switch(command) {
                case 'G':
                case 'F': turtle.forward(1); break;
                case '+': turtle.turn(90); break;
                case '-': turtle.turn(-90); break;
                // Ignore other characters
            }
        }
    }

    private void gosper(Turtle turtle) {
        LSystem system = new LSystem();
        system.addRule("A","A-B--B+A++AA+B-");
        system.addRule("B","+A-BB--B-A++A+B");
        String result = system.generate("A",order);
        for(char command : result.toCharArray()) {
            switch(command) {
                case 'A':
                case 'B': turtle.forward(1); break;
                case '+': turtle.turn(60); break;
                case '-': turtle.turn(-60); break;
                // Ignore other characters
            }
        }
    }

    // Hilbert curve
    private void hilbert(Turtle turtle) {
        LSystem system = new LSystem();
        system.addRule("A","+BF-AFA-FB+");
        system.addRule("B","-AF+BFB+FA-");
        String result = system.generate("A", order);

        for(char command : result.toCharArray()) {
            switch(command) {
                case 'F': turtle.forward(1); break;
                case '+': turtle.turn(90); break;
                case '-': turtle.turn(-90); break;
                // Ignore other characters
            }
        }
    }

    private void ice(Turtle turtle) {
        LSystem system = new LSystem();
        system.addRule("F","FF+F++F+F");
        String result = system.generate("F+F+F+F", order);
        for(char command : result.toCharArray()) {
            switch(command) {
                case 'F': turtle.forward(1); break;
                case '+': turtle.turn(90); break;
                case '-': turtle.turn(-90); break;
                // Ignore other characters
            }
        }
    }

    private void koch(Turtle turtle) {
        LSystem system = new LSystem();
        //system.addRule("F","F+F-F-F+F");
        system.addRule("F","F+F--F+F");

        String result = system.generate("F", order);
        for(char command : result.toCharArray()) {
            switch(command) {
                case 'F': turtle.forward(1); break;
                case '+': turtle.turn(60); break;
                case '-': turtle.turn(-60); break;
                // Ignore other characters
            }
        }
    }

    private void kochSnowflake(Turtle turtle) {
        LSystem system = new LSystem();
        system.addRule("F","F+F--F+F");

        String result = system.generate("F--F--F", order);
        for(char command : result.toCharArray()) {
            switch(command) {
                case 'F': turtle.forward(1); break;
                case '+': turtle.turn(60); break;
                case '-': turtle.turn(-60); break;
                // Ignore other characters
            }
        }
    }

    private void peano(Turtle turtle) {
        LSystem lSystem = new LSystem();
        lSystem.addRule("X", "XFYFX+F+YFXFY−F−XFYFX");
        lSystem.addRule("Y", "YFXFY−F−XFYFX+F+YFXFY");
        String result = lSystem.generate("X", order);

        for(char command : result.toCharArray()) {
            switch(command) {
                case 'F': turtle.forward(1); break;
                case '+': turtle.turn(90); break;
                case '−': turtle.turn(-90); break;
                // Ignore other characters
            }
        }
    }

    private void sierpinski(Turtle turtle) {
        LSystem system = new LSystem();
        system.addRule("F","F-G+F+G-F");
        system.addRule("G","GG");
        String result = system.generate("F-G-G",order);

        for(char c : result.toCharArray()) {
            switch (c) {
                case 'F':
                case 'G': turtle.forward(1); break;
                case '+': turtle.turn(120); break;
                case '-': turtle.turn(-120); break;
                // Ignore other characters
            }
        }
    }

    private void sierpinski2(Turtle turtle) {
        LSystem system = new LSystem();
        system.addRule("X","YF+XF+Y");
        system.addRule("Y","XF-YF-X");
        String result = system.generate("YF",order);

        for(char c : result.toCharArray()) {
            switch (c) {
                case 'F': turtle.forward(1); break;
                case '+': turtle.turn(60); break;
                case '-': turtle.turn(-60); break;
                // Ignore other characters
            }
        }
    }
}
