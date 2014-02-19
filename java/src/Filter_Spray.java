import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;


public class Filter_Spray extends Filter {
	String dest;
	String previous_command;
	double scale,iscale;
	boolean up;
	boolean lastup;
	float w2,h2;
	float feed_rate=2000;
	long image_width;
	long image_height;
	long numPoints;

	Filter_Spray(String _dest,double _scale) {
		dest=_dest;
		scale=_scale;
	}
	
	private void MoveTo(BufferedWriter out,double x,double y,boolean up) throws IOException {
		String command="G00 X"+RoundOff((x-w2)*iscale) + " Y" + RoundOff((h2-y)*iscale)+";\n";
		if(up) {
			previous_command=command;
		}
		if(lastup!=up && !up) {
			out.write(previous_command);
		}
		if(!up) {
			out.write(command);
		}
		if(lastup!=up) {
			if(up) liftPen(out);
			else   lowerPen(out);
		}
		lastup=up;
	}


	private void liftPen(BufferedWriter out) throws IOException {
		out.write("G00 Z"+MachineConfiguration.getSingleton().getPenUpString()+" F80;\n");  // lower the pen.
		out.write("G00 F"+feed_rate+";\n");
	}
	
	
	private void lowerPen(BufferedWriter out) throws IOException {
		out.write("G00 Z"+MachineConfiguration.getSingleton().getPenDownString()+" F80;\n");  // lower the pen.
		out.write("G00 F"+feed_rate+";\n");
	}

	/**
	 * The main entry point
	 * @param img the image to convert.
	 */
	public void Process(BufferedImage img) throws IOException {
		image_height = img.getHeight();
		image_width = img.getWidth();
		int x,y,i;
		
		w2=image_width/2;
		h2=image_height/2;
		iscale=1.0/scale;
		
		
		BufferedWriter out = new BufferedWriter(new FileWriter(dest));
		out.write(MachineConfiguration.getSingleton().GetConfigLine()+";\n");
		out.write(MachineConfiguration.getSingleton().GetBobbinLine()+";\n");
		// change to tool 2
		out.write("M06 T2;\n");
		// set absolute coordinates, lift pen
		out.write("G90;\n");
		liftPen(out);
		
		for(y=0;y<image_height;++y) {
			for(x=0;x<image_width;++x) {
				i=decode(img.getRGB(x,y));
				if(i==0) {
					MoveTo(out,x,y,true);
					MoveTo(out,x+1,y,false);
				}
			}
		}
		
		// lift pen and return to home
		liftPen(out);
		out.write("G00 X0 Y0;\n");
		out.close();

		// TODO move to GUI
		DrawbotGUI.getSingleton().Log("<font color='green'>Completed.</font>\n");
		DrawbotGUI.getSingleton().PlayConversionFinishedSound();
		DrawbotGUI.getSingleton().LoadGCode(dest);
	}
}
