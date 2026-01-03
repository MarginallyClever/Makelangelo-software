package com.marginallyclever.makelangelo.plotter.plotterrenderer;

import com.marginallyclever.convenience.helpers.DrawingHelper;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.texture.TextureFactory;
import com.marginallyclever.makelangelo.texture.TextureWithMetadata;

import javax.vecmath.Point2d;
import java.awt.*;

public class MakelangeloCustom implements PlotterRenderer {
	public final static float PEN_HOLDER_RADIUS_5 = 25; // mm
	public final static float COUNTERWEIGHT_W = 30;
	public final static float COUNTERWEIGHT_H = 60;
	public final static float PULLEY_RADIUS = 1.27f;
	public final static float MOTOR_WIDTH = 42;
	private static TextureWithMetadata controlBoard;

    public MakelangeloCustom() {
        controlBoard = TextureFactory.loadTexture("/textures/rampsv14.png");
    }

	@Override
	public void render(Graphics graphics, Plotter robot) {
		PlotterSettings settings = robot.getSettings();

		paintControlBox(graphics, settings);
		paintMotors(graphics, settings);
		if(robot.getDidFindHome())
			paintPenHolderToCounterweights(graphics,robot);
	}

	/**
	 * paint the controller and the LCD panel
	 * @param graphics the render context
	 * @param settings plottersettings of the robot
	 */
	private void paintControlBox(Graphics graphics, PlotterSettings settings) {
		float left = (float)settings.getDouble(PlotterSettings.LIMIT_LEFT);
		float right = (float)settings.getDouble(PlotterSettings.LIMIT_RIGHT);
		float top = (float)settings.getDouble(PlotterSettings.LIMIT_TOP);

		// mounting plate for PCB
        DrawingHelper.drawRectangle(graphics, top+35f, right+30f, top-35f, left-30f,new Color(1,0.8f,0.5f));

		// wires to each motor
        Graphics2D g2d = (Graphics2D) graphics;
        final float SPACING=2;
        float y=SPACING*-1.5f;
        float cy = top;
        float cx = 0;
        g2d.setColor(Color.RED   );  g2d.drawLine((int)(cx+left), (int)(cy+y), (int)(cx+right), (int)(cy+y));  y+=SPACING;
        g2d.setColor(Color.GREEN );  g2d.drawLine((int)(cx+left), (int)(cy+y), (int)(cx+right), (int)(cy+y));  y+=SPACING;
        g2d.setColor(Color.BLUE  );  g2d.drawLine((int)(cx+left), (int)(cy+y), (int)(cx+right), (int)(cy+y));  y+=SPACING;
        g2d.setColor(Color.YELLOW);  g2d.drawLine((int)(cx+left), (int)(cy+y), (int)(cx+right), (int)(cy+y));  y+=SPACING;
		
		float shiftX = right / 2;
        final double scale = 0.1;
        if (shiftX < 100) {
            shiftX = 45;
        }

		DrawingHelper.paintTexture(graphics, controlBoard, cx+shiftX, top-72, 1024 * scale, 1024 * scale);
        renderLCD(graphics, settings);
	}

	// draw left & right motor
	private void paintMotors(Graphics graphics, PlotterSettings settings) {
		double top = settings.getDouble(PlotterSettings.LIMIT_TOP);
		double right = settings.getDouble(PlotterSettings.LIMIT_RIGHT);
		double left = settings.getDouble(PlotterSettings.LIMIT_LEFT);
		var c = new Color(0.3f,0.3f,0.3f);

        int w2 = (int)MOTOR_WIDTH/2;

		// left motor
        DrawingHelper.drawRectangle(graphics, top+w2, left+w2, top-w2, left-w2, c);
		// right motor
        DrawingHelper.drawRectangle(graphics, top+w2, right+w2, top-w2, right-w2, c);
	}
	
