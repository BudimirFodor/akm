import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.Pair;
import org.apache.commons.collections15.Transformer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ClusterMain {

    public static UndirectedSparseGraph<Vertex, EdgeRel> readGraph(String fileName, String delimiter) {
        UndirectedSparseGraph<Vertex, EdgeRel> graph = new UndirectedSparseGraph<>();

        FileReader fileReader = null;
        BufferedReader bufferedReader = null;

        try {
            fileReader = new FileReader(fileName);
            bufferedReader = new BufferedReader(fileReader);

            String line = bufferedReader.readLine();

            while (line != null) {
                if (!line.startsWith("id") && !line.startsWith("#") && !line.startsWith("from")) {
                    String[] nodesString = line.split(delimiter);
                    Vertex from = new Vertex(nodesString[0]);
                    Vertex to = new Vertex(nodesString[1]);
                    boolean relation = Integer.parseInt(nodesString[2]) >= 0;


                    EdgeRel edge = new EdgeRel(relation, from, to);

                    if (graph.findEdge(from, to) == null && !from.equals(to)) {
                        graph.addEdge(edge, from, to);
                    }

                }
                line = bufferedReader.readLine();
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                    fileReader.close();
                } catch (IOException ex){
                    ex.printStackTrace();
                }
            }
        }

        return graph;
    }

    public static void main(String[] args) {
        Transformer<EdgeRel, Boolean> relationTransformer = new Transformer<EdgeRel, Boolean>() {
            @Override
            public Boolean transform(EdgeRel edge) {
                return edge.isRelation();
            }
        };

        Transformer<Pair<Vertex>, EdgeRel> edgeTransformer = new Transformer<Pair<Vertex>, EdgeRel>() {
            @Override
            public EdgeRel transform(Pair<Vertex> vertices) {
                return new EdgeRel(true, vertices.getFirst(), vertices.getSecond());
            }
        };

        Transformer<Integer, Vertex> vertexTransformer = new Transformer<Integer, Vertex>() {
            @Override
            public Vertex transform(Integer integer) {
                return new Vertex(String.valueOf(integer));
            }
        };

        FileWriter fileWriter = null;
        BufferedWriter bufferedWriter = null;

        try {
            File outputFile = new File("cluster_analysis_big_bitcoin.txt");
            fileWriter = new FileWriter(outputFile);
            bufferedWriter = new BufferedWriter(fileWriter);

//            UndirectedSparseGraph<Vertex, EdgeRel> graph = readGraph("clusterable_test_1.csv", ",");
            UndirectedSparseGraph<Vertex, EdgeRel> graph = readGraph("bitcoin.csv", ",");
//            UndirectedSparseGraph<Vertex, EdgeRel> graph = readGraph("slashdot.txt", "\t");
//            UndirectedSparseGraph<Vertex, EdgeRel> graph = readGraph("epinions.txt", "\t");
//            UndirectedSparseGraph<Vertex, EdgeRel> graph = readGraph("SBMGraph.txt", ",");


            ClusterableGraph<Vertex, EdgeRel> clusterableGraph = new ClusterableGraph<>(graph, relationTransformer, edgeTransformer, vertexTransformer);

            clusterableGraph.clusterAlgorithm();

            if (clusterableGraph.isClusterable()) {
                bufferedWriter.write("Loaded graph is clusterable" + System.lineSeparator());
            } else {
                bufferedWriter.write("Loaded graph is not clusterable" + System.lineSeparator());
                List<EdgeRel> edgesToRemove = clusterableGraph.getEdgesToRemove();
                bufferedWriter.write("-----------------------------------------------------------------------------------------------" + System.lineSeparator());
                bufferedWriter.write("In order for the graph to be clusterable, the following " + edgesToRemove.size() + " branches need to be removed:" + System.lineSeparator());

                for (EdgeRel edge: edgesToRemove) {
                    bufferedWriter.write(edge.toString() + System.lineSeparator());
                }
            }

            List<UndirectedSparseGraph<Vertex, EdgeRel>> clusters = clusterableGraph.getClusters("All");

            bufferedWriter.write("-----------------------------------------------------------------------------------------------" + System.lineSeparator());
            bufferedWriter.write("Printing all clusters" + System.lineSeparator());
            for (UndirectedSparseGraph<Vertex, EdgeRel> cluster: clusters) {
                bufferedWriter.write("# Cluster #" + clusters.indexOf(cluster) + ", Nodes: " + cluster.getVertices() + " Edges: " + cluster.getEdgeCount() + System.lineSeparator());
                for (EdgeRel edge: cluster.getEdges()) {
                    bufferedWriter.write(edge.toString() + System.lineSeparator());
                }
            }

            bufferedWriter.write("-----------------------------------------------------------------------------------------------" + System.lineSeparator());
            if (clusterableGraph.isClusterable()) {
                bufferedWriter.write("As the graph is clustreable, all the clusters are coalitions." + System.lineSeparator());
            } else {
                clusters = clusterableGraph.getClusters("Coalition");

                bufferedWriter.write("Printing clusters which are coalitions" + System.lineSeparator());
                for (UndirectedSparseGraph<Vertex, EdgeRel> cluster: clusters) {
                    bufferedWriter.write("# Coalition Cluster #" + clusters.indexOf(cluster) + ", Nodes: " + cluster.getVertices() + " Edges: " + cluster.getEdgeCount() + System.lineSeparator());
                    for (EdgeRel edge: cluster.getEdges()) {
                        bufferedWriter.write(edge.toString() + System.lineSeparator());
                    }
                }

                clusters = clusterableGraph.getClusters("Not Coalition");

                bufferedWriter.write("-----------------------------------------------------------------------------------------------" + System.lineSeparator());
                bufferedWriter.write("Printing clusters which are not coalitions." + System.lineSeparator());
                for (UndirectedSparseGraph<Vertex, EdgeRel> cluster: clusters) {
                    bufferedWriter.write("# Not Coalition Cluster #" + clusters.indexOf(cluster) + ", Nodes: " + cluster.getVertices() + " Edges: " + cluster.getEdgeCount() + System.lineSeparator());
                    for (EdgeRel edge: cluster.getEdges()) {
                        bufferedWriter.write(edge.toString() + System.lineSeparator());
                    }
                }
            }

            UndirectedSparseGraph<Vertex, EdgeRel> clusterNetwork = clusterableGraph.getClusterNetworkAsGraph();

            bufferedWriter.write("-----------------------------------------------------------------------------------------------" + System.lineSeparator());
            bufferedWriter.write("Printing the cluster network:" + System.lineSeparator());
            bufferedWriter.write("# Nodes: " + clusterNetwork.getVertices() + " Edges: " + clusterNetwork.getEdgeCount() + System.lineSeparator());
            for (EdgeRel edge: clusterNetwork.getEdges()) {
                bufferedWriter.write(edge.toString() + System.lineSeparator());
            }
            bufferedWriter.write("-----------------------------------------------------------------------------------------------" + System.lineSeparator());
            bufferedWriter.write("Finished successfully.");

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                    fileWriter.close();
                } catch (IOException ex){
                    ex.printStackTrace();
                }
            }
        }
    }
}
