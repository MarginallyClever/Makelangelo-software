package com.marginallyclever.makelangelo.makeArt.io.vector;

import java.awt.geom.Rectangle2D;
import java.io.InputStream;
import java.util.ArrayList;

import javax.swing.filechooser.FileNameExtensionFilter;
import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.anim.dom.SVGGraphicsElement;
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
import org.w3c.dom.svg.SVGMatrix;
import org.w3c.dom.svg.SVGPathSeg;
import org.w3c.dom.svg.SVGPathSegCurvetoCubicAbs;
import org.w3c.dom.svg.SVGPathSegLinetoAbs;
import org.w3c.dom.svg.SVGPathSegList;
import org.w3c.dom.svg.SVGPathSegMovetoAbs;
import org.w3c.dom.svg.SVGPoint;
import org.w3c.dom.svg.SVGPointList;

import com.marginallyclever.convenience.Bezier;
import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.convenience.turtle.Turtle;
import com.marginallyclever.makelangelo.makeArt.ImageManipulator;

/**
 * @author Dan Royer
 * See https://www.w3.org/TR/SVG/paths.html
 */
public class LoadSVG extends ImageManipulator implements TurtleLoader {
	private static FileNameExtensionFilter filter = new FileNameExtensionFilter("Scaleable Vector Graphics 1.1", "svg");
		
	@Override
	public String getName() { return "SVG"; }
	
	@Override
	public FileNameExtensionFilter getFileNameFilter() {
		return filter;
	}

	@Override
	public boolean canLoad(String filename) {
		String ext = filename.substring(filename.lastIndexOf('.'));
		return ext.equalsIgnoreCase(".svg");
	}

	@Override
	public Turtle load(InputStream in) throws Exception {
		Log.message("Loading...");
		
		Document document = newDocumentFromInputStream(in);
		initSVGDOM(document);
		
	    turtle = new Turtle();
		turtle.setColor(new ColorRGB(0,0,0));
		try {
			parseAll(document);
		} catch(Exception e) {
			e.printStackTrace();
			throw new Exception("Failed to load some elements (1): "+e.getLocalizedMessage());
		}
		
		Rectangle2D.Double r = turtle.getBounds();
		turtle.translate(-r.width/2,-r.height/2);
		turtle.scale(1, -1);
		
		return turtle;
	}
	
	private void parseAll(Document document) throws Exception {
		SVGOMSVGElement documentElement = (SVGOMSVGElement)document.getDocumentElement();

		parsePathElements(    documentElement.getElementsByTagName( "path"     ));
		parsePolylineElements(documentElement.getElementsByTagName( "polyline" ));
		parsePolylineElements(documentElement.getElementsByTagName( "polygon"  ));
		parseLineElements(    documentElement.getElementsByTagName( "line"     ));
		parseRectElements(    documentElement.getElementsByTagName( "rect"     ));
		
		parseCircleElements(  documentElement.getElementsByTagName( "circle"   ));
		parseEllipseElements( documentElement.getElementsByTagName( "ellipse"  ));
	}
	
	/**
	 * Parse through all the SVG polyline elements and raster them to gcode.
	 * @param pathNodes the source of the elements
	 */
	private void parsePolylineElements(NodeList pathNodes) throws Exception {
		Vector3d v2;
		
	    int pathNodeCount = pathNodes.getLength();
	    for( int iPathNode = 0; iPathNode < pathNodeCount; iPathNode++ ) {
	    	SVGPointShapeElement element = (SVGPointShapeElement)pathNodes.item( iPathNode );
			if(isElementStrokeNone(element)) 
				continue;

			Matrix3d m = getMatrixFromElement(element);

	    	SVGPointList pointList = element.getAnimatedPoints();
	    	int numPoints = pointList.getNumberOfItems();
			//Log.message("New Node has "+pathObjects+" elements.");

			SVGPoint item = (SVGPoint)pointList.getItem(0);
			v2 = transform(item.getX(),item.getY(),m);
			turtle.jumpTo(v2.x,v2.y);
			
			for( int i=1; i<numPoints; ++i ) {
				item = (SVGPoint)pointList.getItem(i);
				v2 = transform(item.getX(),item.getY(),m);
				turtle.moveTo(v2.x,v2.y);
			}
		}
	}
	
