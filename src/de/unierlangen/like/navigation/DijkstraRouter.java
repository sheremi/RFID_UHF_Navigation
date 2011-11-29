package de.unierlangen.like.navigation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;

import android.graphics.Path;
import android.graphics.PointF;
import android.util.Log;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraph;

public class DijkstraRouter {
	private static final boolean DBG = true;
	private static final String TAG = "DijkstraRouter";
	private static final String STATUS_VISITED = "visited";
	private static final String KEY_COORDINATES = "coordinates";
	private static final String KEY_STATUS = "status";
	private static final String STATUS_UNVISITED = "unvisited";
	private static final String KEY_DISTANCE = "dist";

	private TinkerGraph graph;
	private PriorityQueue<Vertex> queue;
	
	/**
	 * Just for testing
	 */
	public DijkstraRouter() {
		graph = new TinkerGraph();
		queue = new PriorityQueue<Vertex>(10, new Comparator<Vertex>() {
			public int compare(Vertex vertex1, Vertex vertex2) {
				if ((Float)vertex1.getProperty(KEY_DISTANCE)>(Float)vertex2.getProperty(KEY_DISTANCE)){
					return 1;
				} else {
					return -1;
				}
			}
		});
		// adding coordinates
		graph.addVertex("1").setProperty(KEY_COORDINATES, new PointF(31.98f,2.72f));
		graph.addVertex("2").setProperty(KEY_COORDINATES, new PointF(31.98f,6.58f));
		graph.addVertex("3").setProperty(KEY_COORDINATES, new PointF(34.24f,6.58f));
		graph.addVertex("4").setProperty(KEY_COORDINATES, new PointF(41.5f,6.58f));
		graph.addVertex("5").setProperty(KEY_COORDINATES, new PointF(41.5f,-1.81f));
		graph.addVertex("6").setProperty(KEY_COORDINATES, new PointF(45.59f,6.58f));
		graph.addVertex("7").setProperty(KEY_COORDINATES, new PointF(45.59f,2.72f));
		graph.getVertex("1").setProperty(KEY_DISTANCE, Float.MAX_VALUE);
		graph.getVertex("2").setProperty(KEY_DISTANCE, Float.MAX_VALUE);
		graph.getVertex("3").setProperty(KEY_DISTANCE, Float.MAX_VALUE);
		graph.getVertex("4").setProperty(KEY_DISTANCE, Float.MAX_VALUE);
		graph.getVertex("5").setProperty(KEY_DISTANCE, Float.MAX_VALUE);
		graph.getVertex("6").setProperty(KEY_DISTANCE, Float.MAX_VALUE);
		graph.getVertex("7").setProperty(KEY_DISTANCE, Float.MAX_VALUE);
		graph.getVertex("1").setProperty(KEY_STATUS, STATUS_UNVISITED);
		graph.getVertex("2").setProperty(KEY_STATUS, STATUS_UNVISITED);
		graph.getVertex("3").setProperty(KEY_STATUS, STATUS_UNVISITED);
		graph.getVertex("4").setProperty(KEY_STATUS, STATUS_UNVISITED);
		graph.getVertex("5").setProperty(KEY_STATUS, STATUS_UNVISITED);
		graph.getVertex("6").setProperty(KEY_STATUS, STATUS_UNVISITED);
		graph.getVertex("7").setProperty(KEY_STATUS, STATUS_UNVISITED);

		graph.addEdge("1", graph.getVertex("1"), graph.getVertex("2"), "1").setProperty(KEY_DISTANCE, 3.86f);
		graph.addEdge("1a", graph.getVertex("2"), graph.getVertex("1"), "1a").setProperty(KEY_DISTANCE, 3.86f);
		graph.addEdge("2", graph.getVertex("2"), graph.getVertex("3"), "2").setProperty(KEY_DISTANCE, 2.27f);
		graph.addEdge("2a", graph.getVertex("3"), graph.getVertex("2"), "2a").setProperty(KEY_DISTANCE, 2.27f);
		graph.addEdge("3", graph.getVertex("3"), graph.getVertex("4"), "3").setProperty(KEY_DISTANCE, 7.26f);
		graph.addEdge("3a", graph.getVertex("4"), graph.getVertex("3"), "3a").setProperty(KEY_DISTANCE, 7.26f);
		graph.addEdge("4", graph.getVertex("4"), graph.getVertex("5"), "4").setProperty(KEY_DISTANCE, 8.39f);
		graph.addEdge("4a", graph.getVertex("5"), graph.getVertex("4"), "4a").setProperty(KEY_DISTANCE, 8.39f);
		graph.addEdge("5", graph.getVertex("4"), graph.getVertex("6"), "5").setProperty(KEY_DISTANCE, 4.08f);
		graph.addEdge("5a", graph.getVertex("6"), graph.getVertex("4"), "5a").setProperty(KEY_DISTANCE, 4.08f);
		graph.addEdge("6", graph.getVertex("6"), graph.getVertex("7"), "6").setProperty(KEY_DISTANCE, 3.86f);
		graph.addEdge("6a", graph.getVertex("7"), graph.getVertex("6"), "6a").setProperty(KEY_DISTANCE, 3.86f);

		if (DBG) {
			dumpGraph(true);
		}

	}

