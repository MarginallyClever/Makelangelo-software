package com.marginallyclever.makelangeloRobot.loadAndSave;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.anim.dom.SVGOMPathElement;
import org.apache.batik.anim.dom.SVGOMSVGElement;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.dom.svg.SVGItem;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGPathSeg;
import org.w3c.dom.svg.SVGPathSegCurvetoCubicAbs;
import org.w3c.dom.svg.SVGPathSegLinetoAbs;
import org.w3c.dom.svg.SVGPathSegList;
import org.w3c.dom.svg.SVGPathSegMovetoAbs;

import com.marginallyclever.gcode.GCodeFile;
import com.marginallyclever.makelangelo.Log;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangeloRobot.ImageManipulator;
import com.marginallyclever.makelangeloRobot.MakelangeloRobot;

/**
 * Reads in DXF file and converts it to a temporary gcode file, then calls LoadGCode. 
 * @author Dan Royer
 *
 */
public class LoadAndSaveSVG extends ImageManipulator implements LoadAndSaveFileType {
	private static FileNameExtensionFilter filter = new FileNameExtensionFilter(Translator.get("FileTypeSVG"), "svg");
	
	protected boolean shouldScaleOnLoad = true;
	
	protected double maxX,minX,maxY,minY;
	protected double scale,imageCenterX,imageCenterY;
	
	@Override
	public String getName() { return "SVG"; }
	
	@Override
	public FileNameExtensionFilter getFileNameFilter() {
		return filter;
	}

	
	@Override
	public boolean canLoad() {
		return true;
	}

	@Override
	public boolean canLoad(String filename) {
		String ext = filename.substring(filename.lastIndexOf('.'));
		return (ext.equalsIgnoreCase(".svg"));
	}

	@Override
	public boolean load(InputStream in,MakelangeloRobot robot) {
		Log.info("Loading...");
		// set up a temporary file
		File tempFile;
		try {
			tempFile = File.createTempFile("temp", ".ngc");
		} catch (IOException e1) {
			e1.printStackTrace();
			return false;
		}
		tempFile.deleteOnExit();
		Log.info(Translator.get("Converting") + " " + tempFile.getName());

		
		Document document = newDocumentFromInputStream(in);
		initSVGDOM(document);
		NodeList pathNodes = ((SVGOMSVGElement)document.getDocumentElement()).getElementsByTagName( "path" );

	    boolean loadOK=true;

		try (FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
				Writer out = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8)) {

			// prepare for exporting
			machine = robot.getSettings();
			setAbsoluteMode(out);
			
			minX = minY =Double.MAX_VALUE;
			maxX = maxY =-Double.MAX_VALUE;
			imageCenterX=imageCenterY=0;
			scale=1;
			loadOK = parseElements(out,pathNodes,false);
			if(loadOK) {
				imageCenterX = ( maxX + minX ) / 2.0;
				imageCenterY = -( maxY + minY ) / 2.0;
	
				double imageWidth  = maxX - minX;
				double imageHeight = maxY - minY;
				double paperHeight = robot.getSettings().getPaperHeight() * robot.getSettings().getPaperMargin();
				double paperWidth  = robot.getSettings().getPaperWidth () * robot.getSettings().getPaperMargin();
	
				scale = 1;
				if(shouldScaleOnLoad) {
					double innerAspectRatio = imageWidth / imageHeight;
					double outerAspectRatio = paperWidth / paperHeight;
					scale = (innerAspectRatio >= outerAspectRatio) ?
							(paperWidth / imageWidth) :
							(paperHeight / imageHeight);
				}
				
				loadOK = parseElements(out,pathNodes,true);
			}		    
			// entities finished. Close up file.
			liftPen(out);
		    moveTo(out, (float)machine.getHomeX(), (float)machine.getHomeY(),true);
			out.flush();
			out.close();

			Log.info(loadOK?"Loaded OK!":"Failed to load some elements.");
			if(loadOK) {
				LoadAndSaveGCode loader = new LoadAndSaveGCode();
				InputStream fileInputStream = new FileInputStream(tempFile);
				loader.load(fileInputStream,robot);
			}			
		} catch (IOException e) {
			e.printStackTrace();
			loadOK=false;
		}

