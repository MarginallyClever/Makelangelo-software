import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;


/**
 * Generate a Gcode file from the BufferedImage supplied.<br>
 * Use the filename given in the constructor as a basis for the gcode filename, but change the extension to .ngc 
 * @author Dan
 */
class Filter_TSPGcodeGenerator extends Filter implements PropertyChangeListener {
	public class Point {
		int x, y;
		
		public Point(int _x,int _y) {
			x=_x;
			y=_y;
		}
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
	
	
	private class TSPOptimizer extends SwingWorker<Void,Void> {
		@Override
		public Void doInBackground() {
			int start, end, i, j, once;
			
			setProgress(0);

			double len=GetTourLength(solution);
			DecimalFormat flen=new DecimalFormat("#.##");
			DrawbotGUI.getSingleton().Log(flen.format(len)+"mm @ 0s\n");

			long t_elapsed=0;
			long t_start = System.currentTimeMillis();

			do {
				once=0;
				for(start=0;start<numPoints-1 && !isCancelled();++start) {
					for(end=start+2;end<=numPoints;++end) {
						// we have s1,s2...e-1,e
						// check if s1,e-1,...s2,e is shorter
						// before
						long a=CalculateWeight(solution[start],solution[start+1]);
						long b=CalculateWeight(solution[end  ],solution[end  -1]);
						// after
						long c=CalculateWeight(solution[start],solution[end  -1]);
						long d=CalculateWeight(solution[end  ],solution[start+1]);
						
						if(a+b>c+d) {
							once = 1;
							// do the flip
							i=0;
							for(j=start+1;j<end;++j) {
								solution2[i]=solution[j];
								++i;
							}
							for(j=start+1;j<end;++j) {
								--i;
								solution[j]=solution2[i];
							}
							t_elapsed=System.currentTimeMillis()-t_start;
							// find the new tour length
							double diff=(Math.sqrt(a)+Math.sqrt(b)) - (Math.sqrt(c)+Math.sqrt(d));
							double newlen=len-diff;
							assert(newlen>len);
							len=newlen;
							
							DrawbotGUI.getSingleton().Log(flen.format(len)+"mm @ "+formatTime(t_elapsed)+": "+start+"\t"+end+"\n");
							setProgress((int)((float)t_elapsed/(float)time_limit));
						}
					}
				}
				// check if moving a point to another part of the tour makes the tour shorter
				for(start=0;start<numPoints-2 && !isCancelled();++start) {
					for(end=start+4;end<=numPoints;++end) {
						// we have points s1,s2,s3,...e-1,e.
						// check if s1,s3,...e-2,s2,e is shorter
						// before
						int p1=solution[start  ];
						int p2=solution[start+1];
						int p3=solution[start+2];
						int p4=solution[end  -1];
						int p5=solution[end    ];
						long a=CalculateWeight(p1,p2);
						long b=CalculateWeight(p2,p3);
						long c=CalculateWeight(p4,p5);
						// after
						long d=CalculateWeight(p1,p3);
						long e=CalculateWeight(p4,p2);
						long f=CalculateWeight(p2,p5);
						
						if(a+b+c>d+e+f) {
							once = 1;
							// do move
							for(j=start+1;j<end-1;++j) {
								solution[j]=solution[j+1];
							}
							solution[j]=p2;
							t_elapsed=System.currentTimeMillis()-t_start;
							// find the new tour length
							len-=(Math.sqrt(a)+Math.sqrt(b)+Math.sqrt(c)) - (Math.sqrt(d)+Math.sqrt(e)+Math.sqrt(f));
							
							DrawbotGUI.getSingleton().Log(flen.format(len)+"mm @2 "+formatTime(t_elapsed)+": "+start+"\t"+end+"\n");
							setProgress((int)(100.0f*(float)t_elapsed/(float)time_limit));
							if(end>1) end--;
						}
					}
				}
				// check if moving a point to another part of the tour makes the tour shorter
				for(start=0;start<numPoints-2 && !isCancelled();++start) {
					for(end=start+4;end<=numPoints;++end) {
						// we have points s1,s2,s3,...e-1,e.
						// check if s1,s3,...e-2,s2,e is shorter
						// before
						int p1=solution[start  ];
						int p2=solution[start+1];
						int p3=solution[end  -2];
						int p4=solution[end  -1];
						int p5=solution[end    ];
						long a=CalculateWeight(p1,p2);
						long b=CalculateWeight(p3,p4);
						long c=CalculateWeight(p4,p5);
						// after
						long d=CalculateWeight(p1,p4);
						long e=CalculateWeight(p4,p2);
						long f=CalculateWeight(p3,p5);
						
						if(a+b+c>d+e+f) {
							once = 1;
							// do move
							for(j=end-1;j>start+1;--j) {
								solution[j]=solution[j-1];
							}
							solution[j]=p4;
							t_elapsed=System.currentTimeMillis()-t_start;
							// find the new tour length
							len-=(Math.sqrt(a)+Math.sqrt(b)+Math.sqrt(c)) - (Math.sqrt(d)+Math.sqrt(e)+Math.sqrt(f));
							
							DrawbotGUI.getSingleton().Log(flen.format(len)+"mm @3 "+formatTime(t_elapsed)+": "+start+"\t"+end+"\n");
							setProgress((int)(100.0f*(float)t_elapsed/(float)time_limit));
						}
					}
				}
				//len=GetTourLength(solution);
			} while(once==1 && t_elapsed<time_limit && !isCancelled());
			
			return null;
		}
		
