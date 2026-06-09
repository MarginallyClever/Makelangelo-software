package com.marginallyclever.makelangelo.editorcontext;

import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.turtle.Turtle;

import javax.swing.event.EventListenerList;
import java.util.function.Consumer;

/**
 *
 */
public class EditorContext {
    private Paper paper = new Paper();
    private Plotter plotter = new Plotter();
    private Turtle turtle = new Turtle();
    private final EventListenerList listeners = new EventListenerList();

    public void mutate(Consumer<Turtle> edit) {
        edit.accept(turtle);
        fireDocumentChanged();
    }

    private void fireDocumentChanged() {
        for( var l : listeners.getListeners(EditorContextListener.class)) {
            l.turtleChanged(turtle);
        }
    }

    public void addChangeListener(EditorContextListener listener) {
        listeners.add(EditorContextListener.class, listener);
    }

    public Turtle getTurtle() {
        return turtle;
    }

    public void setTurtle(Turtle next) {
        this.turtle = next;
        fireDocumentChanged();
    }

    public Paper getPaper() {
        return paper;
    }

    public void setPaper(Paper next) {
        this.paper = next;
    }

    public Plotter getPlotter() {
        return plotter;
    }

    public void setPlotter(Plotter next) {
        plotter = next;
    }
}
