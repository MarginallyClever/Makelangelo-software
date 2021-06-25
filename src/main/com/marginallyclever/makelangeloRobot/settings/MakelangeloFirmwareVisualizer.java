package com.marginallyclever.makelangeloRobot.settings;

import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.turtle.Turtle;
import com.marginallyclever.makelangeloRobot.MakelangeloFirmwareSimulation;
import com.marginallyclever.makelangeloRobot.MakelangeloFirmwareSimulationBlock;

public class MakelangeloFirmwareVisualizer {
	public static int limit;
	
	public MakelangeloFirmwareVisualizer() {}
	
	void render(GL2 gl2,Turtle turtleToRender,MakelangeloRobotSettings settings) {

		MakelangeloFirmwareSimulationBlock.counter=0;
		
		gl2.glPushMatrix();
		gl2.glLineWidth(1);
		gl2.glBegin(GL2.GL_LINE_STRIP);
		MakelangeloFirmwareSimulation m = new MakelangeloFirmwareSimulation(settings);
		m.historyAction(turtleToRender, (block)-> {
			double t = block.end_s;
			double a = block.accelerateUntilT;
			double d = block.decelerateAfterT;
			if(d>t) d=t;

			//if(--limit<=0) return;
			//if(limit<20) block.report();
			
			// nominal vs entry speed
			boolean showNominalVsEntry=false;
			if(showNominalVsEntry) {
				Vector3d o = new Vector3d(-block.normal.y,block.normal.x,0);
				double f = block.nominalSpeed - block.entrySpeed;
				o.scale(f*2);
				o.add(block.start);
				gl2.glVertex2d(block.start.x,block.start.y);
				gl2.glVertex2d(o.x,o.y);
				gl2.glVertex2d(block.start.x,block.start.y);
			}
			
			// nominal section of move is brighter the closer it gets to max feedrate.
			double b = 0.9 * Math.min(1,block.nominalSpeed / settings.getPenDownFeedRate()); 
			if(a>0) {
				// accel part of block
				Vector3d p0 = new Vector3d(block.delta);
				p0.scale(a/t);
				p0.add(block.start);
				gl2.glColor3d(0,1,0);
				gl2.glVertex2d(block.start.x,block.start.y);
				gl2.glColor3d(0,b,0);
				gl2.glVertex2d(p0.x,p0.y);
			}
			if(a<d) {
				// nominal part of block
				Vector3d p1 = new Vector3d(block.delta);
				p1.scale(d/t);
				p1.add(block.start);
				gl2.glColor3d(0,0,b);
				gl2.glVertex2d(p1.x,p1.y);
			}
			if(d<t) {
				// decel part of block
				gl2.glColor3d(1,0,0);
				gl2.glVertex2d(block.end.x,block.end.y);
			}
		});
		gl2.glEnd();
		gl2.glPopMatrix();
	}
}