		//@override
		public void done() {
            Toolkit.getDefaultToolkit().beep();
            pm.setProgress(0);
            pm.close();
			ConvertAndSaveToGCode();
		}
	}

	
	long time_limit=10*60*1000;  // 10 minutes
	String dest;
	int numPoints;
	Point[] points = null;
	int[] solution = null;
	int[] solution2 = null;
	int image_width, image_height;
	int scount;
	ProgressMonitor pm;
	TSPOptimizer task;
	
	
	Filter_TSPGcodeGenerator(String _dest) {
		dest=_dest;
	}

	
	protected long CalculateWeight(int a,int b) {
		long x = points[a].x - points[b].x;
		long y = points[a].y - points[b].y;
		return x*x+y*y;
	}

	
	private void GenerateTSP() {
		GreedyTour();

		DrawbotGUI.getSingleton().Log("Running Lin/Kerighan optimization...\n");

		pm = new ProgressMonitor(null, "Optimizing path...", "", 0, 100);
		pm.setProgress(0);
		pm.setMillisToPopup(0);
		task=new TSPOptimizer();
		task.addPropertyChangeListener(this);
		task.execute();
	}

	
    /**
     * Invoked when task's progress property changes.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress" == evt.getPropertyName() ) {
            int progress = (Integer) evt.getNewValue();
            pm.setProgress(progress);
            String message = String.format("Completed %d%%.\n", progress);
            pm.setNote(message);
            if (pm.isCanceled() || task.isDone()) {
                Toolkit.getDefaultToolkit().beep();
                if (pm.isCanceled()) {
                    task.cancel(true);
                    DrawbotGUI.getSingleton().Log("Task canceled.\n");
                } else {
                	DrawbotGUI.getSingleton().Log("Task completed.\n");
                }
            }
        }
 
    }
    
    
	private double CalculateLength(int a,int b) {
		return Math.sqrt(CalculateWeight(a,b));
	}
	
	
	/**
	 * Get the length of a tour segment
	 * @param list an array of indexes into the point list.  the order forms the tour sequence.
	 * @param start the index of the first point of the tour segment
	 * @param end the index of the last point of the tour segment
	 * @return the length of the tour
	 */
	private double GetTourLength(int[] list) {
		double w=0;
		for(int i=0;i<numPoints-1;++i) {
			w+=CalculateLength(list[i],list[i+1]);
		}
		return w;
	}

	
	/**
	 * Starting with point 0, find the next nearest point and repeat until all points have been "found".
	 */
	private void GreedyTour() {
		DrawbotGUI.getSingleton().Log("Finding greedy tour solution...\n");

		int i;
		long w, bestw;
		int besti;
		
		// put all the points in the solution in no particular order.
		for(i=0;i<numPoints;++i) {
			solution[i]=i;
		}
		scount=1;
		
		solution[numPoints]=solution[0];
		
		do {
			// Find the nearest point not already in the line.
			// Any solution[n] where n>scount is not in the line.
			bestw=CalculateWeight(solution[scount],solution[scount-1]);
			besti=solution[scount];
			for( i=scount; i<numPoints; ++i ) {
				w=CalculateWeight(solution[i],solution[scount-1]);
				if( w < bestw ) {
					bestw=w;
					besti=i;
				}
			}
			i=solution[scount];
			solution[scount]=solution[besti];
			solution[besti]=i;
			scount++;
		} while(scount<numPoints);
	}
	
	
	/**
	 * Open a file and write out the edge list as a set of GCode commands.
	 * Since all the points are connected in a single loop,
	 * start at the tsp point closest to the calibration point and go around until you get back to the start.
	 */
	private void ConvertAndSaveToGCode() {
		DrawbotGUI.getSingleton().Log("Converting to gcode and saving "+dest+"\n");
		
		int w2=image_width/2;
		int h2=image_height/2;
		
		// find the tsp point closest to the calibration point
		int i;
		int besti=-1;
		int bestw=1000000;
		int x,y,w;
		for(i=0;i<numPoints;++i) {
			x=points[solution[i]].x-w2;
			y=points[solution[i]].y-h2;
			w=x*x+y*y;
			if(w<bestw) {
				bestw=w;
				besti=i;
			}
		}
		// rearrange the tsp point list so that the drawing starts at the nearest tsp point
		for(i=besti;i<numPoints;++i) {
			solution2[i-besti]=solution[i];
		}
		for(i=0;i<besti;++i) {
			solution2[i+(numPoints-besti)]=solution[i];
		}

		solution=solution2;
		
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(dest));
			out.write("M06 T0\n");
			out.write("G00 F300 Z90\n");
			out.write("G01 X" + (points[solution[0]].x-w2) + " Y" + (h2-points[solution[0]].y) + "\n");
			out.write("G00 Z10\n");

