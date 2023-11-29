package com.marginallyclever.convenience.voronoi;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.triangulate.VoronoiDiagramBuilder;

import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * VoronoiTesselator2 uses the locationtech.jts library to generate a voronoi diagram.
 * @author Dan
 * @since 2022-04-08
 */
public class VoronoiTesselator2 {
    private final GeometryFactory geometryFactory = new GeometryFactory();
    private Coordinate[] coordinates;
    private Geometry diagram;

    public void setNumHulls(int numHulls) {
        coordinates = new Coordinate[numHulls];
    }

    public void tessellate(List<VoronoiCell> points, Rectangle2D bounds, double tolerance) {
        if(coordinates==null || points.size()!=coordinates.length) setNumHulls(points.size());

        int i=0;
        for(VoronoiCell cell : points) {
            coordinates[i++] = cell.center;
        }
        MultiPoint multiPoint = geometryFactory.createMultiPointFromCoords(coordinates);

        VoronoiDiagramBuilder builder = new VoronoiDiagramBuilder();
        builder.setSites(multiPoint);
        builder.setClipEnvelope(new Envelope(bounds.getMinX(),bounds.getMaxX(),bounds.getMinY(),bounds.getMaxY()));
        builder.setTolerance(tolerance);

        diagram = builder.getDiagram(multiPoint.getFactory());
    }

    public int getNumHulls() {
        return (diagram==null) ? 0 : diagram.getNumGeometries();
    }

    public Polygon getHull(int i) {
        return (Polygon)diagram.getGeometryN(i);
    }

}
