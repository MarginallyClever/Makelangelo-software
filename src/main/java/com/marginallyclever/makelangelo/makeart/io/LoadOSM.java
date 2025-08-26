package com.marginallyclever.makelangelo.makeart.io;

import com.marginallyclever.makelangelo.makeart.turtletool.CropTurtle;
import com.marginallyclever.makelangelo.turtle.StrokeLayer;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.filechooser.FileNameExtensionFilter;
import javax.vecmath.Point2d;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.InputStream;
import java.util.*;
import java.util.List;

/**
 * Load OpenStreetMap (OSM) files and convert them to Turtle graphics commands.
 * Creates one layer (sub-Turtle) per color/category.
 */
public class LoadOSM implements TurtleLoader {
    private static final Logger logger = LoggerFactory.getLogger(LoadOSM.class);
    private static final FileNameExtensionFilter filter = new FileNameExtensionFilter("OpenStreetMap", "osm");

    @Override
    public FileNameExtensionFilter getFileNameFilter() {
        return filter;
    }

    @Override
    public boolean canLoad(String filename) {
        String ext = filename.substring(filename.lastIndexOf('.') + 1);
        return Arrays.stream(filter.getExtensions()).anyMatch(ext::equalsIgnoreCase);
    }

    // Internal data holders
    private static class OSMNode {
        final long id;
        final double lat;
        final double lon;
        OSMNode(long id, double lat, double lon) {
            this.id = id; this.lat = lat; this.lon = lon;
        }
    }

    private static class OSMWay {
        final List<Long> nodeRefs = new ArrayList<>();
        final Map<String,String> tags = new HashMap<>();
    }

    @Override
    public Turtle load(InputStream inputStream) throws Exception {
        Map<Long, OSMNode> nodes = new HashMap<>(100_000);
        List<OSMWay> ways = new ArrayList<>();

        // First pass: parse XML
        XMLInputFactory f = XMLInputFactory.newFactory();
        XMLStreamReader r = f.createXMLStreamReader(inputStream);

        double boundsMinLat=0, boundsMaxLat=0;
        double boundsMinLon=0, boundsMaxLon=0;
        boolean hasBounds=false;
        Point2d p2 = new Point2d();

        OSMWay currentWay = null;
        while (r.hasNext()) {
            int ev = r.next();
            if (ev == XMLStreamConstants.START_ELEMENT) {
                String name = r.getLocalName();
                switch (name) {
                    case "bounds" -> {
                        boundsMinLat = attrDouble(r,"minlat",Double.NaN);
                        boundsMinLon = attrDouble(r,"minlon",Double.NaN);
                        boundsMaxLat = attrDouble(r,"maxlat",Double.NaN);
                        boundsMaxLon = attrDouble(r,"maxlon",Double.NaN);
                        hasBounds = true;
                        logger.info("Bounds detected: lat [{} , {}], lon [{} , {}]",
                                boundsMinLat,boundsMaxLat,boundsMinLon,boundsMaxLon);
                    }
                    case "node" -> {
                        long id = attrLong(r, "id", -1);
                        double latitude = attrDouble(r, "lat", 0);
                        double longitude = attrDouble(r, "lon", 0);

                        convertLatLong(latitude, longitude, p2);
                        if (id >= 0) nodes.put(id, new OSMNode(id, p2.y, p2.x));
                    }
                    case "way" -> currentWay = new OSMWay();
                    case "nd" -> {
                        if (currentWay != null) {
                            long ref = attrLong(r, "ref", -1);
                            if (ref >= 0) currentWay.nodeRefs.add(ref);
                        }
                    }
                    case "tag" -> {
                        if (currentWay != null) {
                            String k = attr(r, "k");
                            String v = attr(r, "v");
                            if (k != null && v != null) currentWay.tags.put(k, v);
                        }
                    }
                }
            } else if (ev == XMLStreamConstants.END_ELEMENT) {
                if ("way".equals(r.getLocalName()) && currentWay != null) {
                    if (!currentWay.nodeRefs.isEmpty()) ways.add(currentWay);
                    currentWay = null;
                }
            }
        }
        r.close();

        if (nodes.isEmpty() || ways.isEmpty()) return new Turtle();

        // Compute bounding box
        double minLat = Double.POSITIVE_INFINITY, maxLat = Double.NEGATIVE_INFINITY;
        double minLon = Double.POSITIVE_INFINITY, maxLon = Double.NEGATIVE_INFINITY;
        for (OSMNode n : nodes.values()) {
            if (n.lat < minLat) minLat = n.lat;
            if (n.lat > maxLat) maxLat = n.lat;
            if (n.lon < minLon) minLon = n.lon;
            if (n.lon > maxLon) maxLon = n.lon;
        }
        double dLat = Math.max(1e-9, maxLat - minLat);
        double dLon = Math.max(1e-9, maxLon - minLon);
        double scale = 1000.0 / Math.max(dLat, dLon); // target ~1000 units max dimension

        // Layers by color
        LinkedHashMap<String, Turtle> layers = new LinkedHashMap<>();

        for (OSMWay w : ways) {
            String layerName = layerNameForTags(w.tags);
            int color = pickColor(w.tags);
            Turtle layer = layers.get(layerName);
            if (layer == null) {
                // this is only logged once but the layer is used for all subsequent lines of this name
                logger.info("New layer: {} (#{}).", layerName, String.format("%06X", color));
                layer = new Turtle(new Color(color));
                layers.put(layerName, layer);
            }

            // Build polyline
            List<double[]> pts = new ArrayList<>();
            for (Long ref : w.nodeRefs) {
                OSMNode n = nodes.get(ref);
                if (n == null) continue;
                double x = (n.lon - minLon) * scale;
                double y = (maxLat - n.lat) * scale; // flip Y so north is up
                pts.add(new double[]{x, y});
            }
            if (pts.size() < 2) continue;

            boolean isArea = isArea(w.tags);
            if (isArea) {
                double[] first = pts.getFirst();
                double[] last = pts.getLast();
                if (first[0] != last[0] || first[1] != last[1]) {
                    pts.add(new double[]{first[0], first[1]});
                }
            }

            // Emit to layer Turtle
            double[] start = pts.getFirst();
            layer.jumpTo(start[0], -start[1]);
            for (int i = 1; i < pts.size(); i++) {
                double[] p = pts.get(i);
                layer.moveTo(p[0], -p[1]);
            }
        }

        // Merge layers into master turtle (preserving insertion order)
        Turtle master = new Turtle();
        for (Map.Entry<String, Turtle> e : layers.entrySet()) {
            StrokeLayer layer = e.getValue().getLayers().getFirst();
            layer.setName(e.getKey());
            master.getLayers().add(layer);
        }

        if(hasBounds) {
            Point2d pMax = new Point2d();
            Point2d pMin = new Point2d();
            convertLatLong(boundsMaxLat,boundsMaxLon,pMin);
            convertLatLong(boundsMinLat,boundsMinLon,pMax);
            // scale the same as the points
            pMin.x = (pMin.x - minLon) * scale;
            pMax.x = (pMax.x - minLon) * scale;
            pMin.y = -(maxLat - pMin.y) * scale;
            pMax.y = -(maxLat - pMax.y) * scale;
            // ensure pMin is the min corner
            if(pMin.x<pMax.x) {
                double t = pMin.x;
                pMin.x = pMax.x;
                pMax.x = t;
            }
            if(pMin.y<pMax.y) {
                double t = pMin.y;
                pMin.y = pMax.y;
                pMax.y = t;
            }
            // pMax becomes width/height
            pMax.sub(pMin);
            // crop to bounds
            CropTurtle.run(master, new Rectangle2D.Double(pMin.x,pMin.y,pMax.x,pMax.y));
        }

        return master;
    }

