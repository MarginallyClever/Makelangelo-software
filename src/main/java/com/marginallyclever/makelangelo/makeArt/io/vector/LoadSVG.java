package com.marginallyclever.makelangelo.makeArt.io.vector;

import com.marginallyclever.convenience.Bezier;
import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.apache.batik.anim.dom.*;
import org.apache.batik.bridge.*;
import org.apache.batik.dom.svg.SVGItem;
import org.apache.batik.util.XMLResourceDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.*;

import javax.swing.filechooser.FileNameExtensionFilter;
import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;
import java.awt.geom.Rectangle2D;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * @author Dan Royer
 * See https://www.w3.org/TR/SVG/paths.html
 */
public class LoadSVG implements TurtleLoader {
	private static final Logger logger = LoggerFactory.getLogger(LoadSVG.class);
	
	private static final String LABEL_STROKE="stroke:";
	
	private static FileNameExtensionFilter filter = new FileNameExtensionFilter("Scaleable Vector Graphics 1.1", "svg");
	private Turtle myTurtle;
	
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
		logger.debug("Loading...");
		
		Document document = newDocumentFromInputStream(in);
		initSVGDOM(document);
		
	    myTurtle = new Turtle();
		myTurtle.setColor(new ColorRGB(0,0,0));
		parseAll(document);
		
		Rectangle2D.Double r = myTurtle.getBounds();
		myTurtle.translate(-r.width/2,-r.height/2);
		myTurtle.scale(1, -1);
		
