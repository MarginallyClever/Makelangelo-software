package com.marginallyclever.makelangelo;
import java.awt.Dimension;
import java.awt.Font;
import java.text.DecimalFormat;

import javax.swing.JLabel;


// manages the status bar at the bottom of the application window
public class StatusBar extends JLabel {
	static final long serialVersionUID=1;
	
	long t_start;
	protected DecimalFormat fmt = new DecimalFormat("#0.00");
	protected String sSoFar = "so far: ";
	protected String sRemaining=" remaining: ";
	protected String sElapsed="";
	protected MultilingualSupport translator;
	
	
	public String formatTime(long millis) {
    	long s=millis/1000;
    	long m=s/60;
    	long h=m/60;
    	m%=60;
    	s%=60;

    	String elapsed="";
    	if(h>0) elapsed+=h+"h";
    	if(h>0||m>0) elapsed+=m+"m";
    	elapsed+=s+"s ";
    	
    	return elapsed;
	}
	
	
    public StatusBar(MultilingualSupport ms) {
        super();
        super.setPreferredSize(new Dimension(100, 16));

        translator = ms;
        
        Font f = getFont();
        setFont(f.deriveFont(Font.BOLD,15));
        Dimension d=getMinimumSize();
        d.setSize(d.getWidth(), d.getHeight()+30);
        setMinimumSize(d);
        
        clear();
    }
    
    public void setMessage(String message) {
        setText(" "+message);        
    }
    
    public String getElapsed() {
    	return sElapsed;
    }
    
    public void clear() {
        setMessage("Ready");
    }
    
    public void start() {
    	t_start=System.currentTimeMillis();
    }
    
    public void setProgress(long sofar,long total) {
    	float progress=0;
    	sElapsed="";
    	if(total>0) {
    		progress = 100.0f*(float)sofar/(float)total;
    		
    		long t_draw_now= (sofar>0) ? System.currentTimeMillis()-t_start : 0;
    		long total_time = (long)( (float)t_draw_now * (float)total / (float)sofar );
        	long remaining = total_time - t_draw_now;
        	sElapsed = translator.get("StatusSoFar") + formatTime(t_draw_now) + 
        			translator.get("StatusRemaining") + formatTime(remaining);
    	}

	   	setMessage(fmt.format(progress)+"% ("+sofar+"/"+total+") "+sElapsed);
    }
}

/**
 * This file is part of DrawbotGUI.
 *
 * DrawbotGUI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * DrawbotGUI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with DrawbotGUI.  If not, see <http://www.gnu.org/licenses/>.
 */