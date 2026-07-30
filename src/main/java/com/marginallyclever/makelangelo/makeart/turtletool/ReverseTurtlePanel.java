package com.marginallyclever.makelangelo.makeart.turtletool;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.editorcontext.EditorContext;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.util.PreferencesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

public class ReverseTurtlePanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(RotateTurtlePanel.class);
    private final EditorContext context;
    private final Turtle turtleOriginal;

    public ReverseTurtlePanel(EditorContext context) {
        super();
        this.context = context;
        turtleOriginal = new Turtle(context.getTurtle());  // make a deep copy of the original.

        // add a jcombobox.
        // option 1 is "reverse islands"
        // option 2 is "reverse everything".
        var combo = new JComboBox<>(new String[]{"only islands", "everything"});
        combo.setSelectedIndex(0);
        combo.addActionListener(e -> reverseNow(combo.getSelectedIndex()));
        add(combo);
        reverseNow(combo.getSelectedIndex());
    }

    /**
     *
     * @param option 1 for sequence of islands
     *               2 for everything.
     */
    public void reverseNow(int option) {
        Turtle newTurtle = new Turtle();
        var layers = turtleOriginal.getLayers();
        layers.reversed().forEach(layer -> {
            newTurtle.setStroke(layer.getColor(), layer.getDiameter());
            // reverse the sequence of islands, but not the points in each island.
            layer.getAllLines().reversed().forEach(line -> {
                final boolean[] first = {true};
                var list = line.getAllPoints();
                if(option==1) list = list.reversed();
                list.forEach(point -> {
                    if (first[0]) {
                        newTurtle.jumpTo(point.x, point.y);
                        first[0] = false;
                    } else {
                        newTurtle.moveTo(point.x, point.y);
                    }
                });
            });
        });
        context.setTurtle(newTurtle);
    }

    private void revertOriginalTurtle() {
        context.setTurtle(turtleOriginal);
    }

    public static void runAsDialog(Window parent, EditorContext context) {
        ReverseTurtlePanel panel = new ReverseTurtlePanel(context);

        JDialog dialog = new JDialog(parent,Translator.get("Reverse"));
        JButton okButton = new JButton(Translator.get("OK"));
        JButton cancelButton = new JButton(Translator.get("Cancel"));

        JPanel outerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx=0;
        c.gridy=0;
        c.gridwidth=3;
        c.anchor=GridBagConstraints.NORTHWEST;
        c.fill=GridBagConstraints.BOTH;
        outerPanel.add(panel,c);

        c.gridx=1;
        c.gridy=1;
        c.gridwidth=1;
        c.weightx=1;
        outerPanel.add(okButton,c);
        c.gridx=2;
        c.gridwidth=1;
        c.weightx=1;
        outerPanel.add(cancelButton,c);

        okButton.addActionListener((e)-> dialog.dispose());
        cancelButton.addActionListener((e)-> {
            panel.revertOriginalTurtle();
            dialog.dispose();
        });

        dialog.add(outerPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    // TEST

    public static void main(String[] args) {
        PreferencesHelper.start();
        Translator.start();

        JFrame frame = new JFrame(RotateTurtlePanel.class.getSimpleName());
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        runAsDialog(frame,new EditorContext());
    }
}
