package com.marginallyclever.artPipeline.loadAndSave;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.anim.dom.SVGOMPathElement;
import org.apache.batik.anim.dom.SVGOMPolylineElement;
import org.apache.batik.anim.dom.SVGOMSVGElement;
import org.apache.batik.anim.dom.SVGPointShapeElement;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.dom.svg.SVGItem;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGPathSeg;
import org.w3c.dom.svg.SVGPathSegCurvetoCubicAbs;
import org.w3c.dom.svg.SVGPathSegLinetoAbs;
import org.w3c.dom.svg.SVGPathSegList;
import org.w3c.dom.svg.SVGPathSegMovetoAbs;
import org.w3c.dom.svg.SVGPoint;
import org.w3c.dom.svg.SVGPointList;

import com.marginallyclever.artPipeline.ImageManipulator;
import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.convenience.Turtle;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangeloRobot.MakelangeloRobot;

/**
 * Reads in SVG file and converts it to a temporary gcode file, then calls LoadGCode. 
 * @author Dan Royer
 * See https://www.w3.org/TR/SVG/paths.html
 */
public class LoadAndSaveSVG extends ImageManipulator implements LoadAndSaveFileType {
	private static FileNameExtensionFilter filter = new FileNameExtensionFilter(Translator.get("FileTypeSVG"), "svg");
	
	protected boolean shouldScaleOnLoad = true;
	
	protected double scale,imageCenterX,imageCenterY;
	protected double toolMinimumStepSize = 1; //mm
	
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
		Log.message("Loading...");
		
		Document document = newDocumentFromInputStream(in);
		initSVGDOM(document);

		// prepare for importing
		machine = robot.getSettings();
		imageCenterX=imageCenterY=0;
		scale=1;
		
	    turtle = new Turtle();
	    turtle.setX(machine.getHomeX());
	    turtle.setY(machine.getHomeX());
		turtle.setColor(new ColorRGB(0,0,0));
		boolean loadOK = parseAll(document);
		if(!loadOK) {
			Log.message("Failed to load some elements (1)");
			return false;
		}
		
		Point2D top = new Point2D();
		Point2D bottom = new Point2D();
		
		turtle.getBounds(top, bottom);
		imageCenterX = ( top.x + bottom.x ) / 2.0;
		imageCenterY = -( top.y + bottom.y ) / 2.0;

		double imageWidth  = top.x - bottom.x;
		double imageHeight = top.y - bottom.y;

		// add 2% for margins

		imageWidth += imageWidth * .02;
		imageHeight += imageHeight * .02;

		double paperHeight = robot.getSettings().getMarginHeight();
		double paperWidth  = robot.getSettings().getMarginWidth ();

		scale = 1;
		if(shouldScaleOnLoad) {
			double innerAspectRatio = imageWidth / imageHeight;
			double outerAspectRatio = paperWidth / paperHeight;
			scale = (innerAspectRatio >= outerAspectRatio) ?
					(paperWidth / imageWidth) :
					(paperHeight / imageHeight);
		}
	    turtle = new Turtle();
	    turtle.setX(machine.getHomeX());
	    turtle.setY(machine.getHomeX());
		turtle.setColor(new ColorRGB(0,0,0));
		loadOK = parseAll(document);
		if(!loadOK) {
			Log.message("Failed to load some elements (2)");
			return false;
		}
		