		return loadOK;
	}

	/**
	 * Parse through all the SVG elements and raster them to gcode.
	 * @param out the writer to send the gcode
	 * @param pathNodes the source of the elements
	 * @param write if true, write gcode.  if false, calculate bounds of rasterized elements.
	 */
	protected boolean parseElements(Writer out,NodeList pathNodes,boolean write) throws IOException {
	    boolean loadOK=true;

	    double previousX,previousY,x,y;
	    x=previousX = machine.getHomeX();
		y=previousY = machine.getHomeY();
		
	    int pathNodeCount = pathNodes.getLength();
	    for( int iPathNode = 0; iPathNode < pathNodeCount; iPathNode++ ) {
	    	SVGOMPathElement pathElement = ((SVGOMPathElement)pathNodes.item( iPathNode ));
	    	SVGPathSegList pathList = pathElement.getNormalizedPathSegList();
	    	int pathObjects = pathList.getNumberOfItems();
			//System.out.println("New Node has "+pathObjects+" elements.");

			for (int i = 0; i < pathObjects; i++) {
				SVGPathSeg item = (SVGPathSeg) pathList.getItem(i);
				switch( item.getPathSegType() ) {
				case SVGPathSeg.PATHSEG_CLOSEPATH:
					{
						//System.out.println("Close path");
						if(write) moveTo(out,previousX,previousY,false);
						x=previousX;
						y=previousY;
					}
					break;
				case SVGPathSeg.PATHSEG_MOVETO_ABS:
					{
						//System.out.println("Move Abs");
						SVGPathSegMovetoAbs path = (SVGPathSegMovetoAbs)item;
						if(write) {
							liftPen(out);
							moveTo(out,x,y,true);
						} 
						x = ( path.getX() - imageCenterX ) * scale;
						y = ( path.getY() - imageCenterY ) * -scale;
						if(write) {
							moveTo(out,x,y,true);
							lowerPen(out);
							moveTo(out,x,y,false);
						} else adjustLimits(x,y);
						previousX=x;
						previousY=y;
					}
					break;
				case SVGPathSeg.PATHSEG_LINETO_ABS:
					{
						//System.out.println("Line Abs");
						SVGPathSegLinetoAbs path = (SVGPathSegLinetoAbs)item;
						x = ( path.getX() - imageCenterX ) * scale;
						y = ( path.getY() - imageCenterY ) * -scale;
						if(write) moveTo(out,x,y,false);
						else adjustLimits(x,y);
					}
					break;
				case SVGPathSeg.PATHSEG_CURVETO_CUBIC_ABS: 
					{
						//System.out.println("Curve Cubic Abs");
						SVGPathSegCurvetoCubicAbs path = (SVGPathSegCurvetoCubicAbs)item;
						// x,y is the first point
						// x0,y0 is the first control point
						double x0=( path.getX1() - imageCenterX ) * scale;
						double y0=( path.getY1() - imageCenterY ) * -scale;
						// x1,y1 is the second control point
						double x1=( path.getX2() - imageCenterX ) * scale;
						double y1=( path.getY2() - imageCenterY ) * -scale;
						// x2,y2 is the third control point
						double x2=( path.getX() - imageCenterX ) * scale;
						double y2=( path.getY() - imageCenterY ) * -scale;

						double xabc=x,yabc=y;
						for(double j=0;j<1;j+=0.1) {
							double xa = p(x ,x0,j);
							double ya = p(y ,y0,j);
							double xb = p(x0,x1,j);
							double yb = p(y0,y1,j);
							double xc = p(x1,x2,j);
							double yc = p(y1,y2,j);
							
							double xab = p(xa,xb,j);
							double yab = p(ya,yb,j);
							double xbc = p(xb,xc,j);
							double ybc = p(yb,yc,j);
							
							xabc = p(xab,xbc,j);
							yabc = p(yab,ybc,j);
							
							if(write) moveTo(out,xabc,yabc,false);
							else adjustLimits(xabc,yabc);
						}
						x = xabc;
						y = yabc;
					}
					break; 
				default:
					System.out.print(item.getPathSegTypeAsLetter()+" "+item.getPathSegType()+" = ");
					System.out.println(((SVGItem)item).getValueAsString());
					loadOK=false;
				}
			}
		}
	    return loadOK;
	}
	
	protected void adjustLimits(double x,double y) {
		if(minX>x) minX = x;
		if(maxX<x) maxX = x;
		if(minY>y) minY = y;
		if(maxY<y) maxY = y;
	}

	protected double p(double a,double b,double fraction) {
		return ( b - a ) * fraction + a;
	}
	

	/**
	 * Enhance the SVG DOM for the given document to provide CSS- and
	 * SVG-specific DOM interfaces.
	 * 
	 * @param document
	 *            The document to enhance.
	 * @link http://wiki.apache.org/xmlgraphics-batik/BootSvgAndCssDom
	 */
	private void initSVGDOM(Document document) {
		UserAgent userAgent = new UserAgentAdapter();
		DocumentLoader loader = new DocumentLoader(userAgent);
		BridgeContext bridgeContext = new BridgeContext(userAgent, loader);
		bridgeContext.setDynamicState(BridgeContext.DYNAMIC);

		// Enable CSS- and SVG-specific enhancements.
		(new GVTBuilder()).build(bridgeContext, document);
	}

	public static SVGDocument newDocumentFromInputStream(InputStream in) {
		SVGDocument ret = null;

		try {
			String parser = XMLResourceDescriptor.getXMLParserClassName();
	        SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);
	        ret = (SVGDocument) factory.createDocument("",in);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return ret;
	}
	  
	@Override
	public boolean canSave() {
		return true;
	}
	
	@Override
	public boolean canSave(String filename) {
		String ext = filename.substring(filename.lastIndexOf('.'));
		return (ext.equalsIgnoreCase(".svg"));
	}


	@Override
	/**
	 * see http://paulbourke.net/dataformats/dxf/min3d.html for details
	 * @param outputStream where to write the data
	 * @param robot the robot from which the data is obtained
	 * @return true if save succeeded.
	 */
	public boolean save(OutputStream outputStream, MakelangeloRobot robot) {
		Log.info("saving...");
		GCodeFile sourceMaterial = robot.gCode;
		sourceMaterial.setLinesProcessed(0);

		machine = robot.getSettings();
		double left = machine.getPaperLeft()*10;
		double top = machine.getPaperTop()*10;
		double right = machine.getPaperRight()*10;
		double bottom = top - machine.getPaperBottom()*10;
		
		OutputStreamWriter out = new OutputStreamWriter(outputStream);
		try {
			// header
			out.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n");
			out.write("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n");
			out.write("<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" viewBox=\"0 0 "+right+" "+bottom+"\">\n");

			boolean penUp=true;
			float x0 = (float) robot.getSettings().getHomeX();
			float y0 = (float) robot.getSettings().getHomeY();
			float x1;
			float y1;
			
			String matchUp = robot.getSettings().getPenUpString();
			String matchDown = robot.getSettings().getPenDownString();
			if(matchUp.contains(";")) {
				matchUp = matchUp.substring(0, matchUp.indexOf(";"));
			}
			matchUp = matchUp.replaceAll("\n", "");
			
			if(matchDown.contains(";")) {
				matchDown = matchDown.substring(0, matchDown.indexOf(";"));
			}
			matchDown = matchDown.replaceAll("\n", "");
			
			int total=sourceMaterial.getLinesTotal();
			Log.info(total+" total lines to save.");
			for(int i=0;i<total;++i) {
				String str = sourceMaterial.nextLine();
				// trim comments
				if(str.contains(";")) {
					str = str.substring(0, str.indexOf(";"));
				}
				if(str.contains(matchUp)) {
					penUp=true;
				}
				if(str.contains(matchDown)) {
					penUp=false;
				}
				if(str.startsWith("G0") || str.startsWith("G1")) {
					// move command
					String[] tokens = str.split(" ");
					x1=x0;
					y1=y0;
					int j;
					for(j=0;j<tokens.length;++j) {
						String tok = tokens[j];
						if(tok.startsWith("X")) {
							x1=Float.parseFloat(tok.substring(1));
						} else if(tok.startsWith("Y")) {
							y1=Float.parseFloat(tok.substring(1));
						}
					}
					if(penUp==false && ( x1!=x0 || y1!=y0 ) ) {
						double svgX1 = roundOff3(x0 - left);
						double svgX2 = roundOff3(x1 - left);
						double svgY1 = roundOff3(top - y0);
						double svgY2 = roundOff3(top - y1);
						out.write("  <line");
						out.write(" x1=\""+svgX1+"\"");
						out.write(" y1=\""+svgY1+"\"");
						out.write(" x2=\""+svgX2+"\"");
						out.write(" y2=\""+svgY2+"\"");
						out.write(" stroke=\"black\"");
						//out.write(" stroke-width=\"1\"");
						out.write(" />\n");
					}
					x0=x1;
					y0=y1;
				}
			}
			// footer
			out.write("</svg>");
			// end
			out.flush();
		}
		catch(IOException e) {
			Log.error(Translator.get("SaveError") +" "+ e.getLocalizedMessage());
			return false;
		}
		
		Log.info("done.");
		return true;
	}

	/**
	 * Round a float off to 3 decimal places.
	 * @param v a value
	 * @return Value rounded off to 3 decimal places
	 */
	public static double roundOff3(double v) {
		double SCALE = 1000.0f;
		
		return Math.round(v*SCALE)/SCALE;
	}
}
