import com.sun.istack.internal.NotNull;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.Pair;
import org.apache.commons.collections15.Transformer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class KCoreMain {

    public static UndirectedSparseGraph<Vertex, Edge> readGraph(String fileName) {
        UndirectedSparseGraph<Vertex, Edge> graph = new UndirectedSparseGraph<>();

        FileReader fileReader = null;
        BufferedReader bufferedReader = null;

        try {
            fileReader = new FileReader(fileName);
            bufferedReader = new BufferedReader(fileReader);

            String line = bufferedReader.readLine();

            while (line != null) {
                if (!line.startsWith("node") && !line.startsWith("from") && !line.startsWith("id")) {
                    String[] nodesString = line.split(",");
                    Vertex from = new Vertex(nodesString[0]);
                    Vertex to = new Vertex(nodesString[1]);

                    Edge edge = new Edge(from, to);

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
        Transformer<Vertex, Integer> integerTransformer = new Transformer<Vertex, Integer>() {
            @Override
            public Integer transform(Vertex vertex) {
                return Integer.parseInt(vertex.getValue());
            }
        };

        Transformer<Integer, Vertex> vertexTransformer = new Transformer<Integer, Vertex>() {
            @Override
            public Vertex transform(Integer integer) {
                return new Vertex(String.valueOf(integer));
            }
        };

        Transformer<Edge, Pair<Vertex>> edgeTransformer = new Transformer<Edge, Pair<Vertex>>() {
            @Override
            public Pair<Vertex> transform(Edge edge) {
                return new Pair<Vertex>(edge.getFromVertex(), edge.getToVertex());
            }
        };

        FileWriter fileWriter = null;
        BufferedWriter bufferedWriter = null;

        try {
            File outputFile = new File("export_test.txt");
            fileWriter = new FileWriter(outputFile);
            bufferedWriter = new BufferedWriter(fileWriter);

//            UndirectedSparseGraph<Vertex, Edge> graph = readGraph("test_graph_3.csv");
//            UndirectedSparseGraph<Vertex, Edge> graph = readGraph("facebook.csv");
//            UndirectedSparseGraph<Vertex, Edge> graph = readGraph("deezer.csv");
            UndirectedSparseGraph<Vertex, Edge> graph = readGraph("twitch.csv");
//            UndirectedSparseGraph<Vertex, Edge> graph = readGraph("CorePeripheryGraph.txt");

            KCoreGraph<Vertex, Edge> kCoreGraph = new KCoreGraph<>(graph, integerTransformer, vertexTransformer, edgeTransformer);

            kCoreGraph.batageljZaversnikAlgorithm();

            int maxCore = kCoreGraph.getMaxCore();

            if (kCoreGraph.straightforwardAlgorithm()) {
                bufferedWriter.write("K-Core decomposition tested successfully when comparing to the straightforward algorithm");
            } else {
                bufferedWriter.write("K-Core decomposition test failed when comparing to the straightforward algorithm");
            }

            bufferedWriter.write(System.lineSeparator() + "-----------------------------------------------------------------------------------------------" + System.lineSeparator());

            bufferedWriter.write("Loaded graph has " + maxCore + " cores" + System.lineSeparator());

            for (int i = 1; i <= maxCore; i++) {
                UndirectedSparseGraph<Vertex, Edge> kCore = kCoreGraph.getKCoreNetwork(i);

                bufferedWriter.write("# Core #" + i + ", Nodes: " + kCore.getVertices() + System.lineSeparator());
                for (Edge edge : kCore.getEdges()) {
                    bufferedWriter.write(edge.toString() + System.lineSeparator());
                }
            }

//            BIG GRAPH SOLUTION
            kCoreGraph.updateMetricsCSV("gephi_twitch.csv", "metrics_twitch.csv");
//            kCoreGraph.updateMetricsCSV("gephi_facebook.csv", "metrics_facebook.csv");
//            kCoreGraph.updateMetricsCSV("gephi_deezer.csv", "metrics_deezer.csv");

//            SMALL GRAPH SOLUTION
//            bufferedWriter.write("-----------------------------------------------------------------------------------------------" + System.lineSeparator());
//
//            bufferedWriter.write("Graph metrics per vertex are as following (vertex, degree, shell index, betweenness centrality, " +
//                    "closeness centrality, eigenvector centrality:" + System.lineSeparator());
//            HashMap<Vertex, ArrayList<Double>> metrics = kCoreGraph.getVertexMetrics();
//
//            for(Vertex vertex: metrics.keySet()) {
//                bufferedWriter.write(vertex.toString() + ":\tdeg = " + String.format("%.0f", metrics.get(vertex).get(0)) + ",\tshell = " +
//                        String.format("%.0f", metrics.get(vertex).get(1)) + ",\tbc = " + String.format("%.2f", metrics.get(vertex).get(2)) + ",\tcc = " +
//                        String.format("%.2f", metrics.get(vertex).get(3)) + ",\tec = " + String.format("%.2f", metrics.get(vertex).get(4)) + System.lineSeparator());
//            }

            bufferedWriter.write("-----------------------------------------------------------------------------------------------" + System.lineSeparator());

            bufferedWriter.write("Spearman correalation = " + kCoreGraph.getSpearmanCorrelation() + System.lineSeparator());

            bufferedWriter.write("-----------------------------------------------------------------------------------------------" + System.lineSeparator());

            bufferedWriter.write("Number of nodes per core is:" + System.lineSeparator());
            ArrayList<Integer> numOfNodesPerCore = kCoreGraph.getNumOfNodesPerCore();
            for (int i = 0; i < numOfNodesPerCore.size(); i++) {
                bufferedWriter.write("Core " + (i + 1) + ": " + numOfNodesPerCore.get(i) + System.lineSeparator());
            }

            bufferedWriter.write("-----------------------------------------------------------------------------------------------" + System.lineSeparator());

            bufferedWriter.write("Number of links per core is:" + System.lineSeparator());
            ArrayList<Integer> numOfLinksPerCore = kCoreGraph.getNumOfLinksPerCore();
            for (int i = 0; i < numOfLinksPerCore.size(); i++) {
                bufferedWriter.write("Core " + (i + 1) + ": " + numOfLinksPerCore.get(i) + System.lineSeparator());
            }

            bufferedWriter.write("-----------------------------------------------------------------------------------------------" + System.lineSeparator());

            bufferedWriter.write("Density per core is:" + System.lineSeparator());
            ArrayList<Double> densityPerCore = kCoreGraph.getDensityPerCore();
            for (int i = 0; i < densityPerCore.size(); i++) {
                bufferedWriter.write("Core " + (i + 1) + ": " + String.format("%.2f", densityPerCore.get(i)) + System.lineSeparator());
            }

            bufferedWriter.write("-----------------------------------------------------------------------------------------------" + System.lineSeparator());

            bufferedWriter.write("Number of connected components per core is:" + System.lineSeparator());
            ArrayList<Integer> numOfConnectedComponentsPerCore = kCoreGraph.getConnectedComponentsPerCore();
            for (int i = 0; i < numOfConnectedComponentsPerCore.size(); i++) {
                bufferedWriter.write("Core " + (i + 1) + ": " + numOfConnectedComponentsPerCore.get(i) + System.lineSeparator());
            }

            bufferedWriter.write("-----------------------------------------------------------------------------------------------" + System.lineSeparator());

            ArrayList<Double> percentInBiggestCore = kCoreGraph.getPercentOfNodesAndLinksInBiggestCore();

            bufferedWriter.write("The biggest core contains " + String.format("%.2f", percentInBiggestCore.get(0) * 100) + "% of total graph vertices and "
                    + String.format("%.2f", percentInBiggestCore.get(1) * 100) + "% of total graph edges." + System.lineSeparator());

            bufferedWriter.write("-----------------------------------------------------------------------------------------------" + System.lineSeparator());

            bufferedWriter.write("Small world coefficients of the biggest core: " + kCoreGraph.getSmallWorldCoefficientInBiggestCore());


            bufferedWriter.write(System.lineSeparator() + "-----------------------------------------------------------------------------------------------" + System.lineSeparator());

            bufferedWriter.write("The diameter of the biggest component is " + String.format("%.0f", kCoreGraph.getMaxComponentDiameter()) + System.lineSeparator());

            bufferedWriter.write("-----------------------------------------------------------------------------------------------" + System.lineSeparator());

            bufferedWriter.write("The clustering coefficients in the biggest core per vertex are as following: " + System.lineSeparator());
            Map<Vertex, Double> ccMap = kCoreGraph.getClusteringCoefficientInBiggestCore();
            Iterator<Vertex> iterator = kCoreGraph.getKCoreNetwork(maxCore).getVertices().iterator();
            while (iterator.hasNext()) {
                Vertex vertex = iterator.next();
                bufferedWriter.write(integerTransformer.transform(vertex) + ": " + String.format("%.4f", ccMap.get(vertex)) + System.lineSeparator());
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
