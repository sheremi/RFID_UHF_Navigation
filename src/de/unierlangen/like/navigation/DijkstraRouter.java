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

import com.better.wakelock.Logger;
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

    public Path findRoute(PointF currentPosition, PointF destination) {
        Vertex positionVertex = findClosestVertexTo(currentPosition);
        Vertex destinationVertex = findClosestVertexTo(destination);
        ArrayList<Vertex> route = findRouteInGraph(positionVertex, destinationVertex);
        // TODO Correct drawing of the path (sometimes it looks ugly (crooked
        // lines)
        // because of the drawing from the currentPosition, but not from a
        // vertex)
        Path path = convertRouteToPath(route);
        path.setLastPoint(currentPosition.x, currentPosition.y);
        return path;
    }

    private Vertex findClosestVertexTo(PointF point) {
        Vertex closestVertex = graph.getVertex("1");
        float shortestDist = Float.MAX_VALUE;
        {
            Logger.d("Search of the closest vertex to the point " + "{" + point.x + "; " + point.y
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
                Logger.d(sb.toString());
            }
            if (distToPoint < shortestDist) {
                shortestDist = distToPoint;
                closestVertex = vertex;
            }
        }
        {
            StringBuilder sb1 = new StringBuilder().append("ShortestDist: " + shortestDist + "; "
                    + "closestVertex: " + closestVertex.getId());
            Logger.d(sb1.toString());
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
            Logger.d(sb.toString());
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
                    Logger.d(sb.toString());
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

    private Path convertRouteToPath(ArrayList<Vertex> route) {
        Path path = new Path();
        Iterator<Vertex> iterator = route.iterator();
        if (iterator.hasNext()) {
            PointF startingPoint = (PointF) iterator.next().getProperty(KEY_COORDINATES);
            path.setLastPoint(startingPoint.x, startingPoint.y);
        }
        while (iterator.hasNext()) {
            PointF pointF = (PointF) iterator.next().getProperty(KEY_COORDINATES);
            path.lineTo(pointF.x, pointF.y);
        }
        return path;
    }

    /**
	 * 
	 */
    private void dumpGraph(boolean verbose) {
        Logger.d("Graph: ");
        for (Vertex vertex : graph.getVertices()) {
            StringBuilder sb = new StringBuilder();
            sb.append(" Vertex ").append(vertex.getId()).append("; ");
            sb.append(" status: ").append(vertex.getProperty(KEY_STATUS).toString()).append("; ");
            PointF vCoord = (PointF) vertex.getProperty(KEY_COORDINATES);
            sb.append(" coordinates: ").append("{" + vCoord.x + "; " + vCoord.y + "}");
            Logger.d(sb.toString());
            if (verbose) {
                Logger.d("  InEdges: ");
                dumpEdges(vertex.getInEdges(new String[0]));
                Logger.d("  OutEdges: ");
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
            Logger.d(sbEdge.toString());
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
            Logger.e("file with vertices vertices.txt is not found", e);
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
                        Logger.d("edgeNumber " + edgeNumber + " failed to get vertex");
                        if (edgeInVertex == null) {
                            Logger.d("edgeInVertex == null");
                        }
                        if (edgeOutVertex == null) {
                            Logger.d("edgeOutVertex == null");
                        }
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            Logger.e("file with edges edges.txt is not found", e);
        }
    }

    private Float calculateDistanceBetween(PointF point1, PointF point2) {
        Float dist = (float) Math.sqrt(Math.pow(point2.x - point1.x, 2)
                + Math.pow(point2.y - point1.y, 2));
        return dist;
    }
}