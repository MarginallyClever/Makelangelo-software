import java.awt.Dimension;
import java.text.DecimalFormat;

import javax.swing.JLabel;


// manages the status bar at the bottom of the application window
public class StatusBar extends JLabel {
	static final long serialVersionUID=1;
	
	long t_start;

	DecimalFormat fmt = new DecimalFormat("#.##");
	
	
	public String formatTime(long millis) {
    	String elapsed="";
    	long s=millis/1000;
    	long m=s/60;
    	long h=m/60;
    	m%=60;
    	s%=60;
    	if(h>0) elapsed+=h+"h";
    	if(h>0||m>0) elapsed+=m+"m";
    	elapsed+=s+"s ";
    	return elapsed;
	}
	
	
    public StatusBar() {
        super();
        super.setPreferredSize(new Dimension(100, 16));
        Clear();
    }
    
    public void SetMessage(String message) {
        setText(" "+message);        
    }
    
    public void Clear() {
        SetMessage("Ready");
    }
    
    public void Start() {
    	t_start=System.currentTimeMillis();
    }
    
    public void SetProgress(long sofar,long total) {
    	float progress=0;
    	String elapsed="";
    	if(total>0) {
    		progress = 100.0f*(float)sofar/(float)total;
    		
    		long t_draw_now= (sofar>0) ? System.currentTimeMillis()-t_start : 0;
    		elapsed=formatTime(t_draw_now);
    	}
	   	SetMessage(fmt.format(progress)+"% ("+sofar+"/"+total+") "+elapsed);
    }
}