		return myTurtle;
	}
	
	private void parseAll(Document document) throws Exception {
		SVGOMSVGElement documentElement = (SVGOMSVGElement)document.getDocumentElement();

		logger.debug("...parse path");
		parsePathElements(    documentElement.getElementsByTagName( "path"     ));
		logger.debug("...parse polylines");
		parsePolylineElements(documentElement.getElementsByTagName( "polyline" ));
		logger.debug("...parse polygons");
		parsePolylineElements(documentElement.getElementsByTagName( "polygon"  ));
		logger.debug("...parse lines");
		parseLineElements(    documentElement.getElementsByTagName( "line"     ));
		logger.debug("...parse rects");
		parseRectElements(    documentElement.getElementsByTagName( "rect"     ));
		logger.debug("...parse circles");
		parseCircleElements(  documentElement.getElementsByTagName( "circle"   ));
		logger.debug("...parse ellipses");
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
			//logger.debug("New Node has "+pathObjects+" elements.");

			SVGPoint item = (SVGPoint)pointList.getItem(0);
			v2 = transform(item.getX(),item.getY(),m);
			myTurtle.jumpTo(v2.x,v2.y);
			
			for( int i=1; i<numPoints; ++i ) {
				item = (SVGPoint)pointList.getItem(i);
				v2 = transform(item.getX(),item.getY(),m);
				myTurtle.moveTo(v2.x,v2.y);
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
			myTurtle.jumpTo(v2.x,v2.y);
			v2 = transform(x2,y2,m);
			myTurtle.moveTo(v2.x,v2.y);
	    }
	}
	
	private boolean isElementStrokeNone(Element element) {
		if(element.hasAttribute("style")) {
			String style = element.getAttribute("style").toLowerCase().replace("\s","");
			if(style.contains(LABEL_STROKE)) {
				int k = style.indexOf(LABEL_STROKE);
				String strokeStyleName = style.substring(k+LABEL_STROKE.length());
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
			myTurtle.jumpTo(v2.x,v2.y);
			arcTurtle(myTurtle, x2,y1, rx,ry, Math.PI * -0.5,Math.PI *  0.0,m); 
			arcTurtle(myTurtle, x2,y2, rx,ry, Math.PI *  0.0,Math.PI *  0.5,m);
			arcTurtle(myTurtle, x1,y2, rx,ry, Math.PI * -1.5,Math.PI * -1.0,m);
			arcTurtle(myTurtle, x1,y1, rx,ry, Math.PI * -1.0,Math.PI * -0.5,m);
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
	    logger.debug("{} circles.", pathNodeCount);
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
			myTurtle.jumpTo(v2.x,v2.y);
			
			double circ = Math.PI * 2.0 * r;
			circ = Math.ceil(Math.min(Math.max(3,circ),360));
			
		    logger.debug("circ={}", circ);
			for(double i=1;i<circ;++i) {
				double v = (Math.PI*2.0) * (i/circ);
				double s=r*Math.sin(v);
				double c=r*Math.cos(v);
				v2 = transform(cx+c,cy+s,m);
				myTurtle.moveTo(v2.x,v2.y);
			}
			v2 = transform(cx+r,cy,m);
			myTurtle.moveTo(v2.x,v2.y);
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
			myTurtle.jumpTo(v2.x,v2.y);
			
			double circ = Math.min(3,Math.floor(Math.PI * ry*rx)); 
			for(double i=1;i<circ;++i) {
				double v = (Math.PI*2.0) * (i/circ);
				double s=ry*Math.sin(v);
				double c=rx*Math.cos(v);
				v2 = transform(cx+c,cy+s,m);
				myTurtle.moveTo(v2.x,v2.y);
			}
			v2 = transform(cx+rx,cy,m);
			myTurtle.moveTo(v2.x,v2.y);
	    }
	}
	
	/**
	 * Parse through all the SVG path elements and raster them to gcode.
	 * @param paths the source of the elements
	 */
	private void parsePathElements(NodeList paths) throws Exception {
		double firstX=0;
		double firstY=0;
		double px=0;
		double py=0;
		
	    int pathCount = paths.getLength();
	    for( int iPath = 0; iPath < pathCount; iPath++ ) {
	    	if(paths.item( iPath ).getClass() == SVGOMPolylineElement.class) {
	    		logger.debug("Node is a polyline.");
	    		parsePolylineElements(paths);
	    		continue;
	    	}
	    	SVGOMPathElement element = ((SVGOMPathElement)paths.item( iPath ));
			if(isElementStrokeNone(element)) 
				continue;
			
			Matrix3d m = getMatrixFromElement(element);
			Vector3d v;

	    	SVGPathSegList pathList = element.getNormalizedPathSegList();
	    	//SVGPathSegList pathList = element.getPathSegList();
	    	int itemCount = pathList.getNumberOfItems();
	    	logger.debug("Node has {} elements.", itemCount);
	    	int sinceClosePath=0;
	    	
			for(int i=0; i<itemCount; i++) {
				++sinceClosePath;
				SVGPathSeg item = pathList.getItem(i);
				logger.debug("segType={}", item.getClass().getSimpleName());
				switch( item.getPathSegType() ) {
				case SVGPathSeg.PATHSEG_CLOSEPATH:  // Z z
					{
						logger.debug("Close path");
						v = transform(firstX,firstY,m);
						myTurtle.moveTo(v.x,v.y);
						sinceClosePath=0;
					}
					break;
				case SVGPathSeg.PATHSEG_MOVETO_ABS:  // M
					{
						SVGPathSegMovetoAbs path = (SVGPathSegMovetoAbs)item;
						logger.debug("Move Abs x{} y{}", path.getX(), path.getY());
						px = path.getX();
						py = path.getY();
						if(sinceClosePath==1) {
							firstX = px;
							firstY = py;
						}
						v = transform(px,py,m);
						myTurtle.jumpTo(v.x,v.y);
					}
					break;
				case SVGPathSeg.PATHSEG_MOVETO_REL:  // m
					{
						SVGPathSegMovetoRel path = (SVGPathSegMovetoRel)item;
						logger.debug("Move Rel x{} y{}", path.getX(), path.getY());
						px += path.getX();
						py += path.getY();
						if(sinceClosePath==1) {
							firstX = px;
							firstY = py;
						}
						v = transform(px,py,m);
						myTurtle.jumpTo(v.x,v.y);
					}
					break;
				case SVGPathSeg.PATHSEG_LINETO_ABS:  // L H V
					{
						SVGPathSegLinetoAbs path = (SVGPathSegLinetoAbs)item;
						logger.debug("Line Abs x{} y{}", path.getX(), path.getY());
						px = path.getX();
						py = path.getY();
						v = transform(px,py,m);
						myTurtle.moveTo(v.x,v.y);
					}
					break;
				case SVGPathSeg.PATHSEG_LINETO_REL:  // l h v
					{
						SVGPathSegLinetoRel path = (SVGPathSegLinetoRel)item;
						logger.debug("Line Rel x{} y{}", path.getX(), path.getY());
						px += path.getX();
						py += path.getY();
						v = transform(px,py,m);
						myTurtle.moveTo(v.x,v.y);
					}
					break;
				case SVGPathSeg.PATHSEG_CURVETO_CUBIC_ABS: // C c
					{
						SVGPathSegCurvetoCubicAbs path = (SVGPathSegCurvetoCubicAbs)item;
						logger.debug("Curve Cubic Abs x{}  y{}", path.getX(), path.getY());
						logger.debug("               1x{} 1y{}", path.getX1(), path.getY1());
						logger.debug("               2x{} 2y{}", path.getX2(), path.getY2());

						// x0,y0 is the first point
						double x0=px;
						double y0=py;
						// x3,y3 is the end point
						double x3=path.getX();
						double y3=path.getY();
						// x1,y1 is the first control point
						double x1=path.getX1();
						double y1=path.getY1();
						// x2,y2 is the second control point
						double x2=path.getX2();
						double y2=path.getY2();
						Bezier b = new Bezier(x0,y0,x1,y1,x2,y2,x3,y3);
						ArrayList<Point2D> points = b.generateCurvePoints(0.1);
						for(Point2D p2 : points) {
							v = transform(p2.x,p2.y,m);
							myTurtle.moveTo(v.x,v.y);
						}
						px=x3;
						py=y3;
					}
					break;
				default:
					{
						String m2="Found unknown SVGPathSeg type "+((SVGItem)item).getValueAsString();
						logger.debug(m2);
						throw new Exception(m2);
					}
				}
			}
		}
	}
    
	private Vector3d transform(double x, double y, Matrix3d m) {
		Vector3d p = new Vector3d(x,y,1);
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
			
			SVGMatrix svgMatrix = svgge.getCTM();
			// [ a c e ]
			// [ b d f ]
			// [ 0 0 1 ]
			m.m00 = svgMatrix.getA();	m.m10 = svgMatrix.getB();	m.m20 = 0;
			m.m01 = svgMatrix.getC();	m.m11 = svgMatrix.getD();	m.m21 = 0;
			m.m02 = svgMatrix.getE();	m.m12 = svgMatrix.getF();	m.m22 = 1;
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
	 * @link https://cwiki.apache.org/confluence/display/XMLGRAPHICSBATIK/BootSvgAndCssDom
	 */
	private void initSVGDOM(Document document) {
		UserAgent userAgent = new UserAgentAdapter();
		DocumentLoader loader = new DocumentLoader(userAgent);
		BridgeContext bridgeContext = new BridgeContext(userAgent, loader);
		bridgeContext.setDynamicState(BridgeContext.STATIC);

		// Enable CSS- and SVG-specific enhancements.
		(new GVTBuilder()).build(bridgeContext, document);
	}

	private static SVGDocument newDocumentFromInputStream(InputStream in) throws Exception {
		String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);
        return (SVGDocument) factory.createDocument("",in);
	}
}
