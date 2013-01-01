package de.unierlangen.like.navigation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

import android.graphics.Path;
import android.graphics.PointF;

import com.github.androidutils.logger.Logger;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraph;

public class DijkstraRouter {
    private static final String STATUS_VISITED = "visited";
    private static final String STATUS_UNVISITED = "unvisited";
    private static final String KEY_COORDINATES = "coordinates";
    private static final String KEY_STATUS = "status";
    private static final String KEY_DISTANCE = "dist";
    private final FileReader fileReader = new FileReader();
    private final Logger log = Logger.getDefaultLogger();

    private TinkerGraph graph;
    private PriorityQueue<Vertex> queue;

    /**
     * Just for testing
     */
    public DijkstraRouter() {
        graph = new TinkerGraph();
        queue = new PriorityQueue<Vertex>(10, new Comparator<Vertex>() {
            @Override
            public int compare(Vertex vertex1, Vertex vertex2) {
                if ((Float) vertex1.getProperty(KEY_DISTANCE) > (Float) vertex2
                        .getProperty(KEY_DISTANCE))
                    return 1;
                else
                    return -1;
            }
        });

        // Adding vertices and edges
        fillGraphWithVertices();
        for (Vertex vertex : graph.getVertices()) {
            vertex.setProperty(KEY_DISTANCE, Float.MAX_VALUE);
            vertex.setProperty(KEY_STATUS, STATUS_UNVISITED);
        }

        fillGraphWithEdges();

        {
            dumpGraph(true);
        }

    }

    public ArrayList<PointF> findRoute(PointF currentPosition, PointF destination) {
        Vertex positionVertex = findClosestVertexTo(currentPosition);
        Vertex destinationVertex = findClosestVertexTo(destination);
        ArrayList<Vertex> route = findRouteInGraph(positionVertex, destinationVertex);
        ArrayList<PointF> routeAsPoints = convertRouteToPoints(route);

        connectRouteToDestination(currentPosition, routeAsPoints);

        return routeAsPoints;
    }

    private void connectRouteToDestination(PointF currentPosition, ArrayList<PointF> routeAsPoints) {
        // We replace the last segment of the route with two lines
        // First line is a perpendicular from current position on the last
        // segment of the route
        // the second line is the rest of the last segment before the
        // intersection with the first line
        PointF p1 = routeAsPoints.get(routeAsPoints.size() - 1);
        PointF p2 = routeAsPoints.get(routeAsPoints.size() - 2);
        PointF crossing = new PointF();
        if (Math.abs(p1.x - p2.x) < 0.0001f) {
            // line is vertical, special case
            crossing.x = p1.x;
            crossing.y = currentPosition.y;
        } else if (Math.abs(p1.y - p2.y) < 0.0001f) {
            // line is horizontal
            crossing.y = p1.y;
            crossing.x = currentPosition.x;
        } else {
            // normal
            float k_line = (p1.y - p2.y) / (p1.x - p2.x);
            float b_line = p1.y - k_line * p1.x;

            // Now find perpendicular line
            float k_perp = -1.0f / k_line;
            float b_perp = currentPosition.y - k_perp * currentPosition.y;

            // Now find the intersection point
            crossing.x = (b_perp - b_line) / (k_line - k_perp);
            crossing.y = k_line * crossing.x + b_line;
        }

        // remove last point
        routeAsPoints.remove(routeAsPoints.size() - 1);
        routeAsPoints.add(crossing);
        routeAsPoints.add(currentPosition);
    }

    private Vertex findClosestVertexTo(PointF point) {
        Vertex closestVertex = graph.getVertex("1");
        float shortestDist = Float.MAX_VALUE;
        {
            log.d("Search of the closest vertex to the point " + "{" + point.x + "; " + point.y
                    + "} " + "was initiated.");
        }
        for (Vertex vertex : graph.getVertices()) {
            PointF vertexCoordinates = (PointF) vertex.getProperty(KEY_COORDINATES);
            StringBuilder sb = new StringBuilder();
            {
                sb.append("Vertex " + vertex.getId() + "; ");
                sb.append("vertexCoordinates: " + vertexCoordinates.x + "; " + vertexCoordinates.y
                        + "; ");
            }
            float distToPoint = (float) Math.sqrt(Math.pow(vertexCoordinates.x - point.x, 2)
                    + Math.pow(vertexCoordinates.y - point.y, 2));
            {
                sb.append(" distToPoint: " + distToPoint);
                log.d(sb.toString());
            }
            if (distToPoint < shortestDist) {
                shortestDist = distToPoint;
                closestVertex = vertex;
            }
        }
        {
            StringBuilder sb1 = new StringBuilder().append("ShortestDist: " + shortestDist + "; "
                    + "closestVertex: " + closestVertex.getId());
            log.d(sb1.toString());
        }
        return closestVertex;
    }