		robot.setTurtle(turtle);
		return true;
	}
	
	protected boolean parseAll(Document document) {
		SVGOMSVGElement documentElement = (SVGOMSVGElement)document.getDocumentElement();

		boolean    loadOK = parsePathElements(    documentElement.getElementsByTagName( "path"     ));
		if(loadOK) loadOK = parsePolylineElements(documentElement.getElementsByTagName( "polyline" ));
		if(loadOK) loadOK = parsePolylineElements(documentElement.getElementsByTagName( "polygon"  ));
		if(loadOK) loadOK = parseLineElements(    documentElement.getElementsByTagName( "line"     ));
		if(loadOK) loadOK = parseRectElements(    documentElement.getElementsByTagName( "rect"     ));
		if(loadOK) loadOK = parseCircleElements(  documentElement.getElementsByTagName( "circle"   ));
		if(loadOK) loadOK = parseEllipseElements( documentElement.getElementsByTagName( "ellipse"  ));
		return loadOK;
	}

	protected double TX(double x) {
		return ( x - imageCenterX ) * scale;
	}
	
	protected double TY(double y) {
		return ( y - imageCenterY ) * -scale;
	}
	
	/**
	 * Parse through all the SVG polyline elements and raster them to gcode.
	 * @param pathNodes the source of the elements
	 */
	protected boolean parsePolylineElements(NodeList pathNodes) {
	    boolean loadOK=true;
		
	    int pathNodeCount = pathNodes.getLength();
	    for( int iPathNode = 0; iPathNode < pathNodeCount; iPathNode++ ) {
	    	SVGPointShapeElement pathElement = (SVGPointShapeElement)pathNodes.item( iPathNode );
	    	SVGPointList pointList = pathElement.getAnimatedPoints();
	    	int numPoints = pointList.getNumberOfItems();
			//Log.message("New Node has "+pathObjects+" elements.");

			SVGPoint item = (SVGPoint)pointList.getItem(0);
			double x = TX( item.getX() );
			double y = TY( item.getY() );
			turtle.jumpTo(x,y);
			
			for( int i=1; i<numPoints; ++i ) {
				item = (SVGPoint)pointList.getItem(i);
				x = TX( item.getX() );
				y = TY( item.getY() );
				turtle.moveTo(x,y);
			}
		}
	    return loadOK;
	}
	
	double distanceSquared(SVGPoint item,double previousX,double previousY) {
		double x = TX( item.getX() );
		double y = TY( item.getY() );
		
		double dx=x-previousX;
		double dy=y-previousY;
		
		return dx*dx+dy*dy;
	}
	
	double distanceSquared(double x,double y,double previousX,double previousY) {
		double dx=x-previousX;
		double dy=y-previousY;
		
		return dx*dx+dy*dy;
	}
	
	
	protected boolean parseLineElements(NodeList node) {
		try {
		    int pathNodeCount = node.getLength();
		    for( int iPathNode = 0; iPathNode < pathNodeCount; iPathNode++ ) {
				Element element = (Element)node.item( iPathNode );
				double x1=0,y1=0;
				double x2=0,y2=0;
				
				if(element.hasAttribute("x1")) x1 = Double.parseDouble(element.getAttribute("x1"));
				if(element.hasAttribute("y1")) y1 = Double.parseDouble(element.getAttribute("y1"));
				if(element.hasAttribute("x2")) x2 = Double.parseDouble(element.getAttribute("x2"));
				if(element.hasAttribute("y2")) y2 = Double.parseDouble(element.getAttribute("y2"));;
				turtle.jumpTo(TX(x1),TY(y1));
				turtle.moveTo(TX(x2),TY(y2));
		    }
		} catch(Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * Draw rectangles that may have rounded corners.
	 * given corners
	 *    x0 x1 x2 x3
	 * y0    a  b
	 * y1 c  i  j  d
	 * y2 e  m  k  f
	 * y3    g  h
	 * draw a-b-d-f-h-g-e-c-a.
	 * 
	 * See https://developer.mozilla.org/en-US/docs/Web/SVG/Element/rect
	 * @param node
	 * @return
	 */
	protected boolean parseRectElements(NodeList node) {
		try {
		    int pathNodeCount = node.getLength();
		    for( int iPathNode = 0; iPathNode < pathNodeCount; iPathNode++ ) {
				Element element = (Element)node.item( iPathNode );
				double x=0,y=0;
				double rx=0,ry=0;
				
				if(element.hasAttribute("x")) x = Double.parseDouble(element.getAttribute("x"));
				if(element.hasAttribute("y")) y = Double.parseDouble(element.getAttribute("y"));
				
				if(element.hasAttribute("rx")) {
					rx = Double.parseDouble(element.getAttribute("rx"));
					if(element.hasAttribute("ry")) {
						ry = Double.parseDouble(element.getAttribute("ry"));
					} else {
						// ry defaults to rx if specified
						ry = rx;
					}
				} else if(element.hasAttribute("ry")) {
					// rx defaults to ry if specified
					rx = ry = Double.parseDouble(element.getAttribute("ry"));
					
				}
				double w = Double.parseDouble(element.getAttribute("width"));
				double h = Double.parseDouble(element.getAttribute("height"));
				
				//double x0=x;
				double x1=x+rx;
				double x2=x+w-rx;
				//double x3=x+w;
				double y0=y;
				double y1=y+ry;
				double y2=y+h-ry;
				//double y3=y+h;

				turtle.jumpTo(TX(x1),TY(y0));
				arcTurtle(turtle, x2,y1, rx,ry, Math.PI * -0.5,Math.PI *  0.0); 
				arcTurtle(turtle, x2,y2, rx,ry, Math.PI *  0.0,Math.PI *  0.5);
				arcTurtle(turtle, x1,y2, rx,ry, Math.PI * -1.5,Math.PI * -1.0);
				arcTurtle(turtle, x1,y1, rx,ry, Math.PI * -1.0,Math.PI * -0.5);
		    }
		} catch(Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * 
	 * @param turtle 
	 * @param cx center position
	 * @param cy center position
	 * @param rx radius on X
	 * @param ry radius on Y
	 * @param p0 radian start angle.
	 * @param p1 radian end angle.
	 */
	protected void arcTurtle(Turtle turtle,double cx,double cy,double rx,double ry,double p0,double p1) {
		double steps=1;
		if(rx>0 && ry>0) {
			double r = rx>ry?rx:ry;
			double circ = Math.PI*r*2.0;  // radius to circumference 
			steps = Math.ceil(circ/4.0);  // 1/4 circumference
			steps = Math.max(steps,1);
		}
		steps = steps/4;
		for(double p = 0;p<=steps;++p) {
			double pFraction = ((p1-p0)*(p/steps) + p0);
			double c = Math.cos(pFraction) * rx;
			double s = Math.sin(pFraction) * ry;
			turtle.moveTo(TX(cx+c), TY(cy+s));
		}
	}
	
	protected boolean parseCircleElements(NodeList node) {
		try {
		    int pathNodeCount = node.getLength();
		    for( int iPathNode = 0; iPathNode < pathNodeCount; iPathNode++ ) {
				Element element = (Element)node.item( iPathNode );
				double cx=0,cy=0,r=0;
				if(element.hasAttribute("cx")) cx = Double.parseDouble(element.getAttribute("cx"));
				if(element.hasAttribute("cy")) cy = Double.parseDouble(element.getAttribute("cy"));
				if(element.hasAttribute("r" )) r  = Double.parseDouble(element.getAttribute("r"));
				turtle.jumpTo(TX(cx+r),TY(cy));
				for(double i=1;i<40;++i) {  // hard coded 40?  gross!
					double v = (Math.PI*2.0) * (i/40.0);
					double s=r*Math.sin(v);
					double c=r*Math.cos(v);
					turtle.moveTo(TX(cx+c),TY(cy+s));
				}
				turtle.moveTo(TX(cx+r),TY(cy));
		    }
		} catch(Exception e) {
			return false;
		}
		return true;
	}

	protected boolean parseEllipseElements(NodeList node) {
		try {
		    int pathNodeCount = node.getLength();
		    for( int iPathNode = 0; iPathNode < pathNodeCount; iPathNode++ ) {
				Element element = (Element)node.item( iPathNode );
				double cx=0,cy=0,rx=0,ry=0;
				if(element.hasAttribute("cx")) cx = Double.parseDouble(element.getAttribute("cx"));
				if(element.hasAttribute("cy")) cy = Double.parseDouble(element.getAttribute("cy"));
				if(element.hasAttribute("rx")) rx = Double.parseDouble(element.getAttribute("rx"));
				if(element.hasAttribute("ry")) ry = Double.parseDouble(element.getAttribute("ry"));
				turtle.jumpTo(TX(cx+rx),TY(cy));
				for(double i=1;i<40;++i) {  // hard coded 40?  gross!
					double v = (Math.PI*2.0) * (i/40.0);
					double s=ry*Math.sin(v);
					double c=rx*Math.cos(v);
					turtle.moveTo(TX(cx+c),TY(cy+s));
				}
				turtle.moveTo(TX(cx+rx),TY(cy));
		    }
		} catch(Exception e) {
			return false;
		}
		return true;
	}
	
	/**
	 * Parse through all the SVG path elements and raster them to gcode.
	 * @param pathNodes the source of the elements
	 */
	protected boolean parsePathElements(NodeList pathNodes) {
	    boolean loadOK=true;

	    double x=turtle.getX();
	    double y=turtle.getY();
		double firstX=0;
		double firstY=0;
		
	    int pathNodeCount = pathNodes.getLength();
	    for( int iPathNode = 0; iPathNode < pathNodeCount; iPathNode++ ) {
	    	if(pathNodes.item( iPathNode ).getClass() == SVGOMPolylineElement.class) {
	    		Log.message("Node is a polyline.");
	    		parsePolylineElements(pathNodes);
	    		continue;
	    	}
	    	
	    	SVGOMPathElement pathElement = ((SVGOMPathElement)pathNodes.item( iPathNode ));
	    	SVGPathSegList pathList = pathElement.getNormalizedPathSegList();
	    	int pathObjects = pathList.getNumberOfItems();
			//Log.message("Node has "+pathObjects+" elements.");

			for (int i = 0; i < pathObjects; i++) {
				SVGPathSeg item = (SVGPathSeg) pathList.getItem(i);
				switch( item.getPathSegType() ) {
				case SVGPathSeg.PATHSEG_CLOSEPATH:  // z
					{
						//Log.message("Close path");
						turtle.moveTo(firstX,firstY);
					}
					break;
				case SVGPathSeg.PATHSEG_MOVETO_ABS:  // m
					{
						//Log.message("Move Abs");
						SVGPathSegMovetoAbs path = (SVGPathSegMovetoAbs)item;
						firstX = x = TX( path.getX() );
						firstY = y = TY( path.getY() );
						turtle.jumpTo(firstX,firstY);
					}
					break;
				case SVGPathSeg.PATHSEG_MOVETO_REL:  // M
					{
						//Log.message("Move Rel");
						SVGPathSegMovetoAbs path = (SVGPathSegMovetoAbs)item;
						firstX = x = TX( path.getX() ) + turtle.getX();
						firstY = y = TY( path.getY() ) + turtle.getY();
						turtle.jumpTo(firstX,firstY);
					}
					break;
				case SVGPathSeg.PATHSEG_LINETO_ABS:  // l
					{
						//Log.message("Line Abs");
						SVGPathSegLinetoAbs path = (SVGPathSegLinetoAbs)item;
						x = TX( path.getX() );
						y = TY( path.getY() );
						turtle.moveTo(x,y);
					}
					break;
				case SVGPathSeg.PATHSEG_LINETO_REL:  // L
					{
						//Log.message("Line REL");
						SVGPathSegLinetoAbs path = (SVGPathSegLinetoAbs)item;
						x = TX( path.getX() ) + turtle.getX();
						y = TY( path.getY() ) + turtle.getY();
						turtle.moveTo(x,y);
					}
					break;
				case SVGPathSeg.PATHSEG_CURVETO_CUBIC_ABS: // c
					{
						//Log.message("Curve Cubic Abs");
						SVGPathSegCurvetoCubicAbs path = (SVGPathSegCurvetoCubicAbs)item;
						
						// x0,y0 is the first point
						double x0=x;
						double y0=y;
						// x1,y1 is the second control point
						double x1=TX( path.getX1());
						double y1=TY( path.getY1());
						// x2,y2 is the third control point
						double x2=TX( path.getX2());
						double y2=TY( path.getY2());
						// x3,y3 is the fourth control point
						double x3=TX( path.getX());
						double y3=TY( path.getY());
						/*
						double d0 = distanceSquared(x0,y0,x,y);
						double dN = distanceSquared(x3,y3,x,y);
						
						if(dN>d0) {
							// the far end of the curve is closer to the current pen position.
							// flip the curve.
							double t;
							
							t = x3; x3=x0; x0=t;
							t = x2; x2=x1; x1=t;
							
							t = y3; y3=y0; y0=t;
							t = y2; y2=y1; y1=t;
						}*/

						double length=0;
						double oldx=x;
						double oldy=y;
						for(double j=0;j<=1;j+=0.1) {
					        double a = Math.pow((1.0 - j), 3.0);
					        double b = 3.0 * j * Math.pow((1.0 - j), 2.0);
					        double c = 3.0 * Math.pow(j, 2.0) * (1.0 - j);
					        double d = Math.pow(j, 3.0);
					 
					        double xabc = a * x0 + b * x1 + c * x2 + d * x3;
					        double yabc = a * y0 + b * y1 + c * y2 + d * y3;
					        
					        length += Math.sqrt( Math.pow(xabc-oldx, 2) + Math.pow(yabc-oldy,2) );
					        oldx=xabc;
					        oldy=yabc;
						}
						
						double steps = (int)Math.ceil(Math.max(Math.min(length, 10),1));

						
						for(double j=0;j<1;j+=1.0/steps) {/*
							// old method
							double xa = p(x0,x1,j);
							double ya = p(y0,y1,j);
							double xb = p(x1,x2,j);
							double yb = p(y1,y2,j);
							double xc = p(x2,x3,j);
							double yc = p(y2,y3,j);
							
							double xab = p(xa,xb,j);
							double yab = p(ya,yb,j);
							double xbc = p(xb,xc,j);
							double ybc = p(yb,yc,j);
							
							xabc = p(xab,xbc,j);
							yabc = p(yab,ybc,j);/*/
					        double a = Math.pow((1.0 - j), 3.0);
					        double b = 3.0 * j * Math.pow((1.0 - j), 2.0);
					        double c = 3.0 * Math.pow(j, 2.0) * (1.0 - j);
					        double d = Math.pow(j, 3.0);
					 
					        double xabc = a * x0 + b * x1 + c * x2 + d * x3;
					        double yabc = a * y0 + b * y1 + c * y2 + d * y3;//*/
							
							//if(j<1 && distanceSquared(xabc,yabc,x,y)>toolMinimumStepSize*toolMinimumStepSize) {
								x=xabc;
								y=yabc;
								turtle.moveTo(xabc,yabc);
							//}
						}
						turtle.moveTo(x3,y3);
					}
					break; 
				default:
					Log.message("Found unexpected SVG Path type "+item.getPathSegTypeAsLetter()
						+" "+item.getPathSegType()+" = "+((SVGItem)item).getValueAsString());
					loadOK=false;
					break;
				}
			}
		}
	    return loadOK;
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
		Log.message("saving...");
		turtle = robot.getTurtle();

		machine = robot.getSettings();
		double left = machine.getPaperLeft();
		double right = machine.getPaperRight();
		double top = machine.getPaperTop();
		double bottom = machine.getPaperBottom();
		
		try(OutputStreamWriter out = new OutputStreamWriter(outputStream)) {
			// header
			out.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n");
			out.write("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n");
			out.write("<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" viewBox=\""+left+" "+bottom+" "+(right-left)+" "+(top-bottom)+"\">\n");

			boolean isUp=true;
			double x0 = robot.getSettings().getHomeX();
			double y0 = robot.getSettings().getHomeY();

			for( Turtle.Movement m : turtle.history ) {
				switch(m.type) {
				case TRAVEL:
					if(!isUp) {
						isUp=true;
					}
					x0=m.x;
					y0=m.y;
					break;
				case DRAW:
					if(isUp) {
						isUp=false;
					} else {
						out.write("  <line");
						out.write(" x1=\""+StringHelper.formatDouble(x0)+"\"");
						out.write(" y1=\""+StringHelper.formatDouble(-y0)+"\"");
						out.write(" x2=\""+StringHelper.formatDouble(m.x)+"\"");
						out.write(" y2=\""+StringHelper.formatDouble(-m.y)+"\"");
						out.write(" stroke=\"black\"");
						//out.write(" stroke-width=\"1\"");
						out.write(" />\n");
					}
					x0=m.x;
					y0=m.y;
					
					break;
				case TOOL_CHANGE:
					break;
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
		
		Log.message("done.");
		return true;
	}
}