	public Path findRoute(PointF currentPosition, PointF destination) {
		Vertex positionVertex = findClosestVertexToPosition();
		Vertex destinationVertex = findClosestVertexToDestination();
		ArrayList<Vertex> route = findRouteInGraph(positionVertex, destinationVertex);
		return convertRouteToPath(route);
	}
	
	// TODO Replace to one method findClosestVertexTo(PointF point)
	private Vertex findClosestVertexToPosition(){
		Vertex vertex = graph.getVertex("1");
		return vertex;
	}
	private Vertex findClosestVertexToDestination(){
		Vertex vertex = graph.getVertex("7");
		return vertex;
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
		if (DBG) {
			StringBuilder sb = new StringBuilder().append("route: ");
			for (Vertex vertex: route) {
				sb.append(vertex.getId()).append(" ");
			}
			Log.d(TAG, sb.toString());
		}
		return route;
	}

	/**
	 * TODO
	 * @param destination
	 * @param route
	 */
	private void addNextVertexToRoute(Vertex vertexToCheck, ArrayList<Vertex> route) {
		for (Edge edge : vertexToCheck.getInEdges(new String[0])){
			float calculatedInVertexDist = (Float) vertexToCheck.getProperty(KEY_DISTANCE);
			float calculatedOutVertexDist = (Float)edge.getOutVertex().getProperty(KEY_DISTANCE);
			float calculatedEdgeDist = (Float)edge.getProperty(KEY_DISTANCE);
			// Check if sum of OutVertexDist and EdgeDist equals to InVertexDist
			if (Math.abs((calculatedOutVertexDist + calculatedEdgeDist) - calculatedInVertexDist) < 0.0001f){
				route.add(edge.getOutVertex());
				if (DBG) {
					StringBuilder sb = new StringBuilder().append("to the route added vertex: ");
					sb.append(edge.getOutVertex().getId()).append(" ");
					Log.d(TAG, sb.toString());
				}
				if (calculatedOutVertexDist > 0.0001f) {
					addNextVertexToRoute(edge.getOutVertex(), route);
				}
			}
		}
	}

	/**
	 * Calculates distances to all vertices in the graph, starting with current position
	 * @param position from which distances are calculated
	 */
	private void calculateDistancesToAllVertices(Vertex position) {
		//TODO don't we have to clean the graph first?
		position.setProperty(KEY_DISTANCE, 0.00f);
		queue.add(position);
		while (!queue.isEmpty()) {
			Vertex nextVertexToVisit = queue.poll();
			addAdjacentVerticesToQueue(nextVertexToVisit);
			nextVertexToVisit.setProperty(KEY_STATUS, STATUS_VISITED);
			if (DBG) {
				dumpGraph(false);
			}
		}
	}
	
	/**
	 * Adds adjacent to the current vertex-position vertices to the queue
	 * @param position
	 */
	private void addAdjacentVerticesToQueue(Vertex position){
		for (Edge edge : position.getInEdges(new String[0])) {
			if (edge.getOutVertex().getProperty(KEY_STATUS).equals(STATUS_UNVISITED)) {
				Float dist = (Float)position.getProperty(KEY_DISTANCE) + (Float)edge.getProperty(KEY_DISTANCE);
				if ((Float)edge.getOutVertex().getProperty(KEY_DISTANCE)>dist){
					edge.getOutVertex().setProperty(KEY_DISTANCE, dist);
				}
				queue.add(edge.getOutVertex());
			}
		}
	}
	
	private Path convertRouteToPath(ArrayList<Vertex> route){
		Path path = new Path();
		Iterator<Vertex> iterator = route.iterator();
		if (iterator.hasNext()) {
			PointF startingPoint = (PointF) iterator.next().getProperty(KEY_COORDINATES);
			path.setLastPoint(startingPoint.x, startingPoint.y);
		}
		while (iterator.hasNext()) {
			PointF pointF = (PointF)iterator.next().getProperty(KEY_COORDINATES);
			path.lineTo(pointF.x, pointF.y);
		}
		return path;
	}

	/**
	 * 
	 */
	private void dumpGraph(boolean verbose) {
		Log.d(TAG, "Graph: ");
		for (Vertex vertex: graph.getVertices()){
			StringBuilder sb = new StringBuilder();
			sb.append(" Vertex ").append(vertex.getId()).append(" ");
			for (String key : vertex.getPropertyKeys()) {
				sb.append(key).append(" ").append(vertex.getProperty(key).toString()).append(" ");
			}
			Log.d(TAG, sb.toString());
			if (verbose) {
				Log.d (TAG, "  InEdges: ");
				dumpEdges(vertex.getInEdges(new String[0]));
				Log.d (TAG, "  OutEdges: ");
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
			Log.d (TAG, sbEdge.toString());
		}
	}
}