    /**
     * 
     * @param position
     * @param destination
     * @return
     */
    private ArrayList<Vertex> findRouteInGraph(Vertex position, Vertex destination) {
        calculateDistancesToAllVertices(position);
        ArrayList<Vertex> route = new ArrayList<Vertex>();
        // Add our destination to the route
        route.add(destination);
        addNextVertexToRoute(destination, route);
        // Display debug information to the Log
        {
            StringBuilder sb = new StringBuilder().append("Route: ");
            for (Vertex vertex : route) {
                sb.append(vertex.getId()).append(" ");
            }
            log.d(sb.toString());
        }
        return route;
    }

    /**
     * TODO
     * 
     * @param destination
     * @param route
     */
    private void addNextVertexToRoute(Vertex vertexToCheck, ArrayList<Vertex> route) {
        for (Edge edge : vertexToCheck.getInEdges(new String[0])) {
            float calculatedInVertexDist = (Float) vertexToCheck.getProperty(KEY_DISTANCE);
            float calculatedOutVertexDist = (Float) edge.getOutVertex().getProperty(KEY_DISTANCE);
            float calculatedEdgeDist = (Float) edge.getProperty(KEY_DISTANCE);
            // Check if sum of OutVertexDist and EdgeDist equals to InVertexDist
            if (Math.abs(calculatedOutVertexDist + calculatedEdgeDist - calculatedInVertexDist) < 0.0001f) {
                route.add(edge.getOutVertex());
                {
                    StringBuilder sb = new StringBuilder()
                            .append("to the route was added vertex: ");
                    sb.append(edge.getOutVertex().getId()).append(" ");
                    log.d(sb.toString());
                }
                if (calculatedOutVertexDist > 0.0001f) {
                    addNextVertexToRoute(edge.getOutVertex(), route);
                }
            }
        }
    }

    /**
     * Calculates distances to all vertices in the graph, starting with the
     * current position
     * 
     * @param position
     *            from which distances are calculated
     */
    private void calculateDistancesToAllVertices(Vertex position) {
        for (Vertex vertex : graph.getVertices()) {
            vertex.setProperty(KEY_DISTANCE, Float.MAX_VALUE);
            vertex.setProperty(KEY_STATUS, STATUS_UNVISITED);
        }
        position.setProperty(KEY_DISTANCE, 0.00f);
        queue.add(position);
        while (!queue.isEmpty()) {
            Vertex nextVertexToVisit = queue.poll();
            addAdjacentVerticesToQueue(nextVertexToVisit);
            nextVertexToVisit.setProperty(KEY_STATUS, STATUS_VISITED);
            {
                dumpGraph(false);
            }
        }
    }

    /**
     * Adds adjacent to the current vertex-position vertices to the queue
     * 
     * @param position
     */
    private void addAdjacentVerticesToQueue(Vertex position) {
        for (Edge edge : position.getInEdges(new String[0])) {
            if (edge.getOutVertex().getProperty(KEY_STATUS).equals(STATUS_UNVISITED)) {
                Float dist = (Float) position.getProperty(KEY_DISTANCE)
                        + (Float) edge.getProperty(KEY_DISTANCE);
                if ((Float) edge.getOutVertex().getProperty(KEY_DISTANCE) > dist) {
                    edge.getOutVertex().setProperty(KEY_DISTANCE, dist);
                }
                queue.add(edge.getOutVertex());
            }
        }
    }

    private ArrayList<PointF> convertRouteToPoints(ArrayList<Vertex> route) {
        ArrayList<PointF> points = new ArrayList<PointF>(route.size() + 3);
        for (Vertex vertex : route) {
            PointF pointF = (PointF) vertex.getProperty(KEY_COORDINATES);
            points.add(pointF);
        }
        return points;
    }

