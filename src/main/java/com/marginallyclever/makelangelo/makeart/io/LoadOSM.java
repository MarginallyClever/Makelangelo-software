package com.marginallyclever.makelangelo.makeart.io;

import com.marginallyclever.makelangelo.turtle.Turtle;

import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.awt.Color;
import java.io.InputStream;
import java.util.*;

/**
 * Load OpenStreetMap (OSM) files and convert them to Turtle graphics commands.
 * Creates one layer (sub-Turtle) per color/category.
 */
public class LoadOSM implements TurtleLoader {
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

        OSMWay currentWay = null;
        while (r.hasNext()) {
            int ev = r.next();
            if (ev == XMLStreamConstants.START_ELEMENT) {
                String name = r.getLocalName();
                switch (name) {
                    case "node" -> {
                        long id = attrLong(r, "id", -1);
                        double lat = attrDouble(r, "lat", 0);
                        double lon = attrDouble(r, "lon", 0);
                        if (id >= 0) nodes.put(id, new OSMNode(id, lat, lon));
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
        LinkedHashMap<Integer, Turtle> layers = new LinkedHashMap<>();

        for (OSMWay w : ways) {
            int color = pickColor(w.tags);
            Turtle layer = layers.computeIfAbsent(color, c -> new Turtle(new Color(c)) );

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
        for (Map.Entry<Integer, Turtle> e : layers.entrySet()) {
            master.add(e.getValue());
        }

        // rotate 90 degrees and flip vertically
        master.rotate(-Math.PI / 2);

        return master;
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
            return rgb(0, 0, 0);
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