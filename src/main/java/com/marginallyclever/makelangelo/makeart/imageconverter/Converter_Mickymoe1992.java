package com.marginallyclever.makelangelo.makeart.imageconverter;

import com.marginallyclever.donatello.select.SelectDouble;
import com.marginallyclever.donatello.select.SelectSlider;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.imagefilter.FilterDesaturate;
import com.marginallyclever.makelangelo.makeart.turtletool.ReorderHelper;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.turtle.Turtle;

import javax.vecmath.Point2d;
import java.awt.*;
import java.util.Arrays;
import java.util.PriorityQueue;

/**
 * <p>Converts an image into a contour map using the fast marching method.
 * The contours are extracted from the arrival times computed from the image brightness.</p>
 * <p>Style idea by <a href="https://www.reddit.com/r/PlotterArt/comments/1lu3bok/finally_i_have_a_contour_plotting_algorithm/">Mickymoe1992</a>.</p>
 */
public class Converter_Mickymoe1992 extends ImageConverter {
    private static double minSpacing = 10.0;
    private static int seedX = 50, seedY = 50;
    
    private double px, py;
    private TransformedImage img;
    private boolean recalculateFFM = true;
    private double [][] ffm;

    public Converter_Mickymoe1992() {
        super();

        var minSpacingSelect = new SelectDouble("minSpacing", Translator.get("Converter_Mickymoe1992.minSpacing"), Converter_Mickymoe1992.minSpacing);
        minSpacingSelect.addSelectListener((evt) -> {
            minSpacing = (double) evt.getNewValue();
            recalculateFFM=false;
            fireRestart();
        });
        add(minSpacingSelect);

        var chooseX = new SelectSlider("px", Translator.get("Converter_Mickymoe1992.seedX"), 100,0,seedX);
        add(chooseX);
        chooseX.addSelectListener((evt) -> {
            seedX = (int) evt.getNewValue();
            recalculateFFM=true;
            fireRestart();
        });

        var chooseY = new SelectSlider("py", Translator.get("Converter_Mickymoe1992.seedY"), 100,0,seedY);
        add(chooseY);
        chooseY.addSelectListener((evt) -> {
            seedY = (int) evt.getNewValue();
            recalculateFFM=true;
            fireRestart();
        });
    }

    @Override
    public String getName() {
        return "Mickymoe1992";
    }

    @Override
    public void start(Paper paper, TransformedImage image) {
        super.start(paper, image);

        FilterDesaturate bw = new FilterDesaturate(myImage);
        img = bw.filter();

        // use fast marching method to compute arrival times
        if(recalculateFFM) {
            ffm = computeArrivalTimes();
        }

        turtle = new Turtle();
        turtle.setStroke(Color.BLACK,settings.getDouble(PlotterSettings.DIAMETER));
        px = myPaper.getMarginRectangle().getMinX();
        py = myPaper.getMarginRectangle().getMinY();

        extractContours(ffm, minSpacing);

        fireConversionFinished();
        recalculateFFM=true;
    }

    /**
     * Extract iso-contours for the given levels from a scalar field.
     * @param T scalar field (arrival times)
     * @param spacing spacing between contours
     */
    public void extractContours(double[][] T, double spacing) {
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

        Turtle sum = new Turtle(Color.BLACK,settings.getDouble(PlotterSettings.DIAMETER));
        var reorderHelper = new ReorderHelper();

        // Build levels spaced by `spacing`
        double step = Math.max(spacing,1);
        for (double level = min; level <= max; level += step) {
            turtle = new Turtle(Color.BLACK,settings.getDouble(PlotterSettings.DIAMETER));
            marchingSquares(T,level);
            var result = reorderHelper.splitAndReorderTurtle(turtle);
            sum.add(result);
        }

        turtle = sum;
    }

    private void marchingSquares(double[][] T, double level) {
        int w = T.length;
        int h = T[0].length;
        for (int x = 0; x < w - 1; x++) {
            for (int y = 0; y < h - 1; y++) {
                marchSquare(x,y,T,level);
            }
        }
    }

    private void marchSquare(int x, int y, double[][] T, double level) {
        double v00 = T[x][y];
        double v10 = T[x + 1][y];
        double v01 = T[x][y + 1];
        double v11 = T[x + 1][y + 1];

        int code = 0;
        if(v00 > level) code |= 1;
        if(v10 > level) code |= 2;
        if(v11 > level) code |= 4;
        if(v01 > level) code |= 8;

        if(code > 7) code = 15 - code; // mirror symmetry

        switch(code) {
            case 0:  break;
            case 1:  case1(x,y,T,level);  break;
            case 2:  case2(x,y,T,level);  break;
            case 3:  case3(x,y,T,level);  break;
            case 4:  case4(x,y,T,level);  break;
            case 5:  case5(x,y,T,level);  break;
            case 6:  case6(x,y,T,level);  break;
            case 7:  case7(x,y,T,level);  break;
        }
    }

