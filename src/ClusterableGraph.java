import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.Pair;
import org.apache.commons.collections15.Transformer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class ClusterableGraph<V, E> {
    private UndirectedSparseGraph<V, E> graph;
    private Transformer<E, Boolean> relationTransformer;
    private Transformer<Pair<V>, E> edgeTransformer;
    public Transformer<Integer, V> vertexTransformer;

    private List<List<V>> clusters;
    private List<E> edgesToRemove;
    private boolean isClusterable;
    private List<Boolean> coalitionClusters;

    public ClusterableGraph(UndirectedSparseGraph<V, E> graph, Transformer<E, Boolean> relationTransformer, Transformer<Pair<V>, E> edgeTransformer,  Transformer<Integer, V> vertexTransformer) {
        this.graph = graph;
        this.relationTransformer = relationTransformer;
        this.edgeTransformer = edgeTransformer;
        this.vertexTransformer = vertexTransformer;
        this.isClusterable = true;
        this.edgesToRemove = new ArrayList<>();
        this.coalitionClusters = new ArrayList<>();
    }

    public List<E> getEdgesToRemove() {
        return edgesToRemove;
    }

    public boolean isClusterable() {
        return isClusterable;
    }

    public void clusterAlgorithm() {
        DepthFirstSearch<V, E> depthFirstSearch = new DepthFirstSearch<>(graph, relationTransformer);

        clusters = depthFirstSearch.getClusters();

        int k = 0;

        for (List<V> cluster: clusters) {
            coalitionClusters.add(k, true);

            for (int i = 0; i < cluster.size(); i++) {
                for (int j = i + 1; j < cluster.size(); j++) {
                    E edge = graph.findEdge(cluster.get(i), cluster.get(j));

                    if (edge != null && !relationTransformer.transform(edge)) {
                        isClusterable = false;
                        edgesToRemove.add(edge);
                        coalitionClusters.set(k, false);
                    }
                }
            }
            k++;
        }
    }

    public List<UndirectedSparseGraph<V, E>> getClusters(String type) {
        List<UndirectedSparseGraph<V, E>> graphClusters = new ArrayList<>();
        for (int i = 0; i < clusters.size(); i++) {
            if (type == null || type.equals("All") ||
                    (type.equals("Coalition") && coalitionClusters.get(i)) || (type.equals("Not Coalition") && !coalitionClusters.get(i))) {
                graphClusters.add(getClusterAsSubgraph(clusters.get(i)));
            }
        }

        return graphClusters;
    }

    private UndirectedSparseGraph<V, E> getClusterAsSubgraph(List<V> vertices) {
        UndirectedSparseGraph<V, E> subgraraph = new UndirectedSparseGraph<>();

        for (V vertex : vertices) {
            subgraraph.addVertex(vertex);
            Collection<E> incidentEdges = graph.getIncidentEdges(vertex);

            for (E edge : incidentEdges) {
                ArrayList<V> incidentVertices = new ArrayList<>(graph.getIncidentVertices(edge));
                if (vertices.contains(incidentVertices.get(0)) && vertices.contains(incidentVertices.get(1))) {
                    subgraraph.addEdge(edge, incidentVertices);
                }
            }
        }
        return subgraraph;
    }

    public UndirectedSparseGraph<V, E> getClusterNetworkAsGraph() {
        UndirectedSparseGraph<V, E> clusterGraph = new UndirectedSparseGraph<>();

        for (int i = 0; i < clusters.size(); i++) {
            clusterGraph.addVertex(vertexTransformer.transform(i));
        }

        for (int i = 0; i < clusters.size(); i++) {
            for (int j = 0; j < clusters.size(); j++) {
                if (areClustersConnected(clusters.get(i), clusters.get(j))) {
                    V from = vertexTransformer.transform(i);
                    V to = vertexTransformer.transform(j);
                    if (!from.equals(to)) {
                        Pair<V> vertices = new Pair<V>(from, to);
                        E edge = edgeTransformer.transform(vertices);
                        clusterGraph.addEdge(edge, from, to);
                    }
                }
            }
        }

        return clusterGraph;
    }

    private Boolean areClustersConnected(List<V> clusterA, List<V> clusterB) {
        for (V vertexA: clusterA) {
            for (V vertexB: clusterB) {
                if (graph.findEdge(vertexA, vertexB) != null) {
                    return true;
                }
            }
        }

        return false;
    }
}
