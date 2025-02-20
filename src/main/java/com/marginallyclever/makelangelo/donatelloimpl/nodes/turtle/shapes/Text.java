package com.marginallyclever.makelangelo.donatelloimpl.nodes.turtle.shapes;

import com.marginallyclever.donatello.ports.InputInt;
import com.marginallyclever.donatello.ports.InputOneOfMany;
import com.marginallyclever.donatello.ports.InputString;
import com.marginallyclever.makelangelo.donatelloimpl.ports.OutputTurtle;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.Generator_Text;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.Node;

import java.awt.*;
import java.util.Locale;

/**
 * A node that generates a {@link com.marginallyclever.makelangelo.turtle.Turtle} that draws a text message.
 */
public class Text extends Node {
    private final InputString text = new InputString("Message","");
    private final InputOneOfMany font = new InputOneOfMany("Font");
    private final InputInt fontSize = new InputInt("Font Size",15);
    private final OutputTurtle turtle = new OutputTurtle("Turtle");
    private final String [] fontNames = getListOfFonts();

    public Text() {
        super("Text");
        addPort(text);
        addPort(font);
        addPort(fontSize);
        addPort(turtle);

        font.setOptions(fontNames);
    }

    private String [] getListOfFonts() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Font[] fontList = ge.getAllFonts();
        var fontNames = new String[fontList.length];
        Locale locale = Locale.getDefault();
        int i=0;
        for(Font f : fontList) {
            fontNames[i++] = f.getFontName(locale);
        }
        return fontNames;
    }

    @Override
    public void update() {
        var fontChoice = font.getValue();
        var message = text.getValue();
        if(message.trim().isEmpty()) {
            turtle.setValue(new Turtle());
            return;
        }
        var size = fontSize.getValue();
        var result = Generator_Text.writeBeautifulMessage(fontNames[fontChoice],size,text.getValue());
        result.scale(1,-1);
        turtle.setValue(result);
    }
}
