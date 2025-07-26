package com.marginallyclever.makelangelo.makeart.imageconverter;

import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.imagefilter.FilterDesaturate;
import com.marginallyclever.makelangelo.makeart.turtletool.CropTurtle;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class Converter_ContourMap extends ImageConverter {
    private static final Logger logger = LoggerFactory.getLogger(Converter_ContourMap.class);

    private final GeometryFactory gf = new GeometryFactory();

    private static double minSpacing = 10.0;

    @Override
    public String getName() {
        return "Contour Map";
    }

    @Override
    public void start(Paper paper, TransformedImage image) {
        super.start(paper, image);

        FilterDesaturate bw = new FilterDesaturate(myImage);
        TransformedImage img = bw.filter();
        var sourceImage = img.getSourceImage();

        // use fast marching method to compute arrival times
        double [][] ffm = computeArrivalTimes(sourceImage,
                (int) (sourceImage.getWidth() / 2.0),
                (int) (sourceImage.getHeight() / 2.0));

        List<LineString> contours = extractContours(ffm, 15.0);

        turtle = new Turtle();
        turtle.setStroke(Color.BLACK,settings.getDouble(PlotterSettings.DIAMETER));
        // convert contours to turtle commands
        for (LineString contour : contours) {
            //LineString simplified = (LineString) DouglasPeuckerSimplifier.simplify(contour, 2.0);

            CropTurtle.addIntersectionToLayer(contour,turtle.getLayers().getLast());
        }

        turtle.translate(
                myPaper.getCenterX()-image.getSourceImage().getWidth()/2.0,
                myPaper.getCenterY()-image.getSourceImage().getHeight()/2.0);

        // flip the turtle to match the paper orientation
        turtle.scale(img.getScaleX(),img.getScaleY());

        fireConversionFinished();
    }

    /**
     * Extract iso-contours for the given levels from a scalar field.
     * @param T scalar field (arrival times)
     * @param spacing spacing between contours (e.g., 10.0)
     * @return list of LineStrings, one for each contour segment
     */
    public List<LineString> extractContours(double[][] T, double spacing) {
        int w = T.length;
        int h = T[0].length;

        // Find min and max values in T
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        for (double[] doubles : T) {
            for (int y = 0; y < h; y++) {
                double v = doubles[y];
                if (Double.isFinite(v)) {
                    min = Math.min(min, v);
                    max = Math.max(max, v);
                }
            }
        }

        // Build levels spaced by `spacing`
        List<Double> levelsList = new ArrayList<>();
        double step = Math.max(spacing,minSpacing);
        for (double level = min; level <= max; level += step) {
            levelsList.add(level);
        }

        return extractContoursAtLevels(T, levelsList);
    }

    /**
     * Core Marching Squares: extract contours for given levels.
     */
    private List<LineString> extractContoursAtLevels(double[][] T, List<Double> levels) {
        List<LineString> result = new ArrayList<>();

        for (double level : levels) {
            result.addAll(marchingSquares(T,level));
        }
        return result;
    }

    private List<LineString> marchingSquares(double[][] T, double level) {
        List<LineString> result = new ArrayList<>();
        int w = T.length;
        int h = T[0].length;
        for (int x = 0; x < w - 1; x++) {
            for (int y = 0; y < h - 1; y++) {
                double v00 = T[x][y];
                double v10 = T[x + 1][y];
                double v01 = T[x][y + 1];
                double v11 = T[x + 1][y + 1];

                int caseIndex = 0;
                if(v00 > level) caseIndex |= 1;
                if(v10 > level) caseIndex |= 2;
                if(v11 > level) caseIndex |= 4;
                if(v01 > level) caseIndex |= 8;

                if (caseIndex == 0 || caseIndex == 15) continue; // no crossing
                if(caseIndex> 7) caseIndex = 15 - caseIndex; // mirror symmetry

                List<Coordinate> segment = new ArrayList<>();

                // Interpolate crossings along edges
                if ((caseIndex & 1) != (caseIndex & 2)) {
                    segment.add(interp(
                            x, y,
                            x + 1, y,
                            v00, v10, level)); // bottom edge
                }
                if ((caseIndex & 2) != (caseIndex & 4)) {
                    segment.add(interp(
                            x + 1, y,
                            x + 1, y + 1,
                            v10, v11, level)); // right edge
                }
                if ((caseIndex & 4) != (caseIndex & 8)) {
                    segment.add(interp(
                            x + 1, y + 1,
                            x, y + 1,
                            v11, v01, level)); // top edge
                }
                if ((caseIndex & 8) != (caseIndex & 1)) {
                    segment.add(interp(
                            x, y + 1,
                            x, y,
                            v01, v00, level)); // left edge
                }

                if (segment.size() >= 2) {
                    result.add(gf.createLineString(segment.toArray(new Coordinate[0])));
                }
            }
        }
        return result;
    }

    double lerp(double a,double b,double t) {
        return a + (b - a) * t;
    }

    /**
     * Linear interpolation along an edge
     * @param x0 start x coordinate
     * @param y0 start y coordinate
     * @param x1 end x coordinate
     * @param y1 end y coordinate
     * @param v0 value at start point
     * @param v1 value at end point
     * @param level contour level to interpolate
     * @return the interpolated coordinate on the edge
     */
    private Coordinate interp(int x0, int y0, int x1, int y1,
                              double v0, double v1, double level) {
        double t = (level - v0) / (v1 - v0); // midpoint if no difference
        double t2 = Math.max(0,Math.min(1,t));
        double x = lerp(x0,x1,t2);
        double y = lerp(y0,y1,t2);
        return new Coordinate(x, y);
    }

    static class Node implements Comparable<Node> {
        int x, y;
        double t;

        Node(int x, int y, double t) {
            this.x = x;
            this.y = y;
            this.t = t;
        }

        @Override
        public int compareTo(Node o) {
            return Double.compare(this.t, o.t);
        }
    }

    public static double[][] computeArrivalTimes(BufferedImage image, int seedX, int seedY) {
        int w = image.getWidth();
        int h = image.getHeight();

        double[][] T = new double[w][h]; // arrival time
        boolean[][] frozen = new boolean[w][h]; // processed pixels
        PriorityQueue<Node> pq = new PriorityQueue<>();

        // Initialize all T to infinity
        for (int i = 0; i < w; i++)
            for (int j = 0; j < h; j++)
                T[i][j] = Double.POSITIVE_INFINITY;

        // Seed point
        T[seedX][seedY] = 0.0;
        pq.add(new Node(seedX, seedY, 0.0));

        // Precompute speeds F (invert brightness: dark = slow)
        double[][] F = new double[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int rgb = image.getRGB(x, y);
                int gray = (rgb >> 16) & 0xff; // assume grayscale
                // Normalize: 0.1..1.0 to avoid divide by zero
                F[x][y] = 0.1 + 0.9 * (gray / 255.0);
            }
        }

        // Neighbor offsets (4-connectivity)
        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, -1, 1};

        while (!pq.isEmpty()) {
            Node n = pq.poll();
            if (frozen[n.x][n.y]) continue;
            frozen[n.x][n.y] = true;

            for (int k = 0; k < 4; k++) {
                int nx = n.x + dx[k];
                int ny = n.y + dy[k];
                if (nx < 0 || nx >= w || ny < 0 || ny >= h || frozen[nx][ny]) continue;

                double oldT = T[nx][ny];
                double newT = solveEikonal(nx, ny, T, F);
                if (newT < oldT) {
                    T[nx][ny] = newT;
                    pq.add(new Node(nx, ny, newT));
                }
            }
        }

        return T;
    }

    // Update rule solving |∇T| * F = 1 using the two smallest neighbors
    private static double solveEikonal(int x, int y, double[][] T, double[][] F) {
        double tx = Math.min(T[Math.max(x - 1, 0)][y], T[Math.min(x + 1, T.length - 1)][y]);
        double ty = Math.min(T[x][Math.max(y - 1, 0)], T[x][Math.min(y + 1, T[0].length - 1)]);

        double f = F[x][y];
        double a = Math.min(tx, ty);
        double b = Math.max(tx, ty);

        // Solve quadratic: (T - a)^2 + (T - b)^2 = (1/f)^2
        double diff = b - a;
        double rhs = 1.0 / f;

        if (diff >= rhs) {
            return a + rhs;
        } else {
            double sum = a + b;
            double disc = sum * sum - 2 * (a * a + b * b - rhs * rhs);
            return 0.5 * (sum + Math.sqrt(Math.max(disc, 0)));
        }
    }
}