    public static Path convertPointsToPath(ArrayList<PointF> route) {
        Path path = new Path();
        Iterator<PointF> iterator = route.iterator();
        if (iterator.hasNext()) {
            PointF startingPoint = iterator.next();
            path.setLastPoint(startingPoint.x, startingPoint.y);
        }
        while (iterator.hasNext()) {
            PointF pointF = iterator.next();
            path.lineTo(pointF.x, pointF.y);
        }
        return path;
    }

    /**
	 * 
	 */
    private void dumpGraph(boolean verbose) {
        log.d("Graph: ");
        for (Vertex vertex : graph.getVertices()) {
            StringBuilder sb = new StringBuilder();
            sb.append(" Vertex ").append(vertex.getId()).append("; ");
            sb.append(" status: ").append(vertex.getProperty(KEY_STATUS).toString()).append("; ");
            PointF vCoord = (PointF) vertex.getProperty(KEY_COORDINATES);
            sb.append(" coordinates: ").append("{" + vCoord.x + "; " + vCoord.y + "}");
            log.d(sb.toString());
            if (verbose) {
                log.d("  InEdges: ");
                dumpEdges(vertex.getInEdges(new String[0]));
                log.d("  OutEdges: ");
                dumpEdges(vertex.getOutEdges(new String[0]));
            }
        }
    }

    /**
     * @param edges
     */
    private void dumpEdges(Iterable<Edge> edges) {
        for (Edge edge : edges) {
            StringBuilder sbEdge = new StringBuilder();
            sbEdge.append("   ").append(edge.getId()).append(" label ").append(edge.getLabel());
            sbEdge.append(" inV ").append(edge.getInVertex().getId());
            sbEdge.append(" outV ").append(edge.getOutVertex().getId()).append(" ");
            for (String key : edge.getPropertyKeys()) {
                sbEdge.append(key).append(" ").append(edge.getProperty(key).toString());
            }
            log.d(sbEdge.toString());
        }
    }

    private void fillGraphWithVertices() {
        try {
            String content = fileReader.getDataFromFile("/sdcard/like/vertices.txt");
            for (String entry : fileReader.splitStringContent(content)) {
                List<String> strings = Arrays.asList(entry.split(","));
                Iterator<String> iterator = strings.iterator();
                while (iterator.hasNext()) {
                    String vertexNumber = iterator.next();
                    float vertexCoordinateX = Float.parseFloat(iterator.next().trim());
                    float vertexCoordinateY = Float.parseFloat(iterator.next().trim());
                    PointF vertexCoordinates = new PointF(vertexCoordinateX, vertexCoordinateY);
                    graph.addVertex(vertexNumber).setProperty(KEY_COORDINATES, vertexCoordinates);
                }
            }
        } catch (IOException e) {
            log.e("file with vertices vertices.txt is not found", e);
        }
    }

    private void fillGraphWithEdges() {
        try {
            String content = fileReader.getDataFromFile("/sdcard/like/edges.txt");
            for (String entry : fileReader.splitStringContent(content)) {
                List<String> strings = Arrays.asList(entry.split(","));
                Iterator<String> iterator = strings.iterator();
                while (iterator.hasNext()) {
                    String edgeNumber = iterator.next();
                    Vertex edgeInVertex = graph.getVertex(iterator.next());
                    Vertex edgeOutVertex = graph.getVertex(iterator.next());
                    try {
                        Float edgeLength = calculateDistanceBetween(
                                (PointF) edgeInVertex.getProperty(KEY_COORDINATES),
                                (PointF) edgeOutVertex.getProperty(KEY_COORDINATES));
                        graph.addEdge(edgeNumber, edgeInVertex, edgeOutVertex, edgeNumber)
                                .setProperty(KEY_DISTANCE, edgeLength);
                        graph.addEdge(edgeNumber + "a", edgeOutVertex, edgeInVertex,
                                edgeNumber + "a").setProperty(KEY_DISTANCE, edgeLength);
                    } catch (NullPointerException e) {
                        log.d("edgeNumber " + edgeNumber + " failed to get vertex");
                        if (edgeInVertex == null) {
                            log.d("edgeInVertex == null");
                        }
                        if (edgeOutVertex == null) {
                            log.d("edgeOutVertex == null");
                        }
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            log.e("file with edges edges.txt is not found", e);
        }
    }

    private Float calculateDistanceBetween(PointF point1, PointF point2) {
        Float dist = (float) Math.sqrt(Math.pow(point2.x - point1.x, 2)
                + Math.pow(point2.y - point1.y, 2));
        return dist;
    }
}