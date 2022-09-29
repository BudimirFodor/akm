import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class RandomGraphGenerator {

    public static void main(String[] args) {
//        stochasticBlockModel();
        corePeriphery();
    }

    public static void genericGraph() {
        int vertexCount = 50;
        int edgeCount = 100;
        String fileName = "CustomGraph.txt";

        FileWriter fileWriter = null;
        BufferedWriter bufferedWriter = null;

        try {
            File outputFile = new File(fileName);
            fileWriter = new FileWriter(outputFile);
            bufferedWriter = new BufferedWriter(fileWriter);

            Random rand = new Random();

            for (int i = 0; i < edgeCount; i++) {
                int from = rand.nextInt(vertexCount);
                int to = rand.nextInt(vertexCount);
                int relation = rand.nextInt(2);

                bufferedWriter.write(from + "," + to + "," + relation + System.lineSeparator());
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

    public static void stochasticBlockModel() {
        String fileName = "SBMGraph.txt";
        Random rand = new Random();

        int n = 10000;
//        int q = 3;
        double[] m = new double[] {0.6, 0.9, 1};
        double[][] b = new double[][] {{0.005, 0.0002, 0.0005}, {0.0002, 0.0005, 0.0025}, {0.0025, 0.0002, 0.0005}};

        int[] g = new int[n];

        for (int i = 0; i < n; i++) {
            double vertexOdds = rand.nextDouble();
            boolean isAssigned = false;
            int j = 0;
            while(!isAssigned) {
                if (vertexOdds <= m[j]) {
                    isAssigned = true;
                    g[i] = j;
                }
                j++;
            }
        }

        FileWriter fileWriter = null;
        BufferedWriter bufferedWriter = null;

        try {
            File outputFile = new File(fileName);
            fileWriter = new FileWriter(outputFile);
            bufferedWriter = new BufferedWriter(fileWriter);

            for (int i = 0; i < n - 1; i++) {
                for (int j = i + 1; j < n; j++) {
                    if (rand.nextDouble() <= b[g[i]][g[j]]) {
                        int relation = g[i] == g[j] ? 1 : -1;
                        bufferedWriter.write(i + "," + j + "," + relation + System.lineSeparator());
                    }
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

    public static void corePeriphery() {
        String fileName = "CorePeripheryGraph.txt";
        Random rand = new Random();

        int n = 10000;
        // chance for a graph to be in the core
        double[] m = new double[] {0.1, 1};
        double[][] b = new double[][] {{0.2, 0.002}, {0.002, 0.0001}};

        int[] g = new int[n];



        for (int i = 0; i < n; i++) {
            double vertexOdds = rand.nextDouble();
            boolean isAssigned = false;
            int j = 0;
            while(!isAssigned) {
                if (vertexOdds <= m[j]) {
                    isAssigned = true;
                    g[i] = j;
                }
                j++;
            }
        }

        FileWriter fileWriter = null;
        BufferedWriter bufferedWriter = null;

        try {
            File outputFile = new File(fileName);
            fileWriter = new FileWriter(outputFile);
            bufferedWriter = new BufferedWriter(fileWriter);

            for (int i = 1; i < n; i++) {
                int otherVertex = rand.nextInt(i);
                bufferedWriter.write(otherVertex + "," + i + System.lineSeparator());
            }

            for (int i = 0; i < n - 1; i++) {
                for (int j = i + 1; j < n; j++) {
                    if (rand.nextDouble() <= b[g[i]][g[j]]) {
                        int relation = g[i] == g[j] ? 1 : -1;
                        bufferedWriter.write(i + "," + j + "," + relation + System.lineSeparator());
                    }
                }
            }

            for (int i = 0; i < n - 1; i++) {
                for (int j = i; j < n; j++) {
                    if (rand.nextDouble() <= b[g[i]][g[j]]) {
                        int relation = g[i] == g[j] ? 1 : 0;
                        bufferedWriter.write(i + "," + j + "," + relation + System.lineSeparator());
                    }
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
}