	private void renderLCD(Graphics graphics, PlotterSettings settings) {
		float left = (float)settings.getDouble(PlotterSettings.LIMIT_LEFT);
		float right = (float)settings.getDouble(PlotterSettings.LIMIT_RIGHT);
		float top = (float)settings.getDouble(PlotterSettings.LIMIT_TOP);

		// position
		float shiftX = left / 2;
		if (shiftX > -100) {
			shiftX = -75;
		}

		// LCD red
		float w = 150f/2;
		float h = 56f/2;
        DrawingHelper.drawRectangle(graphics, top+h, shiftX+w, top-h, shiftX-w, new Color(1,0.8f,0.5f));

		// LCD green
		shiftX += -2.6f/2;
		float shiftY = -0.771f;
		w = 98f/2;
		h = 60f/2;
        DrawingHelper.drawRectangle(graphics, top+shiftY+h, shiftX+w, top+shiftY-h, shiftX-w, new Color(0,0.6f,0.0f));

		// LCD black
		h = 40f/2;
        DrawingHelper.drawRectangle(graphics, top+shiftY+h, shiftX+w, top+shiftY-h, shiftX-w, new Color(0,0,0));

		// LCD blue
		h = 25f/2;
		w = 75f/2;
        DrawingHelper.drawRectangle(graphics, top+shiftY+h, shiftX+w, top+shiftY-h, shiftX-w, new Color(0,0,0.7f));
	}

	private void paintPenHolderToCounterweights(Graphics graphics, Plotter robot ) {
		PlotterSettings settings = robot.getSettings();
		Point2d pos = robot.getPos();
		float gx = (float)pos.x;
		float gy = (float)pos.y;
		
		float top = (float)settings.getDouble(PlotterSettings.LIMIT_TOP);
		float bottom = (float)settings.getDouble(PlotterSettings.LIMIT_BOTTOM);
		float left = (float)settings.getDouble(PlotterSettings.LIMIT_LEFT);
		float right = (float)settings.getDouble(PlotterSettings.LIMIT_RIGHT);
		
		if(gx<left  ) return;
		if(gx>right ) return;
		if(gy>top   ) return;
		if(gy<bottom) return;
		
		float bottleCenter = 8f+7.5f;

		float mw = right-left;
		float mh = top-(float)settings.getDouble(PlotterSettings.LIMIT_BOTTOM);
		float suggestedLength = (float)Math.sqrt(mw*mw+mh*mh)+50;

		float dx = gx - left;
		float dy = gy - top;
		float left_a = (float)Math.sqrt(dx*dx+dy*dy);
		float left_b = (suggestedLength - left_a)/2;

		dx = gx - right;
		float right_a = (float)Math.sqrt(dx*dx+dy*dy);
		float right_b = (suggestedLength - right_a)/2;

		paintPlotter(graphics,gx,gy);

		// belts
        Graphics2D g2d = (Graphics2D) graphics;
        g2d.setColor(new Color(255*0.2f,255*0.2f,255*0.2f));

		// belt from motor to pen holder left
        g2d.drawLine((int)left, (int)top, (int)gx, (int)gy);
		// belt from motor to pen holder right
        g2d.drawLine((int)right, (int)top, (int)gx, (int)gy);
		// belt from motor to counterweight left
        g2d.drawLine(
                (int)(left - bottleCenter - PULLEY_RADIUS), (int)(top-MOTOR_WIDTH/2),
                (int)(left - bottleCenter - PULLEY_RADIUS), (int)(top - left_b));
		// belt from motor to counterweight right
        g2d.drawLine(
                (int)(right + bottleCenter + PULLEY_RADIUS), (int)(top-MOTOR_WIDTH/2),
                (int)(right + bottleCenter + PULLEY_RADIUS), (int)(top - right_b));
		
		// counterweight left
        g2d.setColor(Color.BLUE);
        g2d.drawRect(
                (int)(left - bottleCenter - PULLEY_RADIUS - COUNTERWEIGHT_W/2),
                (int)(top - left_b-COUNTERWEIGHT_H),
                (int)(COUNTERWEIGHT_W),
                (int)(COUNTERWEIGHT_H));

		// counterweight right
        g2d.setColor(Color.BLUE);
        g2d.drawRect(
                (int)(right+PULLEY_RADIUS+bottleCenter-COUNTERWEIGHT_W/2),
                (int)(top-right_b-COUNTERWEIGHT_H),
                (int)(COUNTERWEIGHT_W),
                (int)(COUNTERWEIGHT_H)
        );
	}

	private void paintPlotter(Graphics graphics, float gx, float gy) {
		// plotter
        DrawingHelper.drawCircle(graphics, gx, gy, PEN_HOLDER_RADIUS_5, new Color(0f,0f,1f));
	}
}
