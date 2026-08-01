package com.marginallyclever.makelangelo.makeart.turtletool;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.editorcontext.EditorContext;
import com.marginallyclever.makelangelo.turtle.Line2d;
import com.marginallyclever.makelangelo.turtle.StrokeLayer;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.util.PreferencesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.vecmath.Point2d;
import java.awt.*;

/**
 * A panel that allows the user to remove "hops" (travel moves) that are shorter than a given threshold.
 * It merges adjacent lines if the distance between the end of one and the start of the next is small.
 */
public class RemoveHopsPanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(RemoveHopsPanel.class);
    private final EditorContext context;
    private final Turtle turtleOriginal;
    private final JSpinner thresholdSpinner;

    public RemoveHopsPanel(EditorContext context) {
        super(new BorderLayout());
        this.context = context;
        this.turtleOriginal = new Turtle(context.getTurtle());

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.add(new JLabel(Translator.get("RemoveHopsPanel.Threshold")));
        
        SpinnerNumberModel model = new SpinnerNumberModel(1.0, 0.0, 1000.0, 0.1);
        thresholdSpinner = new JSpinner(model);
        thresholdSpinner.addChangeListener(e -> removeHopsNow());
        controls.add(thresholdSpinner);

        add(controls, BorderLayout.CENTER);
        removeHopsNow();
    }

    public void removeHopsNow() {
        double threshold = (Double) thresholdSpinner.getValue();
        double thresholdSq = threshold * threshold;

        Turtle newTurtle = new Turtle();
        for (StrokeLayer layer : turtleOriginal.getLayers()) {
            if(layer.isEmpty()) continue;

            newTurtle.setStroke(layer.getColor(), layer.getDiameter());
            var allLines = layer.getAllLines();
            var iter = allLines.iterator();
            Line2d currentLine = iter.next();
            while(iter.hasNext()) {
                Line2d line = iter.next();
                if(line.isEmpty()) continue;

                var allPoints = line.getAllPoints();
                var iter2 = allPoints.iterator();
                Point2d lastPoint = currentLine.getAllPoints().getLast();
                Point2d firstPoint = iter2.next();

                if (lastPoint.distanceSquared(firstPoint) < thresholdSq) {
                    // Merge: add all points from 'line' to 'currentLine' except maybe the first if it's identical?
                    // Actually, just add all points for simplicity.
                    while(iter2.hasNext()) {
                        currentLine.add(new Point2d(iter2.next()));
                    }
                } else {
                    // Finish current line and start a new one
                    addLineToTurtle(newTurtle, currentLine);
                    currentLine = line;
                }
            }
            if (currentLine != null) {
                addLineToTurtle(newTurtle, currentLine);
            }
        }
        context.setTurtle(newTurtle);
    }

    private void addLineToTurtle(Turtle turtle, Line2d line) {
        boolean first = true;
        for (Point2d p : line.getAllPoints()) {
            if (first) {
                turtle.jumpTo(p.x, p.y);
                first = false;
            } else {
                turtle.moveTo(p.x, p.y);
            }
        }
    }

    private void revertOriginalTurtle() {
        context.setTurtle(turtleOriginal);
    }

    public static void runAsDialog(Window parent, EditorContext context) {
        RemoveHopsPanel panel = new RemoveHopsPanel(context);

        JDialog dialog = new JDialog(parent, Translator.get("RemoveHopsPanel.Title"));
        dialog.setModal(true);
        JButton okButton = new JButton(Translator.get("OK"));
        JButton cancelButton = new JButton(Translator.get("Cancel"));

        JPanel outerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 3;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        outerPanel.add(panel, c);

        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 1;
        c.weightx = 1;
        outerPanel.add(okButton, c);
        c.gridx = 2;
        c.gridwidth = 1;
        c.weightx = 1;
        outerPanel.add(cancelButton, c);

        okButton.addActionListener((e) -> dialog.dispose());
        cancelButton.addActionListener((e) -> {
            panel.revertOriginalTurtle();
            dialog.dispose();
        });

        dialog.add(outerPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    public static void main(String[] args) {
        PreferencesHelper.start();
        Translator.start();

        JFrame frame = new JFrame(RemoveHopsPanel.class.getSimpleName());
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        runAsDialog(frame, new EditorContext());
    }
}
