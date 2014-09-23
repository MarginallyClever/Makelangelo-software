/*
   Copyright 2008 Simon Mieth

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package org.kabeja.dxf.helpers;

import java.util.ArrayList;
import java.util.Iterator;

import org.kabeja.dxf.DXFPolyline;
import org.kabeja.dxf.DXFSpline;
import org.kabeja.dxf.DXFVertex;
import org.kabeja.math.NURBS;
import org.kabeja.math.NURBSFixedNTELSPointIterator;


public class DXFSplineConverter {
    public static DXFPolyline toDXFPolyline(DXFSpline spline) {
    	return toDXFPolyline(spline,30);
    }
    
    public static DXFPolyline toDXFPolyline(DXFSpline spline, int ntels) {
        DXFPolyline p = new DXFPolyline();
        p.setDXFDocument(spline.getDXFDocument());

        if ((spline.getDegree() > 0) && (spline.getKnots().length > 0)) {
        	// 2014-07-13 changed 30 to 10 to reduce the amount of memory used
            Iterator<Point> pi = new NURBSFixedNTELSPointIterator(toNurbs(spline), ntels);

            while (pi.hasNext()) {
                p.addVertex(new DXFVertex(pi.next()));
            }
        } else {
            // the curve is the control point polygon
            Iterator<SplinePoint> i = spline.getSplinePointIterator();

            while (i.hasNext()) {
                SplinePoint sp = i.next();

                if (sp.isControlPoint()) {
                    p.addVertex(new DXFVertex(sp));
                }
            }
        }

        if (spline.isClosed()) {
            p.setFlags(1);
        }

        return p;
    }

    public static NURBS toNurbs(DXFSpline spline) {
        Iterator<SplinePoint> i = spline.getSplinePointIterator();
        ArrayList<Point> list = new ArrayList<Point>();

        while (i.hasNext()) {
            SplinePoint sp = (SplinePoint) i.next();

            if (sp.isControlPoint()) {
                list.add((Point) sp);
            }
        }

        NURBS n = new NURBS((Point[]) list.toArray(new Point[list.size()]),
                spline.getKnots(), spline.getWeights(), spline.getDegree());
        n.setClosed(spline.isClosed());

        return n;
    }
}
