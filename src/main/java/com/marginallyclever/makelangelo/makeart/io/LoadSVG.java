package com.marginallyclever.makelangelo.makeart.io;

import com.marginallyclever.convenience.Bezier;
import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.convenience.W3CColorNames;
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
import java.io.InputStream;
import java.util.List;

/**
 * @author Dan Royer
 * See <a href="https://www.w3.org/TR/SVG/paths.html">w3</a>
 */
public class LoadSVG implements TurtleLoader {
	private static final Logger logger = LoggerFactory.getLogger(LoadSVG.class);

	private static final String LABEL_STROKE="stroke:";

	private static final FileNameExtensionFilter filter = new FileNameExtensionFilter("Scaleable Vector Graphics 1.1", "svg");
	private Turtle myTurtle;

	private boolean isNewPath;  // for cubic paths
	private final Vector3d pathFirstPoint = new Vector3d();
	private final Vector3d pathPoint = new Vector3d();

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
		if (in == null) {
			throw new NullPointerException("Input stream is null");
		}

		logger.debug("Loading...");

		Document document = newDocumentFromInputStream(in);
		initSVGDOM(document);

		myTurtle = new Turtle();
		myTurtle.setColor(new ColorRGB(0,0,0));
		parseAll(document);

		// plotter coordinates are inverted in Y so flip the image.
		myTurtle.scale(1, -1);

		Turtle t2 = new Turtle();
		t2.history.clear();