			for(i=1;i<numPoints;++i) {
				out.write("G01 X" + (points[solution[i]].x-w2) + " Y" + (h2-points[solution[i]].y) + "\n");
			}
			out.write("G01 X" + (points[solution[0]].x-w2) + " Y" + (h2-points[solution[0]].y) + "\n");
			out.write("G00 Z90\n");
			out.write("G00 X0 Y0\n");
			out.close();
		}
		catch(IOException e) {
			DrawbotGUI.getSingleton().Log("Error saving "+dest+": "+e.getMessage());
		}
		DrawbotGUI.getSingleton().Log("Completed.\n");
	}
	
	
	/**
	 * The main entry point
	 * @param img the image to convert.
	 */
	public void Process(BufferedImage img) {
		System.out.println("Processing...");
		image_height = img.getHeight();
		image_width = img.getWidth();
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
		
		DrawbotGUI.getSingleton().Log(numPoints + " points\n");
		points = new Point[numPoints+1];
		solution = new int[numPoints+1];
		solution2 = new int[numPoints+1];

		// collect the point data
		numPoints=0;
		for(y=0;y<image_height;++y) {
			for(x=0;x<image_width;++x) {
				i=decode(img.getRGB(x,y));
				if(i==0) {
					points[numPoints++]=new Point(x,y);
				}
			}
		}
		
		
		GenerateTSP();
	}
}