	private void parseLineElements(NodeList node) throws Exception {
		Vector3d v2;
		
	    int pathNodeCount = node.getLength();
	    for( int iPathNode = 0; iPathNode < pathNodeCount; iPathNode++ ) {
			Element element = (Element)node.item( iPathNode );
			if(isElementStrokeNone(element)) 
				continue;
			
			Matrix3d m = getMatrixFromElement(element);

			double x1=0,y1=0;
			double x2=0,y2=0;
			
			if(element.hasAttribute("x1")) x1 = Double.parseDouble(element.getAttribute("x1"));
			if(element.hasAttribute("y1")) y1 = Double.parseDouble(element.getAttribute("y1"));
			if(element.hasAttribute("x2")) x2 = Double.parseDouble(element.getAttribute("x2"));
			if(element.hasAttribute("y2")) y2 = Double.parseDouble(element.getAttribute("y2"));
			v2 = transform(x1,y1,m);
			turtle.jumpTo(v2.x,v2.y);
			v2 = transform(x2,y2,m);
			turtle.moveTo(v2.x,v2.y);
	    }
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
				if(strokeStyleName.contentEquals("none") || strokeStyleName.contentEquals("white") )
					// it is!  bail.
					return true;
			} else {
				// default SVG stroke is "none", which isn't even transparent - it's nothing!
				return false;
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
	 */
	private void parseRectElements(NodeList node) throws Exception {
	    int pathNodeCount = node.getLength();
	    for( int iPathNode = 0; iPathNode < pathNodeCount; iPathNode++ ) {
			Element element = (Element)node.item( iPathNode );
			if(isElementStrokeNone(element)) 
				continue;

			Matrix3d m = getMatrixFromElement(element);
			
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

			Vector3d v2 = transform(x1,y0,m);
			turtle.jumpTo(v2.x,v2.y);
			arcTurtle(turtle, x2,y1, rx,ry, Math.PI * -0.5,Math.PI *  0.0,m); 
			arcTurtle(turtle, x2,y2, rx,ry, Math.PI *  0.0,Math.PI *  0.5,m);
			arcTurtle(turtle, x1,y2, rx,ry, Math.PI * -1.5,Math.PI * -1.0,m);
			arcTurtle(turtle, x1,y1, rx,ry, Math.PI * -1.0,Math.PI * -0.5,m);
	    }
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
	private void arcTurtle(Turtle turtle,double cx,double cy,double rx,double ry,double p0,double p1,Matrix3d m) {
		Vector3d v2;
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
			v2 = transform(cx+c,cy+s,m);
			turtle.moveTo(v2.x,v2.y);
		}
	}
	
	private void parseCircleElements(NodeList node) throws Exception {
		Vector3d v2;

	    int pathNodeCount = node.getLength();
	    for( int iPathNode = 0; iPathNode < pathNodeCount; iPathNode++ ) {
			Element element = (Element)node.item( iPathNode );
			if(isElementStrokeNone(element)) 
				continue;
			
			Matrix3d m = getMatrixFromElement(element);
			
			double cx=0,cy=0,r=0;
			if(element.hasAttribute("cx")) cx = Double.parseDouble(element.getAttribute("cx"));
			if(element.hasAttribute("cy")) cy = Double.parseDouble(element.getAttribute("cy"));
			if(element.hasAttribute("r" )) r  = Double.parseDouble(element.getAttribute("r"));
			v2 = transform(cx+r,cy,m);
			turtle.jumpTo(v2.x,v2.y);
			
			double circ = Math.min(3,Math.floor(Math.PI * r*r)); 
			for(double i=1;i<circ;++i) {
				double v = (Math.PI*2.0) * (i/circ);
				double s=r*Math.sin(v);
				double c=r*Math.cos(v);
				v2 = transform(cx+c,cy+s,m);
				turtle.moveTo(v2.x,v2.y);
			}
			v2 = transform(cx+r,cy,m);
			turtle.moveTo(v2.x,v2.y);
	    }
	}

	private void parseEllipseElements(NodeList node) {
		Vector3d v2;
		
	    int pathNodeCount = node.getLength();
	    for( int iPathNode = 0; iPathNode < pathNodeCount; iPathNode++ ) {
			Element element = (Element)node.item( iPathNode );
			if(isElementStrokeNone(element)) 
				continue;
			
			Matrix3d m = getMatrixFromElement(element);
			
			double cx=0,cy=0,rx=0,ry=0;
			if(element.hasAttribute("cx")) cx = Double.parseDouble(element.getAttribute("cx"));
			if(element.hasAttribute("cy")) cy = Double.parseDouble(element.getAttribute("cy"));
			if(element.hasAttribute("rx")) rx = Double.parseDouble(element.getAttribute("rx"));
			if(element.hasAttribute("ry")) ry = Double.parseDouble(element.getAttribute("ry"));
			v2 = transform(cx+rx,cy,m);
			turtle.jumpTo(v2.x,v2.y);
			
			double circ = Math.min(3,Math.floor(Math.PI * ry*rx)); 
			for(double i=1;i<circ;++i) {
				double v = (Math.PI*2.0) * (i/circ);
				double s=ry*Math.sin(v);
				double c=rx*Math.cos(v);
				v2 = transform(cx+c,cy+s,m);
				turtle.moveTo(v2.x,v2.y);
			}
			v2 = transform(cx+rx,cy,m);
			turtle.moveTo(v2.x,v2.y);
	    }
	}
	
	/**
	 * Parse through all the SVG path elements and raster them to gcode.
	 * @param paths the source of the elements
	 */
	private void parsePathElements(NodeList paths) throws Exception {
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
			
			Matrix3d m = getMatrixFromElement(element);
			Vector3d v;
			
	    	SVGPathSegList pathList = element.getNormalizedPathSegList();
	    	int pathObjects = pathList.getNumberOfItems();
			//Log.message("Node has "+pathObjects+" elements.");

			for (int i = 0; i < pathObjects; i++) {
				SVGPathSeg item = (SVGPathSeg) pathList.getItem(i);
				switch( item.getPathSegType() ) {
				case SVGPathSeg.PATHSEG_CLOSEPATH:  // z
					{
						//System.out.println("Close path");
						turtle.moveTo(firstX,firstY);
					}
					break;
				case SVGPathSeg.PATHSEG_MOVETO_ABS:  // m
					{
						//System.out.println("Move Abs");
						SVGPathSegMovetoAbs path = (SVGPathSegMovetoAbs)item;
						v = transform(path.getX(),path.getY(),m);
						firstX = x = v.x;
						firstY = y = v.y;
						turtle.jumpTo(firstX,firstY);
					}
					break;
				case SVGPathSeg.PATHSEG_MOVETO_REL:  // M
					{
						//System.out.println("Move Rel");
						SVGPathSegMovetoAbs path = (SVGPathSegMovetoAbs)item;
						v = transform(path.getX(),path.getY(),m);
						firstX = x = v.x + turtle.getX();
						firstY = y = v.y + turtle.getY();
						turtle.jumpTo(firstX,firstY);
					}
					break;
				case SVGPathSeg.PATHSEG_LINETO_ABS:  // l
					{
						//System.out.println("Line Abs");
						SVGPathSegLinetoAbs path = (SVGPathSegLinetoAbs)item;
						v = transform(path.getX(),path.getY(),m);
						x = v.x;
						y = v.y;
						turtle.moveTo(x,y);
					}
					break;
				case SVGPathSeg.PATHSEG_LINETO_REL:  // L
					{
						//System.out.println("Line REL");
						SVGPathSegLinetoAbs path = (SVGPathSegLinetoAbs)item;
						v = transform(path.getX(),path.getY(),m);
						x = v.x + turtle.getX();
						y = v.y + turtle.getY();
						turtle.moveTo(x,y);
					}
					break;
				case SVGPathSeg.PATHSEG_CURVETO_CUBIC_ABS: // c
					{
						//System.out.println("Curve Cubic Abs");
						SVGPathSegCurvetoCubicAbs path = (SVGPathSegCurvetoCubicAbs)item;

						// x0,y0 is the first point
						double x0=x;
						double y0=y;
						// x1,y1 is the second control point
						v = transform(path.getX1(),path.getY1(),m);
						double x1=v.x;
						double y1=v.y;
						// x2,y2 is the third control point
						v = transform(path.getX2(),path.getY2(),m);
						double x2=v.x;
						double y2=v.y;
						// x3,y3 is the fourth control point
						v = transform(path.getX(),path.getY(),m);
						double x3=v.x;
						double y3=v.y;
						Bezier b = new Bezier(x0,y0,x1,y1,x2,y2,x3,y3);
						ArrayList<Point2D> points = b.generateCurvePoints(0.05);
						for(Point2D p2 : points) {
							turtle.moveTo(p2.x, p2.y);
							x = p2.x;
							y = p2.y;
						}
					}
					break; 
				default:
					throw new Exception("Found unexpected SVG Path type "+item.getPathSegTypeAsLetter()
						+" "+item.getPathSegType()+" = "+((SVGItem)item).getValueAsString());
				}
			}
		}
	}
    
