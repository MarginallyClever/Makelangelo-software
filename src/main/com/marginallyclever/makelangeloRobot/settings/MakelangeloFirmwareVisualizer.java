package com.marginallyclever.makelangeloRobot.settings;

import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.OpenGLHelper;
import com.marginallyclever.convenience.turtle.Turtle;
import com.marginallyclever.makelangeloRobot.MakelangeloFirmwareSimulation;
import com.marginallyclever.makelangeloRobot.MakelangeloFirmwareSimulationBlock;

public class MakelangeloFirmwareVisualizer {
	public static int limit;
	
	public MakelangeloFirmwareVisualizer() {}
	
	public void render(GL2 gl2,Turtle turtleToRender,MakelangeloRobotSettings settings) {
		MakelangeloFirmwareSimulationBlock.counter=0;

		gl2.glPushMatrix();
		gl2.glLineWidth(1);
		gl2.glBegin(GL2.GL_LINE_STRIP);
		MakelangeloFirmwareSimulation m = new MakelangeloFirmwareSimulation(settings);
		m.historyAction(turtleToRender, (block)->{
			//renderAccelDecel(gl2,block,settings);
			renderMinLength(gl2,block);
		});
		gl2.glEnd();
		gl2.glPopMatrix();
	}

	private void renderMinLength(GL2 gl2,MakelangeloFirmwareSimulationBlock block) {
		double d = block.distance / MakelangeloFirmwareSimulation.MIN_SEGMENT_LENGTH_MM;
		d = Math.max(Math.min(d, 1), 0);
		double g = d;
		double r = 1-d;
		gl2.glColor3d(r, g, 0);
		//gl2.glVertex2d(block.start.x,block.start.y);
		gl2.glVertex2d(block.end.x,block.end.y);
	}
	
	private void renderAccelDecel(GL2 gl2,MakelangeloFirmwareSimulationBlock block,MakelangeloRobotSettings settings) {
		double t,a,d;
		boolean useDistance=true;
		if(useDistance) {
			t = block.distance;
			a = block.accelerateUntilD;
			d = block.decelerateAfterD;
		} else {
			// use time
			t = block.end_s;
			a = block.accelerateUntilT;
			d = block.decelerateAfterT;
		}
		//if(d>t) d=t;

		//if(--limit<=0) return;
		//if(limit<20) block.report();
		
		double [] rgba = OpenGLHelper.getCurrentColor(gl2);
		
		// nominal vs entry speed
		
		boolean showNominal=false;
		if(showNominal) {
			Vector3d o = new Vector3d(-block.normal.y,block.normal.x,0);
			double f = block.nominalSpeed / settings.getPenDownFeedRate();
			o.scale(f*1);
			o.add(block.start);
			gl2.glColor3d(0,0,0);
			gl2.glVertex2d(block.start.x,block.start.y);
			gl2.glVertex2d(o.x,o.y);
			gl2.glVertex2d(block.start.x,block.start.y);
		}
		boolean showEntry=false;
		if(showEntry) {
			Vector3d o = new Vector3d(-block.normal.y,block.normal.x,0);
			double f = block.entrySpeed / settings.getPenDownFeedRate();
			o.scale(f*1);
			o.add(block.start);
			gl2.glColor3d(1,0,0);
			gl2.glVertex2d(block.start.x,block.start.y);
			gl2.glVertex2d(o.x,o.y);
			gl2.glVertex2d(block.start.x,block.start.y);
		}

		gl2.glColor3dv(rgba, 0);
		
		// nominal section of move is brighter the closer it gets to max feedrate.
		double c0 = 1;//Math.min(1,block.entrySpeed / settings.getPenDownFeedRate()); 
		double c1 = 1;//Math.min(1,block.nominalSpeed / settings.getPenDownFeedRate()); 
		double c2 = 1;//Math.min(1,block.exitSpeed / settings.getPenDownFeedRate()); 
		Vector3d pLast = block.start;
		if(a>0) {
			// accel part of block
			Vector3d p0 = new Vector3d(block.delta);
			p0.scale(a/t);
			p0.add(block.start);
			gl2.glColor3d(0,c0,0);
			gl2.glVertex2d(block.start.x,block.start.y);
			gl2.glVertex2d(p0.x,p0.y);
			pLast=p0;
		}
		if(a<d) {
			// nominal part of block
			Vector3d p1 = new Vector3d(block.delta);
			p1.scale(d/t);
			p1.add(block.start);
			gl2.glColor3d(0,0,c1);
			gl2.glVertex2d(pLast.x,pLast.y);
			gl2.glVertex2d(p1.x,p1.y);
			pLast=p1;
		}
		// decel part of block
		gl2.glColor3d(c2,0,0);
		gl2.glVertex2d(pLast.x,pLast.y);
		gl2.glVertex2d(block.end.x,block.end.y);
	}
}