		// remove tool changes for zero-length moves.
		List<Turtle> list = myTurtle.splitByToolChange();
		for(Turtle t : list) {
			if(t.getDrawDistance()>0)
				t2.add(t);
		}
		return t2;
	}

	private void parseAll(Document document) throws Exception {
		SVGOMSVGElement documentElement = (SVGOMSVGElement)document.getDocumentElement();

		logger.debug("...parse path");			parsePathElements(    documentElement.getElementsByTagName( "path"     ));
		logger.debug("...parse polylines");		parsePolylineElements(documentElement.getElementsByTagName( "polyline" ));
		logger.debug("...parse polygons");		parsePolylineElements(documentElement.getElementsByTagName( "polygon"  ));
		logger.debug("...parse lines");			parseLineElements(    documentElement.getElementsByTagName( "line"     ));
		logger.debug("...parse rects");			parseRectElements(    documentElement.getElementsByTagName( "rect"     ));
		logger.debug("...parse circles");		parseCircleElements(  documentElement.getElementsByTagName( "circle"   ));
		logger.debug("...parse ellipses");		parseEllipseElements( documentElement.getElementsByTagName( "ellipse"  ));
	}

	/**
	 * Parse through all the SVG polyline elements and raster them to gcode.
	 * @param pathNodes the source of the elements
	 */
	private void parsePolylineElements(NodeList pathNodes) throws Exception {
	    int pathNodeCount = pathNodes.getLength();
		logger.debug("{} elements", pathNodeCount);
	    for( int iPathNode = 0; iPathNode < pathNodeCount; iPathNode++ ) {
	    	SVGPointShapeElement element = (SVGPointShapeElement)pathNodes.item( iPathNode );
			if(setStrokeToElementColorBecomesNone(element)) continue;

			Matrix3d m = getMatrixFromElement(element);

			SVGPointList pointList = element.getAnimatedPoints();
			int numPoints = pointList.getNumberOfItems();
			//logger.debug("New Node has "+pathObjects+" elements.");

			SVGPoint item = pointList.getItem(0);
			Vector3d v2 = transform(item.getX(),item.getY(),m);
			myTurtle.jumpTo(v2.x,v2.y);

			for( int i=1; i<numPoints; ++i ) {
				item = pointList.getItem(i);
				v2 = transform(item.getX(),item.getY(),m);
				myTurtle.moveTo(v2.x,v2.y);
			}
		}
	}

	/**
	 * Read and apply the element stroke color.
	 * @param element source of the stroke color.
	 * @return true if the stroke color is 'none' or white (assumed to be the paper color)
	 */
	private boolean setStrokeToElementColorBecomesNone(Element element) {
		ColorRGB color = getStroke(element);
		if(color==null) return false;  // none
		if(color.equals(new ColorRGB(255,255,255))) return true;  // white

		if(!color.equals(myTurtle.getColor())) {
			logger.debug("Setting stroke color to {}",color);
			myTurtle.setColor(color);
		}
		return false;
	}

	private void parseLineElements(NodeList node) throws Exception {
		Vector3d v2;
	    int pathNodeCount = node.getLength();
		logger.debug("{} elements", pathNodeCount);
	    for( int iPathNode = 0; iPathNode < pathNodeCount; iPathNode++ ) {
			Element element = (Element)node.item( iPathNode );
			if(setStrokeToElementColorBecomesNone(element)) continue;

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

	private ColorRGB getStroke(Element element) {
		if(element.hasAttribute("style")) {
			String style = element.getAttribute("style").toLowerCase().replace("\\s+","");
			if(!style.contains(LABEL_STROKE)) {
				// default SVG stroke is "none", which isn't even transparent - it's nothing!
				return null;
			} else {
				int k = style.indexOf(LABEL_STROKE);
				String strokeStyleName = style.substring(k + LABEL_STROKE.length());
				return stringToColor(strokeStyleName);
			}
		}
		if(element.hasAttribute("stroke")) {
			String strokeStyleName = element.getAttribute("stroke").toLowerCase().replace("\\s+","");
			return stringToColor(strokeStyleName);
		}
		return null;
	}

	private ColorRGB stringToColor(String strokeName) {
		if(strokeName.startsWith("#")) {
			strokeName = strokeName.substring(1);
			if(strokeName.length()==3) {
				int r = Integer.parseInt(strokeName.substring(0,1),16);
				int g = Integer.parseInt(strokeName.substring(1,2),16);
				int b = Integer.parseInt(strokeName.substring(2,3),16);
				return new ColorRGB(r,g,b);
			} else if(strokeName.length()==6) {
				int r = Integer.parseInt(strokeName.substring(0,2),16);
				int g = Integer.parseInt(strokeName.substring(2,4),16);
				int b = Integer.parseInt(strokeName.substring(4,6),16);
				return new ColorRGB(r,g,b);
			}
		} else if(strokeName.startsWith("rgb(")) {
			strokeName = strokeName.substring(4,strokeName.length()-1);
			if(strokeName.contains("%")) {
				strokeName = strokeName.replace("%","");
				String [] parts = strokeName.split(",");
				int r = (int)(Integer.parseInt(parts[0])*255.0/100.0);
				int g = (int)(Integer.parseInt(parts[1])*255.0/100.0);
				int b = (int)(Integer.parseInt(parts[2])*255.0/100.0);
				return new ColorRGB(r,g,b);
			} else {
				String [] parts = strokeName.split(",");
				int r = Integer.parseInt(parts[0]);
				int g = Integer.parseInt(parts[1]);
				int b = Integer.parseInt(parts[2]);
				return new ColorRGB(r,g,b);
			}
		} else {
			return W3CColorNames.get(strokeName);
		}
		return null;
	}

	/**
	 * Draw rectangles that may have rounded corners.
	 * given corners
	 * <pre>
	 *    x0 x1 x2 x3
	 * y0    a  b
	 * y1 c  i  j  d
	 * y2 e  m  k  f
	 * y3    g  h
	 * </pre>
	 * draw a-b-d-f-h-g-e-c-a.
	 * See <a href="https://developer.mozilla.org/en-US/docs/Web/SVG/Element/rect">mozilla</a>
	 * @param node the source of the elements
	 */
	private void parseRectElements(NodeList node) throws Exception {
	    int pathNodeCount = node.getLength();
		logger.debug("{} elements", pathNodeCount);
	    for( int iPathNode = 0; iPathNode < pathNodeCount; iPathNode++ ) {
			Element element = (Element)node.item( iPathNode );
			if(setStrokeToElementColorBecomesNone(element)) continue;

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
	 * @param turtle the drawing tool
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
			double r = Math.max(rx, ry);
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
		logger.debug("{} elements", pathNodeCount);
	    for( int iPathNode = 0; iPathNode < pathNodeCount; iPathNode++ ) {
			Element element = (Element)node.item( iPathNode );
			if(setStrokeToElementColorBecomesNone(element)) continue;

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
			printEllipse(m, cx, cy, r, r, circ);
		}
	}

	private void parseEllipseElements(NodeList node) {
		Vector3d v2;
	    int pathNodeCount = node.getLength();
		logger.debug("{} elements", pathNodeCount);
	    for( int iPathNode = 0; iPathNode < pathNodeCount; iPathNode++ ) {
			Element element = (Element)node.item( iPathNode );
			if(setStrokeToElementColorBecomesNone(element)) continue;

			Matrix3d m = getMatrixFromElement(element);

			double cx=0,cy=0,rx=0,ry=0;
			if(element.hasAttribute("cx")) cx = Double.parseDouble(element.getAttribute("cx"));
			if(element.hasAttribute("cy")) cy = Double.parseDouble(element.getAttribute("cy"));
			if(element.hasAttribute("rx")) rx = Double.parseDouble(element.getAttribute("rx"));
			if(element.hasAttribute("ry")) ry = Double.parseDouble(element.getAttribute("ry"));
			v2 = transform(cx+rx,cy,m);
			myTurtle.jumpTo(v2.x,v2.y);

			double perimeterOfAnEllipseApprox = Math.PI * 2.0 * Math.sqrt((ry*ry + rx*rx)/2.0);
			double steps = Math.max(3,perimeterOfAnEllipseApprox);
			steps = Math.min(60,steps);
			printEllipse(m, cx, cy, rx, ry, steps);
		}
	}

	private void printEllipse(Matrix3d m, double cx, double cy, double rx, double ry, double steps) {
		Vector3d v2;
		for(double i = 1; i<steps; ++i) {
			double v = (Math.PI*2.0) * (i/steps);
			double s=ry*Math.sin(v);
			double c=rx*Math.cos(v);
			v2 = transform(cx+c,cy+s,m);
			myTurtle.moveTo(v2.x,v2.y);
		}
		v2 = transform(cx+rx,cy,m);
		myTurtle.moveTo(v2.x,v2.y);
	}

	/**
	 * Parse through all the SVG path elements and raster them to {@link Turtle}.
	 * @param paths the source of the elements
	 */
	private void parsePathElements(NodeList paths) throws Exception {
		int pathCount = paths.getLength();
		logger.debug("{} elements", pathCount);
		for( int iPath = 0; iPath < pathCount; iPath++ ) {
			if(paths.item( iPath ) instanceof SVGOMPolylineElement) {
				logger.debug("Node is a polyline.");
				parsePolylineElements(paths);
				continue;
			}
			SVGOMPathElement element = ((SVGOMPathElement)paths.item( iPath ));
			if(setStrokeToElementColorBecomesNone(element)) continue;


			Matrix3d m = getMatrixFromElement(element);

			SVGPathSegList pathList = element.getNormalizedPathSegList();
			int itemCount = pathList.getNumberOfItems();
			logger.debug("Node has {} elements.", itemCount);
			isNewPath=true;

			for(int i=0; i<itemCount; i++) {
				SVGPathSeg item = pathList.getItem(i);
				switch( item.getPathSegType() ) {
					case SVGPathSeg.PATHSEG_MOVETO_ABS 			-> doMoveToAbs(item,m);  	// M
					case SVGPathSeg.PATHSEG_MOVETO_REL 			-> doMoveRel(item,m);  		// m
					case SVGPathSeg.PATHSEG_LINETO_ABS 			-> doLineToAbs(item,m);  	// L H V
					case SVGPathSeg.PATHSEG_LINETO_REL 			-> doLineToRel(item,m);  	// l h v
					case SVGPathSeg.PATHSEG_CURVETO_CUBIC_ABS 	-> doCubicCurveAbs(item,m);	// C c
					case SVGPathSeg.PATHSEG_CLOSEPATH 			-> doClosePath(); 			// Z z
					default -> throw new Exception("Found unknown SVGPathSeg type "+((SVGItem)item).getValueAsString());
				}
			}
		}
	}

	private void doCubicCurveAbs(SVGPathSeg item, Matrix3d m) {
		SVGPathSegCurvetoCubicAbs path = (SVGPathSegCurvetoCubicAbs)item;

		// x0,y0 is the first point
		Vector3d p0 = pathPoint;
		// x1,y1 is the first control point
		Vector3d p1 = transform(path.getX1(),path.getY1(),m);
		// x2,y2 is the second control point
		Vector3d p2 = transform(path.getX2(),path.getY2(),m);
		// x3,y3 is the end point
		Vector3d p3 = transform(path.getX(),path.getY(),m);

		logger.debug("Cubic curve {} {} {} {}", p0,p1,p2,p3);

		Bezier b = new Bezier(
				p0.x,p0.y,
				p1.x,p1.y,
				p2.x,p2.y,
				p3.x,p3.y);
		List<Point2D> points = b.generateCurvePoints(0.1);
		for(Point2D p : points) myTurtle.moveTo(p.x,p.y);
		pathPoint.set(p3);
		isNewPath=true;
	}

	private void doLineToRel(SVGPathSeg item, Matrix3d m) {
		SVGPathSegLinetoRel path = (SVGPathSegLinetoRel)item;
		Vector3d p = transform(path.getX(),path.getY(),m);
		logger.debug("Line Rel {}", p);
		pathPoint.set(myTurtle.getX(),myTurtle.getY(),0);
		pathPoint.add(p);
		myTurtle.moveTo(pathPoint.x,pathPoint.y);
		isNewPath=false;
	}

	private void doLineToAbs(SVGPathSeg item, Matrix3d m) {
		SVGPathSegLinetoAbs path = (SVGPathSegLinetoAbs)item;
		Vector3d p = transform(path.getX(),path.getY(),m);
		logger.debug("Line Abs {}", p);
		pathPoint.set(p);
		myTurtle.moveTo(pathPoint.x,pathPoint.y);
		isNewPath=false;
	}

	private void doMoveRel(SVGPathSeg item, Matrix3d m) {
		SVGPathSegMovetoRel path = (SVGPathSegMovetoRel)item;
		Vector3d p = transform(path.getX(),path.getY(),m);
		logger.debug("Move Rel {}", p);
		pathPoint.add(p);
		if(isNewPath) pathFirstPoint.set(pathPoint);
		myTurtle.jumpTo(p.x,p.y);
		isNewPath=false;
	}

	private void doMoveToAbs(SVGPathSeg item, Matrix3d m) {
		SVGPathSegMovetoAbs path = (SVGPathSegMovetoAbs)item;
		Vector3d p = transform(path.getX(),path.getY(),m);
		logger.debug("Move Abs {}", p);
		pathPoint.set(p);
		if(isNewPath) pathFirstPoint.set(pathPoint);
		myTurtle.jumpTo(p.x,p.y);
		isNewPath=false;
	}

	private void doClosePath() {
		logger.debug("Close path");
		myTurtle.moveTo(pathFirstPoint.x,pathFirstPoint.y);
		isNewPath=true;
	}

	private Vector3d transform(double x, double y, Matrix3d m) {
		Vector3d p = new Vector3d(x,y,1);
		m.transform(p);
		return p;
	}

	private Matrix3d getMatrixFromElement(Element element) {
		Matrix3d m = new Matrix3d();

		if(!(element instanceof SVGGraphicsElement)) {
			m.setIdentity();
			return m;
		}

		try {
			SVGGraphicsElement svgge = (SVGGraphicsElement)element;
			SVGMatrix svgMatrix = svgge.getCTM();
			// [ a c e ]
			// [ b d f ]
			// [ 0 0 1 ]
			m.m00 = svgMatrix.getA();	m.m01 = svgMatrix.getC();	m.m02 = svgMatrix.getE();
			m.m10 = svgMatrix.getB();	m.m11 = svgMatrix.getD();	m.m12 = svgMatrix.getF();
			m.m20 = 0;					m.m21 = 0;					m.m22 = 1;
		}
		catch(Exception e) {
			m.setIdentity();
		}
		return m;
	}

	/**
	 * Enhance the SVG DOM for the given document to provide CSS- and
	 * SVG-specific DOM interfaces.
	 * See <a href="https://cwiki.apache.org/confluence/display/XMLGRAPHICSBATIK/BootSvgAndCssDom">apache</a>
	 * @param document The document to enhance.
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