    double lerp(double a,double b,double v) {
        return a + (b - a) * v;
    }

    Point2d lerpEdge(int x0, int y0, int x1, int y1, double[][] T,double level) {
        double in0 = T[x0][y0];
        double in1 = T[x1][y1];

        double v = (level-in0) / (in1-in0);
        v = Math.max(0,Math.min(1,v));
        double x3 = lerp(x0,x1,v);
        double y3 = lerp(y0,y1,v);
        return new Point2d(x3,y3);
    }

    void line(Point2d a,Point2d b) {
        var d = 1;//turtle.getDiameter();
        turtle.jumpTo(px+a.x*d,py+a.y*d);
        turtle.moveTo(px+b.x*d,py+b.y*d);
    }

    void case1(int x0,int y0, double[][] T,double level) {
        int x1 = x0+1;
        int y1 = y0+1;
        Point2d a = lerpEdge(x0,y0,x0,y1,T,level);
        Point2d b = lerpEdge(x0,y0,x1,y0,T,level);
        line(a,b);
    }

    void case2(int x0,int y0, double[][] T,double level) {
        int x1 = x0+1;
        int y1 = y0+1;
        Point2d a = lerpEdge(x1,y0,x0,y0,T,level);
        Point2d b = lerpEdge(x1,y0,x1,y1,T,level);
        line(a,b);
    }

    // 1 + 2
    void case3(int x0,int y0, double[][] T,double level) {
        int x1 = x0+1;
        int y1 = y0+1;
        Point2d a = lerpEdge(x0,y0,x0,y1,T,level);
        Point2d b = lerpEdge(x1,y0,x1,y1,T,level);
        line(a,b);
    }

    void case4(int x0,int y0, double[][] T,double level) {
        int x1 = x0+1;
        int y1 = y0+1;
        Point2d a = lerpEdge(x1,y1,x0,y1,T,level);
        Point2d b = lerpEdge(x1,y1,x1,y0,T,level);
        line(a,b);
    }

    // 1 + 4
    void case5(int x0,int y0, double[][] T,double level) {
        case1(x0,y0,T,level);
        case4(x0,y0,T,level);
    }

    // 2 + 4
    void case6(int x0,int y0, double[][] T,double level) {
        int x1 = x0+1;
        int y1 = y0+1;
        Point2d a = lerpEdge(x0,y0,x1,y0,T,level);
        Point2d b = lerpEdge(x0,y1,x1,y1,T,level);
        line(a,b);
    }

    // 1+2+4
    void case7(int x0,int y0, double[][] T,double level) {
        int x1 = x0+1;
        int y1 = y0+1;
        Point2d a = lerpEdge(x0,y1,x0,y0,T,level);
        Point2d b = lerpEdge(x0,y1,x1,y1,T,level);
        line(a,b);
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

    /**
     * Computes the arrival times using the Fast Marching Method (FMM).
     * @return
     */
    public double[][] computeArrivalTimes() {
        var rect = myPaper.getMarginRectangle();
        var d = Math.max(1,turtle.getDiameter());
        int w = (int)Math.ceil(rect.getWidth()/d);
        int h = (int)Math.ceil(rect.getHeight()/d);
        int minX = (int)rect.getMinX();
        int minY = (int)rect.getMinY();

        // Precompute speeds F (invert brightness: dark = slow)
        double[][] F = new double[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int rgb = img.sample(x*d+minX, y*d+minY,2);
                int gray = rgb & 0xff; // assume grayscale
                // Normalize: 0.1..1.0 to avoid divide by zero
                F[x][y] = 0.01 + 0.99 * (gray / 255.0);
            }
        }

        // Neighbor offsets (4-connectivity)
        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, -1, 1};

        boolean[][] frozen = new boolean[w][h]; // processed pixels
        double[][] T = new double[w][h]; // arrival time
        // Initialize all T to infinity
        for (int i = 0; i < w; i++) {
            Arrays.fill(T[i], Double.POSITIVE_INFINITY);
        }

        PriorityQueue<Node> pq = new PriorityQueue<>();
        // Seed point
        int sx = (int)Math.max(0,Math.min(w-1,w*seedX/100.0));
        int sy = (int)Math.max(0,Math.min(h-1,h*seedY/100.0));
        T[sx][sy] = 0.0;
        pq.add(new Node(sx,sy, 0.0));

        while (!pq.isEmpty()) {
            Node n = pq.poll();
            if(frozen[n.x][n.y]) continue;
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