    // convert lat/lon to Web Mercator (EPSG:3857) x/y in meters
    private void convertLatLong(double latitude, double longitude, Point2d p) {
        final double R = 6378137.0;
        p.x = Math.toRadians(longitude) * R;
        p.y = Math.log(Math.tan(Math.PI / 4 + Math.toRadians(latitude) / 2)) * R;
    }

    private String layerNameForTags(Map<String,String> tags) {
        if (tags.containsKey("water") || "water".equals(tags.get("natural")) || tags.containsKey("waterway"))
            return "water";
        if (tags.containsKey("building"))
            return "building";
        if (tags.containsKey("highway"))
            return "highway";
        if ("forest".equals(tags.get("landuse")) || "wood".equals(tags.get("natural")))
            return "forest";
        if ("park".equals(tags.get("leisure")))
            return "park";
        if (tags.containsKey("railway"))
            return "railway";
        if (tags.containsKey("boundary"))
            return "boundary";
        return "other";
    }

    // Decide if way is an area
    private boolean isArea(Map<String,String> tags) {
        if ("yes".equalsIgnoreCase(tags.get("area"))) return true;
        if (tags.containsKey("building")) return true;
        if (tags.containsKey("landuse")) return true;
        if (tags.containsKey("natural")) return true;
        if (tags.containsKey("leisure")) return true;
        return false;
    }

    // Map tags to a color (RGB int)
    private int pickColor(Map<String,String> tags) {
        if (tags.containsKey("water") || "water".equals(tags.get("natural")) || tags.containsKey("waterway"))
            return rgb(0, 102, 204);
        if (tags.containsKey("building"))
            return rgb(80, 80, 80);
        if (tags.containsKey("highway"))
            return rgb(255, 128, 0);
        if ("forest".equals(tags.get("landuse")) || "wood".equals(tags.get("natural")))
            return rgb(0, 140, 0);
        if ("park".equals(tags.get("leisure")))
            return rgb(50, 180, 50);
        if (tags.containsKey("railway"))
            return rgb(150, 60, 60);
        if (tags.containsKey("boundary"))
            return rgb(120, 0, 180);
        return rgb(0, 0, 0);
    }

    private static int rgb(int r, int g, int b) {
        return (r & 255) << 16 | (g & 255) << 8 | (b & 255);
    }

    // Attribute helpers
    private String attr(XMLStreamReader r, String name) {
        return r.getAttributeValue(null, name);
    }
    private long attrLong(XMLStreamReader r, String name, long def) {
        String v = attr(r, name);
        if (v == null) return def;
        try { return Long.parseLong(v); } catch (NumberFormatException e) { return def; }
    }
    private double attrDouble(XMLStreamReader r, String name, double def) {
        String v = attr(r, name);
        if (v == null) return def;
        try { return Double.parseDouble(v); } catch (NumberFormatException e) { return def; }
    }
}