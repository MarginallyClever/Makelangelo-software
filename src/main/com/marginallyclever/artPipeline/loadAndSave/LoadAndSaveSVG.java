package com.marginallyclever.artPipeline.loadAndSave;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

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
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.convenience.turtle.Turtle;
import com.marginallyclever.convenience.turtle.TurtleMove;
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

		imageWidth *=1.02;
		imageHeight*=1.02;

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
	    	SVGPointShapeElement element = (SVGPointShapeElement)pathNodes.item( iPathNode );
			if(isElementStrokeNone(element)) 
				continue;
	    	
	    	SVGPointList pointList = element.getAnimatedPoints();
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
				if(isElementStrokeNone(element)) 
					continue;
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

	private boolean isElementStrokeNone(Element element) {
		if(element.hasAttribute("style")) {
			// we have a style
			String style = element.getAttribute("style");
			// stripe whitespace and smash to lower case
			style = style.toLowerCase().replace(" ","");
			// does the style contain "stroke:"?
			String strokeLabelName="stroke:";
			if(style.contains(strokeLabelName)) {
				// is the stroke none?
				int k = style.indexOf(strokeLabelName);
				String strokeStyleName = style.substring(k+strokeLabelName.length());
				if(strokeStyleName.startsWith("none"))
					// it is!  bail.
					return true;
			}
		}
		return false;
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
				if(isElementStrokeNone(element)) 
					continue;
				
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
				if(isElementStrokeNone(element)) 
					continue;
				double cx=0,cy=0,r=0;
				if(element.hasAttribute("cx")) cx = Double.parseDouble(element.getAttribute("cx"));
				if(element.hasAttribute("cy")) cy = Double.parseDouble(element.getAttribute("cy"));
				if(element.hasAttribute("r" )) r  = Double.parseDouble(element.getAttribute("r"));
				turtle.jumpTo(TX(cx+r),TY(cy));
				double circ = Math.min(3,Math.floor(Math.PI * r*r)); 
				for(double i=1;i<circ;++i) {
					double v = (Math.PI*2.0) * (i/circ);
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
				if(isElementStrokeNone(element)) 
					continue;
				double cx=0,cy=0,rx=0,ry=0;
				if(element.hasAttribute("cx")) cx = Double.parseDouble(element.getAttribute("cx"));
				if(element.hasAttribute("cy")) cy = Double.parseDouble(element.getAttribute("cy"));
				if(element.hasAttribute("rx")) rx = Double.parseDouble(element.getAttribute("rx"));
				if(element.hasAttribute("ry")) ry = Double.parseDouble(element.getAttribute("ry"));
				turtle.jumpTo(TX(cx+rx),TY(cy));
				double circ = Math.min(3,Math.floor(Math.PI * ry*rx)); 
				for(double i=1;i<circ;++i) {
					double v = (Math.PI*2.0) * (i/circ);
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
	 * @param paths the source of the elements
	 */
	protected boolean parsePathElements(NodeList paths) {
	    boolean loadOK=true;

	    double x=turtle.getX();
	    double y=turtle.getY();
		double firstX=0;
		double firstY=0;
		
	    int pathCount = paths.getLength();
	    for( int iPath = 0; iPath < pathCount; iPath++ ) {
	    	if(paths.item( iPath ).getClass() == SVGOMPolylineElement.class) {
	    		Log.message("Node is a polyline.");
	    		parsePolylineElements(paths);
	    		continue;
	    	}
	    	SVGOMPathElement element = ((SVGOMPathElement)paths.item( iPath ));
			if(isElementStrokeNone(element)) 
				continue;
	    	
	    	SVGPathSegList pathList = element.getNormalizedPathSegList();
	    	int pathObjects = pathList.getNumberOfItems();
			//Log.message("Node has "+pathObjects+" elements.");

			for (int i = 0; i < pathObjects; i++) {
				SVGPathSeg item = (SVGPathSeg) pathList.getItem(i);
				switch( item.getPathSegType() ) {
				case SVGPathSeg.PATHSEG_CLOSEPATH:  // z
					{
						System.out.println("Close path");
						turtle.moveTo(firstX,firstY);
					}
					break;
				case SVGPathSeg.PATHSEG_MOVETO_ABS:  // m
					{
						System.out.println("Move Abs");
						SVGPathSegMovetoAbs path = (SVGPathSegMovetoAbs)item;
						firstX = x = TX( path.getX() );
						firstY = y = TY( path.getY() );
						turtle.jumpTo(firstX,firstY);
					}
					break;
				case SVGPathSeg.PATHSEG_MOVETO_REL:  // M
					{
						System.out.println("Move Rel");
						SVGPathSegMovetoAbs path = (SVGPathSegMovetoAbs)item;
						firstX = x = TX( path.getX() ) + turtle.getX();
						firstY = y = TY( path.getY() ) + turtle.getY();
						turtle.jumpTo(firstX,firstY);
					}
					break;
				case SVGPathSeg.PATHSEG_LINETO_ABS:  // l
					{
						System.out.println("Line Abs");
						SVGPathSegLinetoAbs path = (SVGPathSegLinetoAbs)item;
						x = TX( path.getX() );
						y = TY( path.getY() );
						turtle.moveTo(x,y);
					}
					break;
				case SVGPathSeg.PATHSEG_LINETO_REL:  // L
					{
						System.out.println("Line REL");
						SVGPathSegLinetoAbs path = (SVGPathSegLinetoAbs)item;
						x = TX( path.getX() ) + turtle.getX();
						y = TY( path.getY() ) + turtle.getY();
						turtle.moveTo(x,y);
					}
					break;
				case SVGPathSeg.PATHSEG_CURVETO_CUBIC_ABS: // c
					{
						System.out.println("Curve Cubic Abs");
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

						ArrayList<Point2D> points = generateCurvePointsB(x0,y0,x1,y1,x2,y2,x3,y3);
						for(Point2D p : points) {
							turtle.moveTo(p.x, p.y);
							x = p.x;
							y = p.y;
						}
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

	protected int RECURSION_LIMIT = 8;
	protected double curve_angle_tolerance_epsilon =0.01;
	protected double m_angle_tolerance = 0;
	protected double m_cusp_limit= 0;
	protected double FLT_EPSILON=1.19209290e-7;
    
	// Based on https://github.com/pelson/antigrain/blob/master/agg-2.4/src/agg_curves.cpp
	// and https://github.com/mattdesl/adaptive-bezier-curve
	protected ArrayList<Point2D> generateCurvePointsB(double x1,double y1,double x2,double y2,double x3,double y3,double x4,double y4) {
		RECURSION_LIMIT = 8;
		curve_angle_tolerance_epsilon =0.01;
		m_angle_tolerance = 0;
		m_cusp_limit= 0;
		FLT_EPSILON=1.19209290e-7;
		
		double distanceTolerance = 0.1;
		ArrayList<Point2D> points = new ArrayList<Point2D>();
		points.add(new Point2D(x1,y1));
		recursive(x1,y1,x2,y2,x3,y3,x4,y4,points,distanceTolerance*distanceTolerance,0);
		points.add(new Point2D(x4,y4));
		return points;
	}
	
	protected void recursive(double x1,double y1,double x2,double y2,double x3,double y3,double x4,double y4,ArrayList<Point2D> points, double distanceTolerance,int level) {
        if(level > RECURSION_LIMIT) 
            return;

        // Calculate all the mid-points of the line segments
        double x12   = (x1 + x2) / 2.0;
        double y12   = (y1 + y2) / 2.0;
        double x23   = (x2 + x3) / 2.0;
        double y23   = (y2 + y3) / 2.0;
        double x34   = (x3 + x4) / 2.0;
        double y34   = (y3 + y4) / 2.0;
        double x123  = (x12 + x23) / 2.0;
        double y123  = (y12 + y23) / 2.0;
        double x234  = (x23 + x34) / 2.0;
        double y234  = (y23 + y34) / 2.0;
        double x1234 = (x123 + x234) / 2.0;
        double y1234 = (y123 + y234) / 2.0;

        if(level > 0) { // Enforce subdivision first time
            // Try to approximate the full cubic curve by a single straight line
            double dx = x4-x1;
            double dy = y4-y1;

            double d2 = Math.abs((x2 - x4) * dy - (y2 - y4) * dx);
            double d3 = Math.abs((x3 - x4) * dy - (y3 - y4) * dx);

            double da1, da2;

            if(d2 > FLT_EPSILON && d3 > FLT_EPSILON) {
                // Regular care
                if((d2 + d3)*(d2 + d3) <= distanceTolerance * (dx*dx + dy*dy)) {
                    // If the curvature doesn't exceed the distanceTolerance value we tend to finish subdivisions.
                    if(m_angle_tolerance < curve_angle_tolerance_epsilon) {
                        points.add(new Point2D(x1234, y1234));
                        return;
                    }

                    // Angle & Cusp Condition
                    double a23 = Math.atan2(y3 - y2, x3 - x2);
                    da1 = Math.abs(a23 - Math.atan2(y2 - y1, x2 - x1));
                    da2 = Math.abs(Math.atan2(y4 - y3, x4 - x3) - a23);
                    if(da1 >= Math.PI) da1 = 2.0*Math.PI - da1;
                    if(da2 >= Math.PI) da2 = 2.0*Math.PI - da2;

                    if(da1 + da2 < m_angle_tolerance) {
                        // Finally we can stop the recursion
                        points.add(new Point2D(x1234, y1234));
                        return;
                    }

                    if(m_cusp_limit != 0.0) {
                        if(da1 > m_cusp_limit) {
                            points.add(new Point2D(x2, y2));
                            return;
                        }

                        if(da2 > m_cusp_limit) {
                            points.add(new Point2D(x3, y3));
                            return;
                        }
                    }
                }
            }
            else {
                if(d2 > FLT_EPSILON) {
                    // p1,p3,p4 are co-linear, p2 is considerable
                    if(d2 * d2 <= distanceTolerance * (dx*dx + dy*dy)) {
                        if(m_angle_tolerance < curve_angle_tolerance_epsilon) {
                            points.add(new Point2D(x1234, y1234));
                            return;
                        }

                        // Angle Condition
                        da1 = Math.abs(Math.atan2(y3 - y2, x3 - x2) - Math.atan2(y2 - y1, x2 - x1));
                        if(da1 >= Math.PI) da1 = 2.0*Math.PI - da1;

                        if(da1 < m_angle_tolerance) {
                            points.add(new Point2D(x2, y2));
                            points.add(new Point2D(x3, y3));
                            return;
                        }

                        if(m_cusp_limit != 0.0) {
                            if(da1 > m_cusp_limit) {
                                points.add(new Point2D(x2, y2));
                                return;
                            }
                        }
                    }
                }
                else if(d3 > FLT_EPSILON) {
                    // p1,p2,p4 are collinear, p3 is considerable
                    //----------------------
                    if(d3 * d3 <= distanceTolerance * (dx*dx + dy*dy)) {
                        if(m_angle_tolerance < curve_angle_tolerance_epsilon) {
                            points.add(new Point2D(x1234, y1234));
                            return;
                        }

                        // Angle Condition
                        //----------------------
                        da1 = Math.abs(Math.atan2(y4 - y3, x4 - x3) - Math.atan2(y3 - y2, x3 - x2));
                        if(da1 >= Math.PI) da1 = 2.0*Math.PI - da1;

                        if(da1 < m_angle_tolerance) {
                            points.add(new Point2D(x2, y2));
                            points.add(new Point2D(x3, y3));
                            return;
                        }

                        if(m_cusp_limit != 0.0) {
                            if(da1 > m_cusp_limit) {
                                points.add(new Point2D(x3, y3));
                                return;
                            }
                        }
                    }
                }
                else {
                    // Collinear case
                    //-----------------
                    dx = x1234 - (x1 + x4) / 2.0;
                    dy = y1234 - (y1 + y4) / 2.0;
                    if(dx*dx + dy*dy <= distanceTolerance) {
                        points.add(new Point2D(x1234, y1234));
                        return;
                    }
                }
            }
        }

        // Continue subdivision
        //----------------------
        recursive(x1, y1, x12, y12, x123, y123, x1234, y1234, points, distanceTolerance, level + 1);
        recursive(x1234, y1234, x234, y234, x34, y34, x4, y4, points, distanceTolerance, level + 1);
	}
	
	protected ArrayList<Point2D> generateCurvePointsA(double x0,double y0,double x1,double y1,double x2,double y2,double x3,double y3) {
		ArrayList<Point2D> list = new ArrayList<Point2D>();
		list.add(new Point2D(x0,y0));
		
		double steps=25;
		for(double k=1;k<steps;k++) {
			double j = k/steps;
			/*
			// old method
			double xa = lerp(x0,x1,j);
			double ya = lerp(y0,y1,j);
			double xb = lerp(x1,x2,j);
			double yb = lerp(y1,y2,j);
			double xc = lerp(x2,x3,j);
			double yc = lerp(y2,y3,j);
			
			double xab = lerp(xa,xb,j);
			double yab = lerp(ya,yb,j);
			double xbc = lerp(xb,xc,j);
			double ybc = lerp(yb,yc,j);
			
			xabc = lerp(xab,xbc,j);
			yabc = lerp(yab,ybc,j);/*/
	        double a = Math.pow((1.0 - j), 3.0);
	        double b = 3.0 * j * Math.pow((1.0 - j), 2.0);
	        double c = 3.0 * Math.pow(j, 2.0) * (1.0 - j);
	        double d = Math.pow(j, 3.0);
	 
	        double xabc = a * x0 + b * x1 + c * x2 + d * x3;
	        double yabc = a * y0 + b * y1 + c * y2 + d * y3;//*/
			
	        list.add(new Point2D(xabc,yabc));
		}
		list.add(new Point2D(x3,y3));
		
		return list;
	}

	protected double lerp(double a,double b,double fraction) {
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

			for( TurtleMove m : turtle.history ) {
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
