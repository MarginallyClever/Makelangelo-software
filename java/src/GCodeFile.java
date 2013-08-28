import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * contains the text for a gcode file.
 * also provides methods for estimating the total length of lines drawn
 * also provides methods for "walking" a file and remembering certain states
 * also provides bounding information?
 * also provides file scaling?
 * @author danroyer
 *
 */
public class GCodeFile {
    public long linesTotal=0;
	public long linesProcessed=0;
	public boolean fileOpened=false;
	public ArrayList<String> lines;
	public float estimated_time=0;
	public float estimated_length=0;
	public int estimate_count=0;
	public float scale=1.0f;
	public float feed_rate=1.0f;

	// returns angle of dy/dx as a value from 0...2PI
	private double atan3(double dy,double dx) {
	  double a=Math.atan2(dy,dx);
	  if(a<0) a=(Math.PI*2.0)+a;
	  return a;
	}
	
	void EstimateDrawTime() {
		int i,j;
		
		double px=0,py=0,pz=0;
		feed_rate=1.0f;
		scale=0.1f;
		estimated_time=0;
		estimated_length=0;
		estimate_count=0;
		
		for(i=0;i<lines.size();++i) {
			String line=lines.get(i);
			String[] pieces=line.split(";");  // comments come after a semicolon.
			if(pieces.length==0) continue;
			
			String[] tokens = pieces[0].split("\\s");
			if(tokens.length==0) continue;

			for(j=0;j<tokens.length;++j) {
				if(tokens[j].equals("G20")) scale=2.54f;  // in->cm
				if(tokens[j].equals("G21")) scale=0.10f;  // mm->cm
				if(tokens[j].startsWith("F")) {
					feed_rate=Float.valueOf(tokens[j].substring(1)) * scale;
				}
			}
			
			double x=px;
			double y=py;
			double z=pz;
			double ai=px;
			double aj=py;
			for(j=1;j<tokens.length;++j) {
				if(tokens[j].startsWith("X")) x = Float.valueOf(tokens[j].substring(1)) * scale;
				if(tokens[j].startsWith("Y")) y = Float.valueOf(tokens[j].substring(1)) * scale;
				if(tokens[j].startsWith("Z")) z = Float.valueOf(tokens[j].substring(1)) * scale;
				if(tokens[j].startsWith("I")) ai = px + Float.valueOf(tokens[j].substring(1)) * scale;
				if(tokens[j].startsWith("J")) aj = py + Float.valueOf(tokens[j].substring(1)) * scale;
			}
			
			if(tokens[0].equals("G00") || tokens[0].equals("G0") ||
			   tokens[0].equals("G01") || tokens[0].equals("G1")) {
				// draw a line
				double ddx=x-px;
				double ddy=y-py;
				double dd=Math.sqrt(ddx*ddx+ddy*ddy);
				estimated_time+=dd/feed_rate;
				estimated_length+=dd;
				++estimate_count;
				px=x;
				py=y;
				pz=z;
			} else if(tokens[0].equals("G02") || tokens[0].equals("G2") ||
					  tokens[0].equals("G03") || tokens[0].equals("G3")) {
				// draw an arc
				int dir = (tokens[0].equals("G02") || tokens[0].equals("G2")) ? -1 : 1;
				double dx=px - ai;
				double dy=py - aj;
				double radius=Math.sqrt(dx*dx+dy*dy);

				// find angle of arc (sweep)
				double angle1=atan3(dy,dx);
				double angle2=atan3(y-aj,x-ai);
				double theta=angle2-angle1;

				if(dir>0 && theta<0) angle2+=2.0*Math.PI;
				else if(dir<0 && theta>0) angle1+=2.0*Math.PI;

				theta=Math.abs(angle2-angle1);
				// length of arc=theta*r (http://math.about.com/od/formulas/ss/surfaceareavol_9.htm)
				double dd = theta * radius;
				
				estimated_time+=dd/feed_rate;
				estimated_length+=dd;
				++estimate_count;
				px=x;
				py=y;
				pz=z;
			}
		}  // for ( each instruction )
	   	estimated_time += estimate_count * 0.007617845117845f;
	   	estimated_time *= 10000;
	}
	
	// close the file, clear the preview tab
	public void CloseFile() {
		if(fileOpened==true) {
			fileOpened=false;
		}
	}
	
	public void Load(String filename) throws IOException {
		CloseFile();

    	Scanner scanner = new Scanner(new FileInputStream(filename));
    	linesTotal=0;
    	lines = new ArrayList<String>();
	    try {
	      while (scanner.hasNextLine()) {
	    	  lines.add(scanner.nextLine());
	    	  ++linesTotal;
	      }
	    }
	    finally{
	      scanner.close();
	    }
	    fileOpened=true;
	    EstimateDrawTime();
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
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */