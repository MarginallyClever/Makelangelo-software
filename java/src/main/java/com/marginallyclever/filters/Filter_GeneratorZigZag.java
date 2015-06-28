package com.marginallyclever.filters;


import com.jogamp.opengl.GL2;
import com.marginallyclever.basictypes.Point2D;
import com.marginallyclever.makelangelo.DrawDecorator;
import com.marginallyclever.makelangelo.MachineConfiguration;
import com.marginallyclever.makelangelo.MainGUI;
import com.marginallyclever.makelangelo.MultilingualSupport;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Generate a Gcode file from the BufferedImage supplied.<br>
 * Use the filename given in the constructor as a basis for the gcode filename, but change the extension to .ngc 
 * @author Dan
 */
public class Filter_GeneratorZigZag extends Filter implements DrawDecorator {
	private ReentrantLock lock = new ReentrantLock();
	
	@Override
	public String getName() { return translator.get("ZigZagName"); }
	
	// processing tools
	long t_elapsed,t_start;
	double progress;
	double old_len,len;
	long time_limit=10*60*1000;  // 10 minutes

	int numPoints;
	Point2D[] points = null;
	int[] solution = null;
	int scount;
	
	
	public Filter_GeneratorZigZag(MainGUI gui,MachineConfiguration mc,MultilingualSupport ms) {
		super(gui,mc,ms);
	}
	

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
	

	public void updateProgress(double len,int color) {
		t_elapsed=System.currentTimeMillis()-t_start;
		double new_progress = 100.0 * (double)t_elapsed / (double)time_limit;
		if( new_progress > progress + 0.1 ) {
			// find the new tour length
			len=getTourLength(solution);
			if( old_len > len ) {
				old_len=len;
				DecimalFormat flen=new DecimalFormat("#.##");
				String c;
				switch(color) {
				case  0: c="yellow";  break;
				case  1: c="blue";    break;
				case  2: c="red";	  break;
				default: c="white";   break;
				}
				mainGUI.log("<font color='" + c + "'>" + formatTime(t_elapsed) + ": " + flen.format(len) + "mm</font>\n");
			}
			progress = new_progress;
			pm.setProgress((int)progress);
		}
	}
		
/*
	// does moving a point to somewhere else in the series shorten the series?
	// @TODO: Debug, it doesn't seem to work.
	// we have s1,s2,s3...e-1,e
	// check if s1,s3,...e-1,s2,e is shorter
	private int transposeForwardTest() {
		int start, end, j, once=0;
		
		for(start=0;start<numPoints-4 && !isCancelled();++start) {
			float a=CalculateWeight(solution[start],solution[start+1]);  // s1->s2
			float b=CalculateWeight(solution[start+1],solution[start+2]);  // s2->s3
			float d=CalculateWeight(solution[start],solution[start+2]);  // s1->s3
			// a+b > d always true, the line gets shorter at this end
			//assert(a+b>d);
			float abd=a+b-d;

			int best_end=-1;
			double best_diff=0;
			
			for(end=start+4;end<numPoints && !isCancelled();++end) {
				// before
				float c=CalculateWeight(solution[end-1],solution[end]);  // e-1->e
				// after
				float e=CalculateWeight(solution[end-1],solution[start+1]);  // e-1->s2
				float f=CalculateWeight(solution[start+1],solution[end]);  // s2->e
				// e+f > c always true, the line gets longer at this end
				//assert(e+f>c);
				float efc = e+f-c;

				float temp_diff = abd-efc;
				if( best_diff < temp_diff ) {
					best_diff = temp_diff;
					best_end=end;
					//DrawbotGUI.getSingleton().Log("<font color='red'>"+best_diff+"</font>\n");
				}
			}
			
			if(best_end != -1 && !isCancelled()) {
				once = 1;
				// move start+1 to just before end
				int temp=solution[start+1];
				for(j=start+1;j<best_end-1;++j) {
					solution[j]=solution[j+1];
				}
				solution[best_end-1]=temp;

				UpdateProgress(len,0);
			}
		}
		return once;
	}*/
/*
	// does moving a point to somewhere else in the series shorten the series?
	// @TODO: Debug, it doesn't seem to work.
	// we have s1,s2...e-2,e-1,e
	// check if s1,e-1,s2...e-2,e is shorter
	private int transposeBackwardTest() {
		int start, end, j, once=0;
		
		for(start=0;start<numPoints-4 && !isCancelled();++start) {
			float a=CalculateWeight(solution[start],solution[start+1]);  // s1->s2

			int best_end=-1;
			double best_diff=0;
			
			for(end=start+4;end<numPoints && !isCancelled();++end) {
				float b=CalculateWeight(solution[end-2],solution[end-1]);  // e2->e1
				float c=CalculateWeight(solution[end-1],solution[end]);  // e1->e
				float f=CalculateWeight(solution[end-2],solution[end]);  // e2->e
				// b+c > f, this end is getting shorter
				assert(b+c>f);
				float bcf=b+c-f;
				
				float d=CalculateWeight(solution[start],solution[end-1]);  // s1->e1
				float e=CalculateWeight(solution[end-1],solution[start+1]);  // e1->s2
				// d+e>a, this end is getting longer
				assert(d+e>a);
				float dea=d+e-a;

				float temp_diff = bcf-dea;
				if( best_diff < temp_diff ) {
					best_diff = temp_diff;
					best_end=end;
				}
			}
			
			if(best_end != -1 && !isCancelled()) {
				once = 1;
				// move best_end-1 to just after start
				int temp=solution[best_end-1];
				for(j=best_end-1;j>start+1;--j) {
					solution[j]=solution[j-1];
				}
				solution[start+1]=temp;

				UpdateProgress(len,3);
			}
		}
		return once;
	}*/

	// we have s1,s2...e-1,e
	// check if s1,e-1,...s2,e is shorter
	public int flipTests() {
		int start, end, j, once=0;
		
		for(start=0;start<numPoints-2 && !parent.isCancelled() && !pm.isCanceled();++start) {
			float a=calculateWeight(solution[start],solution[start+1]);
			int best_end=-1;
			double best_diff=0;
			
			for(end=start+2;end<=numPoints && !parent.isCancelled() && !pm.isCanceled();++end) {
				// before
				float b=calculateWeight(solution[end  ],solution[end  -1]);
				// after
				float c=calculateWeight(solution[start],solution[end  -1]);
				float d=calculateWeight(solution[end  ],solution[start+1]);
				
				double temp_diff=(a+b)-(c+d);
				if(best_diff < temp_diff) {
					best_diff = temp_diff;
					best_end=end;
				}
			}
			
			if(best_end != -1 && !parent.isCancelled() && !pm.isCanceled()) {
				once = 1;
				// do the flip
				int begin=start+1;
				int finish=best_end;
				int half=(finish-begin)/2;
				int temp;
				while(lock.isLocked());
				
				lock.lock();	
				//DrawbotGUI.getSingleton().Log("<font color='red'>flipping "+(finish-begin)+"</font>\n");
				for(j=0;j<half;++j) {
					temp = solution[begin+j];
					solution[begin+j]=solution[finish-1-j];
					solution[finish-1-j]=temp;
				}
				lock.unlock();
				mainGUI.getDrawPanel().repaintNow();
				updateProgress(len,1);
			}
		}
		return once;
	}
	
	
	public void render(GL2 gl2, MachineConfiguration machine) {
		if(points==null || solution==null ) return;
		
		while(lock.isLocked());
		
		lock.lock();
		
		gl2.glColor3f(0,0,0);
		gl2.glBegin(GL2.GL_LINE_STRIP);
		for(int i=0;i<points.length;++i) {
			if(points[solution[i]]==null) break;
			gl2.glVertex2f((points[solution[i]].x)*0.1f,
						(points[solution[i]].y)*0.1f);
		}
		gl2.glEnd();
		
		lock.unlock();
	}
	
	
	protected float calculateWeight(int a,int b) {
		float x = points[a].x - points[b].x;
		float y = points[a].y - points[b].y;
		return x*x+y*y;
	}
	
	
	private void generateTSP() {
		greedyTour();

		mainGUI.log("<font color='green'>Running Lin/Kerighan optimization...</font>\n");

		len=getTourLength(solution);
		old_len=len;
		
		t_elapsed=0;
		t_start = System.currentTimeMillis();
		progress=0;
		updateProgress(len,2);

		int once=1;
		while(once==1 && t_elapsed<time_limit && !parent.isCancelled()) {
			once=0;
			//@TODO: make these optional for the very thorough people
			//once|=transposeForwardTest();
			//once|=transposeBackwardTest();
		
			once|=flipTests();
			
			updateProgress(len,2);
		}
		
		convertAndSaveToGCode();
	}
	
    
	private double calculateLength(int a,int b) {
		return Math.sqrt(calculateWeight(a,b));
	}
	
	/**
	 * Get the length of a tour segment
	 * @param list an array of indexes into the point list.  the order forms the tour sequence.
	 * @return the length of the tour
	 */
	private double getTourLength(int[] list) {
		double w=0;
		for(int i=0;i<numPoints-1;++i) {
			w+=calculateLength(list[i],list[i+1]);
		}
		return w;
	}

	/**
	 * Starting with point 0, find the next nearest point and repeat until all points have been "found".
	 */
	private void greedyTour() {
		mainGUI.log("<font color='green'>Finding greedy tour solution...</font>\n");

		int i;
		float w, bestw;
		int besti;
		
		// put all the points in the solution in no particular order.
		for(i=0;i<numPoints;++i) {
			solution[i]=i;
		}
		

		int scount=0;
		solution[scount]=solution[0];
		
		do {
			// Find the nearest point not already in the line.
			// Any solution[n] where n>scount is not in the line.
			bestw=calculateWeight(solution[scount],solution[scount+1]);
			besti=scount+1;
			for( i=scount+2; i<numPoints; ++i ) {
				w=calculateWeight(solution[scount],solution[i]);
				if( w < bestw ) {
					bestw=w;
					besti=i;
				}
			}
			i=solution[scount+1];
			solution[scount+1]=solution[besti];
			solution[besti]=i;
			scount++;
		} while(scount<numPoints);
	}
	

	private void moveTo(Writer out,int i,boolean up) throws IOException {
		tool.writeMoveTo(out, points[solution[i]].x, points[solution[i]].y);
	}
	
	
	/**
	 * Open a file and write out the edge list as a set of GCode commands.
	 * Since all the points are connected in a single loop,
	 * start at the tsp point closest to the calibration point and go around until you get back to the start.
	 */
	private void convertAndSaveToGCode() {
		// find the tsp point closest to the calibration point
		int i;
		int besti=-1;
		float bestw=1000000;
		float x,y,w;
		for(i=0;i<numPoints;++i) {
			x=points[solution[i]].x;
			y=points[solution[i]].y;
			w=x*x+y*y;
			if(w<bestw) {
				bestw=w;
				besti=i;
			}
		}
		
		// write
        try(
        final OutputStream fileOutputStream = new FileOutputStream(dest);
        final Writer out = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);
        ) {
			out.write(machine.getConfigLine()+";\n");
			out.write(machine.getBobbinLine()+";\n");

			//MachineConfiguration mc = machine;
			//tool = mc.GetCurrentTool();
			
			setAbsoluteMode(out);
			tool.writeChangeTo(out);
			liftPen(out);
			// move to the first point
			moveTo(out,besti,false);
			lowerPen(out);

			for(i=1;i<numPoints;++i) {
				moveTo(out,(besti+i)%numPoints,false);
			}
			moveTo(out,besti,false);
			
			// lift pen and return to home
			liftPen(out);
			signName(out);
			tool.writeMoveTo(out,0,0);
		}
		catch(IOException e) {
			mainGUI.log("<font color='red'>Error saving " + dest + ": " + e.getMessage() + "</font>");
		}
	}
	
	
	protected void connectTheDots(BufferedImage img) {
		tool = machine.getCurrentTool();
		imageSetupTransform(img);

		int x,y,i;
		// count the points
		numPoints=0;
		for(y=0;y<image_height;++y) {
			for(x=0;x<image_width;++x) {
				i=decode(img.getRGB(x,y));
				if(i==0) {
					++numPoints;
				}
			}
		}
		
		mainGUI.log("<font color='green'>" + numPoints + " points,</font>\n");
		points = new Point2D[numPoints+1];
		solution = new int[numPoints+1];
	
		// collect the point data
		numPoints=0;
		for(y=0;y<image_height;++y) {
			for(x=0;x<image_width;++x) {
				i=decode(img.getRGB(x,y));
				if(i==0) {
					points[numPoints++]=new Point2D(TX(x),TY(y));
				}
			}
		}
	}
	
	/**
	 * The main entry point
	 * @param img the image to convert.
	 */
	@Override
	public void convert(BufferedImage img) {
		
		mainGUI.getDrawPanel().setDecorator(this);

		// resize & flip as needed
		Filter_Resize rs = new Filter_Resize(mainGUI,machine,translator,250,250); 
		img = rs.process(img);
		// make black & white
		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(mainGUI,machine,translator,255);
		img = bw.process(img);
		// dither
		Filter_DitherFloydSteinberg dither = new Filter_DitherFloydSteinberg(mainGUI,machine,translator);
		img = dither.process(img);
		// connect the dots
		connectTheDots(img);
		// Shorten the line that connects the dots
		generateTSP();

		mainGUI.getDrawPanel().setDecorator(null);
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