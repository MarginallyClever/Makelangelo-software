package com.marginallyclever.makelangelo.makeart.turtletool;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.turtle.MovementType;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.operation.linemerge.LineMerger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class LeastHops extends TurtleTool {
    private static final Logger logger = LoggerFactory.getLogger(LeastHops.class);

    private final GeometryFactory geometryFactory = new GeometryFactory();
    private final Set<LineString> lineSet = new HashSet<>(); // Store all lines in the drawing
    private final List<LineString> mergedLines = new ArrayList<>(); // Merged lines after processing
    private final Map<Point, List<Point>> graph = new HashMap<>(); // Directed gra

    public LeastHops() {
        super(Translator.get("LeastHops.title"));
    }

    @Override
    public Turtle run(Turtle turtle) {
        lineSet.clear();
        mergedLines.clear();
        graph.clear();

        addLinesToJTS(turtle);

        if(hasMultipleComponents()) {
            logger.info("Multiple islands detected.  Conversion halted.");
            return turtle;
        }

        var path = findBestPath();
        if(path.isEmpty()) {
            logger.info("Found a path with zero lines?");
            return turtle;
        }

        //var newTurtle = convertLineStringsToTurtle();
        var newTurtle = convertPathToTurtle(path);

        return newTurtle;
    }

    private Turtle convertLineStringsToTurtle() {
        var newTurtle = new Turtle();
        for( var p : mergedLines ) {
            convertOneLineStringToTurtle(newTurtle,p);
        }
        return newTurtle;
    }

    private void convertOneLineStringToTurtle(Turtle newTurtle, LineString p) {
        boolean first = true;
        for( var c : p.getCoordinates() ) {
            if(first) {
                newTurtle.jumpTo(c.getX(), c.getY());
                first = false;
            } else {
                newTurtle.moveTo(c.getX(), c.getY());
            }
        }
    }

    private Turtle convertPathToTurtle(List<Point> path) {
        logger.debug("Converting path to turtle");
        var newTurtle = new Turtle();
        for (Point p0 : path) {
            LineString s = findMergedLine(p0);
            if(s==null) {
                logger.error("Could not find line for point {}",p0);
                continue;
            }
            convertOneLineStringToTurtle(newTurtle, s);
        }
        logger.debug("end");
        return newTurtle;
    }

    private LineString findMergedLine(Point p0) {
        for (LineString line : mergedLines) {
            Point start = line.getStartPoint();
            if(Double.compare(start.getX(), p0.getX())==0 && Double.compare(start.getY(), p0.getY())==0) {
                return line;
            }
        }
        return null;
    }

    // Method to convert the Eulerian path back into a detailed Turtle drawing
    private Turtle convertPathToTurtle2(List<LineString> eulerianLineStrings) {
        var newTurtle = new Turtle();

        // Start by jumping to the beginning of the first line
        LineString firstLine = eulerianLineStrings.get(0);
        Coordinate[] firstCoordinates = firstLine.getCoordinates();
        newTurtle.jumpTo(firstCoordinates[0].getX(), firstCoordinates[0].getY());
        newTurtle.penDown();

        // Traverse each LineString fully
        for (LineString line : eulerianLineStrings) {
            Coordinate[] coordinates = line.getCoordinates();
            for (Coordinate coord : coordinates) {
                newTurtle.moveTo(coord.getX(), coord.getY());
            }
        }

        newTurtle.penUp(); // Lift the pen at the end
        return newTurtle;
    }

    private void addLinesToJTS(Turtle turtle) {
        Coordinate previous = new Coordinate(0,0);
        for( var move : turtle.history ) {
            if(move.type == MovementType.DRAW_LINE) {
                Coordinate current = new Coordinate(move.x,move.y);
                addLine( geometryFactory.createLineString( new Coordinate[]{new Coordinate(previous),current}) );
            }
            previous.setX(move.x);
            previous.setY(move.y);
        }
    }

    // Add a line to the set
    public void addLine(LineString line) {
        this.lineSet.add(line);
    }

    // Find the best path
    public List<Point> findBestPath() {
        // Step 1: Merge lines where necessary to build the graph
        mergeLinesAndBuildGraph();
        addRetracesForOddVertices();
        // Step 2: Find Eulerian path or circuit
        return eulerianPath();
    }

    // Merge lines and build the graph of intersections
    private void mergeLinesAndBuildGraph() {
        try {
            LineMerger lineMerger = new LineMerger();
            lineMerger.add(this.lineSet);
            mergedLines.addAll(lineMerger.getMergedLineStrings());

            // Step 3: Add vertices (endpoints/intersections) to the graph
            for (LineString line : mergedLines) {
                Point start = line.getStartPoint();
                Point end = line.getEndPoint();
                graph.computeIfAbsent(start, k -> new ArrayList<>()).add(end);
                graph.computeIfAbsent(end, k -> new ArrayList<>()).add(start);
            }
        } catch (TopologyException e) {
            System.err.println("Error merging lines: " + e.getMessage());
        }
    }

    private void addRetracesForOddVertices() {
        // Step 1: Identify odd-degree vertices
        List<Point> oddVertices = new ArrayList<>();
        for (Point vertex : graph.keySet()) {
            if (graph.get(vertex).size() % 2 != 0) {
                oddVertices.add(vertex);
            }
        }

        // Step 2: If there are more than 2 odd vertices, add retraces until only 2 remain
        if (oddVertices.size() > 2) {
            while (oddVertices.size() > 2) {
                Point v1 = oddVertices.remove(0);
                Point bestMatch = null;
                double bestDistance = Double.MAX_VALUE;

                // Find the closest odd vertex to v1
                for (Point v2 : oddVertices) {
                    double distance = v1.distance(v2); // Calculate distance
                    if (distance < bestDistance) {
                        bestDistance = distance;
                        bestMatch = v2;
                    }
                }

                if (bestMatch != null) {
                    oddVertices.remove(bestMatch);

                    // Step 3: Add retrace (imaginary edge) to make the degrees even
                    graph.computeIfAbsent(v1, k -> new ArrayList<>()).add(bestMatch);
                    graph.computeIfAbsent(bestMatch, k -> new ArrayList<>()).add(v1);

                    System.out.println("Added retrace between: " + v1 + " and " + bestMatch);
                }
            }
        }

        // After this step, there should be exactly 2 odd-degree vertices, which will be the start and end points.
    }

    // Assuming graph is now Eulerian, we use Hierholzer's algorithm to find the path.
    private List<Point> eulerianPath() {
        Stack<Point> stack = new Stack<>();
        List<Point> path = new ArrayList<>();

        // Choose one of the odd-degree vertices
        stack.push(findStartVertex());

        while (!stack.isEmpty()) {
            Point current = stack.peek();
            var node = graph.get(current);
            if (node.isEmpty()) {
                // If no more edges, add to path
                path.add(current);
                stack.pop();
            } else {
                // Continue to traverse edges
                Point next = node.remove(0); // Get the next vertex
                graph.get(next).remove(current); // Remove the reverse edge as well
                stack.push(next);
            }
        }

        // The currentPath will contain the sequence of points for the Turtle to follow
        Collections.reverse(path);
        return path;
    }

    // Helper method to find a vertex with an odd degree to start (or any vertex if all are even)
    private Point findStartVertex() {
        for (Point vertex : graph.keySet()) {
            if (graph.get(vertex).size() % 2 != 0) {
                return vertex; // Start at an odd-degree vertex
            }
        }
        return graph.keySet().iterator().next(); // Default to any vertex if all are even
    }

    private boolean hasMultipleComponents() {
        Set<Point> visited = new HashSet<>();
        boolean foundFirstComponent = false;

        // go through all vertexes
        for (Point vertex : graph.keySet()) {
            // if we hit one that isn't visited (might be the first, might not)
            if (!visited.contains(vertex)) {
                if (foundFirstComponent) {
                    // Found a second island (unvisited area) so quit.
                    return true;
                }

                // Explore the first component
                exploreComponent(vertex, visited);
                foundFirstComponent = true;
            }
        }

        // If we finish and only found one component, return false
        return false;
    }

    // Depth-First Search to explore a connected component
    private void exploreComponent(Point start, Set<Point> visited) {
        Stack<Point> stack = new Stack<>();
        stack.push(start);

        while (!stack.isEmpty()) {
            Point vertex = stack.pop();
            if (!visited.contains(vertex)) {
                visited.add(vertex);
                for (Point neighbor : graph.get(vertex)) {
                    if (!visited.contains(neighbor)) {
                        stack.push(neighbor);
                    }
                }
            }
        }
    }

}