	private Vector3d transform(double x, double y, Matrix3d m) {
		Vector3d p = new Vector3d(x,y,0);
		m.transform(p);
		return p;
	}

	private Matrix3d getMatrixFromElement(Element element) {
		if(!(element instanceof SVGGraphicsElement)) {
			Matrix3d m = new Matrix3d();
			m.setIdentity();
			return m;
		}
		
		Matrix3d m = new Matrix3d();

		try {
			SVGGraphicsElement svgge = (SVGGraphicsElement)element;
			SVGMatrix svgMatrix = svgge.getScreenCTM();
			// [ a c e ]
			// [ b d f ]
			// [ 0 0 1 ]
			m.m00 = svgMatrix.getA();
			m.m01 = svgMatrix.getC();
			m.m02 = svgMatrix.getE();
			m.m10 = svgMatrix.getB();
			m.m11 = svgMatrix.getD();
			m.m12 = svgMatrix.getF();
			m.m20 = 0;
			m.m21 = 0;
			m.m22 = 1;
		}
		catch(Exception e) {
			m.setIdentity();
		}
		return m;
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

	private static SVGDocument newDocumentFromInputStream(InputStream in) throws Exception {
		SVGDocument ret = null;

		String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);
        ret = (SVGDocument) factory.createDocument("",in);

		return ret;
	}
}
