package com.marginallyclever.makelangelo.makeart.turtlegenerator;

import com.marginallyclever.makelangelo.turtle.Turtle;

import java.util.EventListener;

public interface TurtleGeneratorListener extends EventListener {
	void turtleReady(Turtle t);
}
