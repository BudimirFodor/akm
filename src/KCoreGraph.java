import edu.uci.ics.jung.algorithms.metrics.Metrics;
import edu.uci.ics.jung.algorithms.scoring.BetweennessCentrality;
import edu.uci.ics.jung.algorithms.scoring.ClosenessCentrality;
import edu.uci.ics.jung.algorithms.scoring.EigenvectorCentrality;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraDistance;
import edu.uci.ics.jung.algorithms.shortestpath.DistanceStatistics;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.Pair;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class KCoreGraph<V, E> {
    private UndirectedSparseGraph<V, E> graph;
    private List<Integer> degrees;
    private List<List<V>> degreeSets;
    private Transformer<V, Integer> integerTransformer;
    private Transformer<Integer, V> vertexTransformer;
    private Transformer<E, Pair<V>> edgeTransformer;

    private List<Integer> shellIndexes;

    public KCoreGraph (UndirectedSparseGraph<V, E> graph, Transformer<V, Integer> integerTransformer, Transformer<Integer, V> vertexTransformer, Transformer<E, Pair<V>> edgeTransformer) {
        this.graph = graph;
        this.integerTransformer = integerTransformer;
        this.vertexTransformer = vertexTransformer;
        this.edgeTransformer = edgeTransformer;
        this.degrees = new ArrayList<>();
        this.degreeSets = new ArrayList<>();
        this.shellIndexes = new ArrayList<>();
    }

    public List<Integer> getShellIndexes() {
        return shellIndexes;
    }

    public void batageljZaversnikAlgorithm() {
        int maxDegree = getMaxDegree();

        List<V> vertices = new ArrayList<>(graph.getVertices());

        for (int i = 0; i <= maxDegree; i++) {
            List<V> degreeSet = new ArrayList<>();
            degreeSets.add(degreeSet);
        }

        for (int i = 0; i < graph.getVertexCount(); i++) {
            degrees.add(0);
            shellIndexes.add(0);
        }

        for (V vertex: vertices) {
            int vertexDegree = graph.degree(vertex);
            degrees.set(integerTransformer.transform(vertex), vertexDegree);
            degreeSets.get(vertexDegree).add(vertex);
        }

        for (int k = 0; k < maxDegree; k++) {
            while(degreeSets.get(k).size() > 0) {
                Random rand = new Random();
                int vertexIndex = rand.nextInt(degreeSets.get(k).size());
                V vertex = degreeSets.get(k).get(vertexIndex);
                Collection<E> incidentEdges = graph.getIncidentEdges(vertex);

                for (E edge : incidentEdges) {
                    Pair<V> pair = graph.getEndpoints(edge);
                    V otherVertex = pair.getFirst().equals(vertex) ? pair.getSecond() : pair.getFirst();
                    int otherVertexValue = integerTransformer.transform(otherVertex);

                    int otherVertexDegree = degrees.get(otherVertexValue);
                    if (otherVertexDegree > k) {
                        degreeSets.get(otherVertexDegree).remove(otherVertex);
                        degrees.set(otherVertexValue, otherVertexDegree - 1);
                        otherVertexDegree--;
                        degreeSets.get(otherVertexDegree).add(otherVertex);
                    }
                }

                degreeSets.get(k).remove(vertex);
                shellIndexes.set(integerTransformer.transform(vertex), k);
            }
        }
    }

    public UndirectedSparseGraph<V, E> getKCoreNetwork(int k) {
        if (k > getMaxDegree()) {
            return null;
        }

        UndirectedSparseGraph<V, E> kCoreGraph = new UndirectedSparseGraph<>();

        List<V> kCoreNodes = new ArrayList<>();

        for (int i = 0; i < graph.getVertexCount(); i++) {
            if (shellIndexes.get(i) == k) {
                kCoreNodes.add(vertexTransformer.transform(i));
                kCoreGraph.addVertex(vertexTransformer.transform(i));
            }
        }

        for (int i = 0; i < kCoreNodes.size(); i++) {
            for (int j = i + 1; j < kCoreNodes.size(); j++) {
                E kCoreEdge = graph.findEdge(kCoreNodes.get(i), kCoreNodes.get(j));
                if (kCoreEdge != null) {
                    kCoreGraph.addEdge(kCoreEdge, kCoreNodes.get(i), kCoreNodes.get(j));
                }
            }
        }

        return kCoreGraph;
    }

    public boolean straightforwardAlgorithm() {
        UndirectedSparseGraph<V, E> testGraph = cloneGraph(graph);
        int maxDegree = getMaxDegree();

        int shellCounter = 1;

        ArrayList<Integer> testIndexes = new ArrayList<>();

        for (int i = 0; i < testGraph.getVertexCount(); i++) {
            testIndexes.add(0);
        }

        while (shellCounter <= maxDegree) {
            boolean changed = false;

            List<V> verticesToRemove = new ArrayList<>();
            List<E> edgesToRemove = new ArrayList<>();
            for (V vertex: testGraph.getVertices()) {
                if (testGraph.degree(vertex) <= shellCounter) {
                    changed = true;
                    verticesToRemove.add(vertex);
                    testIndexes.set(integerTransformer.transform(vertex), shellCounter);
                    edgesToRemove.addAll(testGraph.getIncidentEdges(vertex));
                }
            }

            if (!changed) {
                shellCounter++;
            } else {
                for (V vertex: verticesToRemove) {
                    testGraph.removeVertex(vertex);
                }
                for (E edge: edgesToRemove) {
                    testGraph.removeEdge(edge);
                }
            }
        }

        return testIndexes.equals(shellIndexes);
    }

    private UndirectedSparseGraph<V, E> cloneGraph(UndirectedSparseGraph<V,E> original) {
        UndirectedSparseGraph<V, E> clone = new UndirectedSparseGraph<>();

        for (V vertex: original.getVertices()) {
            clone.addVertex(vertex);
        }
        for (E edge: original.getEdges()) {
            original.getIncidentVertices(edge);
            Pair<V> vertices = edgeTransformer.transform(edge);
            clone.addEdge(edge, vertices.getFirst(), vertices.getSecond());
        }

        return clone;
    }

    // NOTE: Big graph solution - Adding node shell index to a csv of centrality data exported from Gephi
    public void updateMetricsCSV(String inFile, String outFile) {
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;

        ArrayList<String> readLines = new ArrayList<>();
        try {
            fileReader = new FileReader(inFile);
            bufferedReader = new BufferedReader(fileReader);

            String line = bufferedReader.readLine();

            while (line != null) {
                readLines.add(line);
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

        FileWriter fileWriter = null;
        BufferedWriter bufferedWriter = null;

        try {
            File outputFile = new File(outFile);
            fileWriter = new FileWriter(outputFile);
            bufferedWriter = new BufferedWriter(fileWriter);

            for (String line: readLines) {
                String id = line.split(",")[0];
                if (id.equals("Id")) {
                    bufferedWriter.write(line + ",shellIndex" + System.lineSeparator());
                } else {
                    bufferedWriter.write(line + "," + shellIndexes.get(Integer.parseInt(id)) + System.lineSeparator());
                }
            }

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

    // NOTE: Small graph solution
    public HashMap<V, ArrayList<Double>> getVertexMetrics() {
        HashMap<V, ArrayList<Double>> metrics = new HashMap<>();
        List<V> vertices = new ArrayList<>(graph.getVertices());
        BetweennessCentrality<V, E> betweennessCentrality = new BetweennessCentrality<V, E>(graph);
        ClosenessCentrality<V, E> closenessCentrality = new ClosenessCentrality<V, E>(graph);
        EigenvectorCentrality<V, E> eigenvectorCentrality = new EigenvectorCentrality<V, E>(graph);

        for (int i = 0; i < vertices.size(); i++) {
            int vertexPosition = integerTransformer.transform(vertices.get(i));
            ArrayList<Double> values = new ArrayList<>();

            values.add(Double.valueOf(degrees.get(vertexPosition)));
            values.add(Double.valueOf(shellIndexes.get(vertexPosition)));
            values.add(betweennessCentrality.getVertexScore(vertices.get(vertexPosition)));
            values.add(closenessCentrality.getVertexScore(vertices.get(vertexPosition)));
            values.add(eigenvectorCentrality.getVertexScore(vertices.get(vertexPosition)));

            metrics.put(vertices.get(i), values);
        }

        return metrics;
    }

    public double getSpearmanCorrelation() {
        SpearmansCorrelation spearmansCorrelation = new SpearmansCorrelation();
        int edgeCount = graph.getEdgeCount();
        double[] xArray = new double[edgeCount];
        double[] yArray = new double[edgeCount];

        int counter = 0;

        for (E edge: graph.getEdges()) {
            Pair<V> vertices = graph.getEndpoints(edge);
            xArray[counter] = integerTransformer.transform(vertices.getFirst());
            yArray[counter] = integerTransformer.transform(vertices.getSecond());
            counter++;
        }
        return spearmansCorrelation.correlation(xArray, yArray);
    }

    public ArrayList<Integer> getNumOfNodesPerCore() {
        ArrayList<Integer> numOfNodesPerCore = new ArrayList<>();
        for (int i = 1; i <= getMaxCore(); i++) {
            UndirectedSparseGraph<V, E> kCoreGraph = getKCoreNetwork(i);
            int numOfNodes = kCoreGraph.getVertexCount();
            numOfNodesPerCore.add(numOfNodes);
        }

        return numOfNodesPerCore;
    }

    public ArrayList<Integer> getNumOfLinksPerCore() {
        ArrayList<Integer> numOfLinksPerCore = new ArrayList<>();
        for (int i = 1; i <= getMaxCore(); i++) {
            UndirectedSparseGraph<V, E> kCoreGraph = getKCoreNetwork(i);
            int numOfLinks = kCoreGraph.getEdgeCount();
            numOfLinksPerCore.add(numOfLinks);
        }

        return numOfLinksPerCore;
    }

    public ArrayList<Double> getDensityPerCore() {
        ArrayList<Double> densityPerCore = new ArrayList<>();
        for (int i = 1; i <= getMaxCore(); i++) {
            UndirectedSparseGraph<V, E> kCoreGraph = getKCoreNetwork(i);
            int numOfNodes = kCoreGraph.getVertexCount();
            int numOfLinks = kCoreGraph.getEdgeCount();
            double density = ((double) numOfLinks)/(numOfNodes * (numOfNodes - 1));

            densityPerCore.add(density);
        }

        return densityPerCore;
    }

    public ArrayList<Integer> getConnectedComponentsPerCore() {
        ArrayList<Integer> connectedComponentsPerCore = new ArrayList<>();
        for (int i = 1; i <= getMaxCore(); i++) {
            UndirectedSparseGraph<V, E> kCoreGraph = getKCoreNetwork(i);
            DepthFirstSearch<V, E> depthFirstSearch = new DepthFirstSearch<>(kCoreGraph, null);

            int numOfConnectedComponents = depthFirstSearch.getNumberOfComponents();
            connectedComponentsPerCore.add(numOfConnectedComponents);
        }

        return connectedComponentsPerCore;
    }

    public ArrayList<Double> getPercentOfNodesAndLinksInBiggestCore() {
        ArrayList<Double> percents = new ArrayList<>();

        UndirectedSparseGraph<V, E> biggestCore = getKCoreNetwork(getMaxCore());
        double vertexPercent = ((double) biggestCore.getVertexCount()) / graph.getVertexCount();
        double edgePercent = ((double) biggestCore.getEdgeCount()) / graph.getEdgeCount();

        percents.add(vertexPercent);
        percents.add(edgePercent);

        return percents;
    }

    public double getSmallWorldCoefficientInBiggestCore() {

        UndirectedSparseGraph<V, E> kCoreGraph = getKCoreNetwork(getMaxCore());
        DijkstraDistance<V, E> dijkstraDistance = new DijkstraDistance<>(kCoreGraph);

        int vertexCount = kCoreGraph.getVertexCount();
        double distanceSum = 0;

        for (V vertex: kCoreGraph.getVertices()) {
            for (V otherVertex: kCoreGraph.getVertices()) {
                if (!vertex.equals(otherVertex)) {
                    distanceSum += dijkstraDistance.getDistance(vertex, otherVertex).doubleValue();
                }
            }
        }

        distanceSum = distanceSum / 2;

        return distanceSum / (vertexCount * (vertexCount - 1));
    }

    public double getMaxComponentDiameter() {
        UndirectedSparseGraph<V, E> biggestCore = getKCoreNetwork(getMaxCore());

        return DistanceStatistics.diameter(biggestCore);
    }

    public Map<V, Double> getClusteringCoefficientInBiggestCore() {
        UndirectedSparseGraph<V, E> biggestCore = getKCoreNetwork(getMaxCore());

        return Metrics.clusteringCoefficients(biggestCore);
    }

    private int getMaxDegree() {
        int maxDegree = 0;
        for (V vertex: graph.getVertices()) {
            if (graph.degree(vertex) > maxDegree) {
                maxDegree = graph.degree(vertex);
            }
        }

        return maxDegree;
    }

    public int getMaxCore() {
        int maxCore = 0;

        for (int shellIndex: shellIndexes) {
            if (shellIndex > maxCore) {
                maxCore = shellIndex;
            }
        }

        return maxCore;
    }
}
