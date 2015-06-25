package com.marginallyclever.filters;

import com.marginallyclever.makelangelo.MachineConfiguration;
import com.marginallyclever.makelangelo.MainGUI;
import com.marginallyclever.makelangelo.MultilingualSupport;

import javax.swing.*;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class Filter_GeneratorHilbertCurve extends Filter {
	float turtle_x,turtle_y;
	float turtle_dx,turtle_dy;
	float turtle_step=10.0f;
	float xmax = 7;
	float xmin = -7;
	float ymax = 7;
	float ymin = -7;
	float tool_offset_z = 1.25f;
	float z_down=40;
	float z_up=90;
	int order=4; // controls complexity of curve
	float x,y;

	
	
	public Filter_GeneratorHilbertCurve(MainGUI gui, MachineConfiguration mc,
			MultilingualSupport ms) {
		super(gui, mc, ms);
	}

	@Override
	public String getName() { return translator.get("HilbertCurveName"); }
	
	/**
	 * Overrides teh basic MoveTo() because optimizing for spirals is different logic than straight lines.
	 */
	@Override
	protected void moveTo(Writer out,float x,float y,boolean up) throws IOException {
		tool.writeMoveTo(out, TX(x), TY(y));
		if(lastup!=up) {
			if(up) liftPen(out);
			else   lowerPen(out);
			lastup=up;
		}
	}
	
	
	public void generate(final String dest) {
		final JTextField field_order = new JTextField(Integer.toString(order));

	
		JPanel panel = new JPanel(new GridLayout(0,1));
		panel.add(new JLabel(translator.get("HilbertCurveOrder")));
		panel.add(field_order);
		
	    int result = JOptionPane.showConfirmDialog(null, panel, getName(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
	    if (result == JOptionPane.OK_OPTION) {
			xmax = (float)( machine.getPaperWidth() * machine.paperMargin );
			ymax = xmax;
			xmin = 0;
			ymin = 0;
			order = Integer.parseInt(field_order.getText());
			createCurveNow(dest);
	    }
	}
	

	private void createCurveNow(String dest) {
        try(
        final OutputStream fileOutputStream = new FileOutputStream(dest);
        final Writer output = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);
        ) {
            tool = machine.getCurrentTool();
			setupTransform((int)Math.ceil(xmax-xmin),(int)Math.ceil(ymax-ymin));
			output.write(machine.getConfigLine()+";\n");
			output.write(machine.getBobbinLine()+";\n");
			tool.writeChangeTo(output);
						
			turtle_x=0;
			turtle_y=0;
			turtle_dx=0;
			turtle_dy=-1;
			turtle_step = (float)((xmax-xmin) / (Math.pow(2, order)));

			// Draw bounding box
			//SetAbsoluteMode(output);
			liftPen(output);
			moveTo(output,xmax,ymax,false);
			moveTo(output,xmax,ymin,false);
			moveTo(output,xmin,ymin,false);
			moveTo(output,xmin,ymax,false);
			moveTo(output,xmax,ymax,false);
			liftPen(output);

			// move to starting position
			x = (xmax-turtle_step/2);
			y = (ymax-turtle_step/2);
			moveTo(output,x,y,true);
			lowerPen(output);
			// do the curve
			hilbert(output,order);
			liftPen(output);
			
        	output.flush();
	        output.close();
		}
		catch(IOException ex) {}
	}
	
	
    // Hilbert curve
    private void hilbert(Writer output, int n) throws IOException {
        if (n == 0) return;
        turtle_turn(90);
        treblih(output,n-1);
        turtle_goForward(output);
        turtle_turn(-90);
        hilbert(output,n-1);
        turtle_goForward(output);
        hilbert(output,n-1);
        turtle_turn(-90);
        turtle_goForward(output);
        treblih(output,n-1);
        turtle_turn(90);
    }


    // evruc trebliH
    public void treblih(Writer output,int n) throws IOException {
        if (n == 0) return;
        turtle_turn(-90);
        hilbert(output,n-1);
        turtle_goForward(output);
        turtle_turn(90);
        treblih(output,n-1);
        turtle_goForward(output);
        treblih(output,n-1);
        turtle_turn(90);
        turtle_goForward(output);
        hilbert(output,n-1);
        turtle_turn(-90);
    }
    

    public void turtle_turn(float degrees) {
    	double n = degrees * Math.PI / 180.0;
    	double newx =  Math.cos(n) * turtle_dx + Math.sin(n) * turtle_dy;
    	double newy = -Math.sin(n) * turtle_dx + Math.cos(n) * turtle_dy;
    	double len = Math.sqrt(newx*newx + newy*newy);
    	assert(len>0);
    	turtle_dx = (float)(newx/len);
    	turtle_dy = (float)(newy/len);
    }

    
    public void turtle_goForward(Writer output) throws IOException {
    	//turtle_x += turtle_dx * distance;
    	//turtle_y += turtle_dy * distance;
    	//output.write(new String("G0 X"+(turtle_x)+" Y"+(turtle_y)+"\n").getBytes());
    	x+=(turtle_dx*turtle_step);
    	y+=(turtle_dy*turtle_step);
    	moveTo(output,x,y,false);
    }
}
