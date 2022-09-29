import edu.uci.ics.jung.graph.UndirectedSparseGraph;

import edu.uci.ics.jung.graph.util.Pair;
import org.apache.commons.collections15.Transformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class DepthFirstSearch<V, E> {

    private UndirectedSparseGraph<V, E> graph;
    private Transformer<E, Boolean> transformer;

    private List<V> visited;
    private List<List<V>> clusters;

    public DepthFirstSearch(UndirectedSparseGraph<V, E> graph, Transformer<E, Boolean> transformer) {
        this.graph = graph;
        this.transformer = transformer;
        this.visited = new ArrayList<>();
        this.clusters = new ArrayList<>();
    }

    public List<List<V>> getClusters() {
        for (V vertex: graph.getVertices()) {
            if (!visited.contains(vertex)) {
                clusters.add(getCluster(vertex));
            }
        }
        return clusters;
    }

    private List<V> getCluster(V vertex) {
        List<V> cluster = new ArrayList<>();

        cluster.add(vertex);
        visited.add(vertex);

        Stack<V> stack = new Stack<V>();
        stack.push(vertex);
        while (!stack.empty()) {
            V v = stack.pop();

            for (E edge : graph.getIncidentEdges(v)) {
                Pair<V> pair = graph.getEndpoints(edge);
                V otherVertex = pair.getFirst().equals(v) ? pair.getSecond() : pair.getFirst();

                if (!visited.contains(otherVertex) && transformer.transform(edge)) {
                    cluster.add(otherVertex);
                    visited.add(otherVertex);

                    stack.push(otherVertex);
                }
            }
        }

        System.out.println("Printed cluster from vertex: " + vertex.toString());

        return cluster;
    }

    public int getNumberOfComponents() {
        int numberOfComponents = 0;

        for (V vertex: graph.getVertices()) {
            if (!visited.contains(vertex)) {
                getComponent(vertex);
                numberOfComponents++;
            }
        }

        return numberOfComponents;
    }

    private void getComponent(V vertex) {
        visited.add(vertex);

        Stack<V> stack = new Stack<V>();
        stack.push(vertex);
        while (!stack.empty()) {
            V v = stack.pop();

            for (E edge : graph.getIncidentEdges(v)) {
                Pair<V> pair = graph.getEndpoints(edge);
                V otherVertex = pair.getFirst().equals(v) ? pair.getSecond() : pair.getFirst();

                if (!visited.contains(otherVertex)) {
                    visited.add(otherVertex);

                    stack.push(otherVertex);
                }
            }
        }
    }
}
