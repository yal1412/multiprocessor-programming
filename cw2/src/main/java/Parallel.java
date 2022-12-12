import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Parallel {

    private Graph graph;

    public Parallel(Graph graph) {
        this.graph = graph;
    }

    private int degree(Integer i) {
        return graph.getVertexNeighbours(i).size();
    }

    public int[] bfs(int startVertex) {

        int[] frontier = new int[1];
        AtomicBoolean[] visited = new AtomicBoolean[graph.getSize()];
        visited[startVertex] = new AtomicBoolean(true);
        IntStream.range(startVertex + 1, visited.length).parallel().forEach(i ->
                visited[i] = new AtomicBoolean(false)
        );

        int[] distances = new int[graph.getSize()];
        Arrays.fill(distances, -1);
        distances[startVertex] = 0;

        frontier[0] = startVertex;

        while (frontier.length != 0) {
            int[] d = new int[frontier.length + 1];
            d[0] = 0;
            int[] finalFrontier1 = frontier;
            IntStream.range(0, frontier.length).parallel().forEach(i ->
                            d[i + 1] = degree(finalFrontier1[i])
            );

            Arrays.parallelPrefix(d, Integer::sum);
            Integer[] frontier_p = new Integer[d[d.length - 1]];

            int[][] neighbours = new int[frontier.length][];
            int[] finalFrontier = frontier;
            IntStream.range(0, frontier.length).parallel().forEach(i -> {
//                System.out.println(i + " " + Thread.currentThread().getName());
                List<Integer> N = graph.getVertexNeighbours(finalFrontier[i]);
                neighbours[i] = N.stream().mapToInt(Integer::intValue).toArray();
                IntStream.range(0, degree(finalFrontier[i])).parallel().forEach(j -> {
                            if (visited[neighbours[i][j]].compareAndSet(false, true)) {
                                frontier_p[d[i] + j] = neighbours[i][j];
                                distances[neighbours[i][j]] = distances[finalFrontier[i]] + 1;
                            }
                        }
                    );
                    }
            );

            List<Integer> s = Arrays.asList(frontier_p);
            Stream<Integer> a = s.parallelStream().filter(Objects::nonNull);
            frontier = a.mapToInt(Integer::intValue).toArray();
//            frontier = Arrays.asList(frontier_p).parallelStream().filter(Objects::nonNull).collect(Collectors.toList());
        }

        System.out.println(visited.length);
        return distances;
    }

    static public void testBFS() {
        int side = 10;
        Graph graph = new Cube(side);
        Parallel s = new Parallel(graph);
        int[] d = s.bfs(0);
        Vertex v;
        for (int i = 0; i < graph.getSize(); i++) {
            v = new Vertex(i, side);
            if (d[i] != v.getI() + v.getJ() + v.getK()) {
                System.out.println("Error in " + i + " vertex\n");
                return;
            }
        }
        System.out.println("Correct!");
    }

    static public void warmUp() {
        int side = 100;
        Graph graph = new Cube(side);
        Parallel s = new Parallel(graph);
        for (int i = 0; i < 5; i++) {
            s.bfs(0);
        }
    }

    public static void main(String[] args) {
        testBFS();

        warmUp();

        Graph graph = new Cube(500);
        Parallel p = new Parallel(graph);

        long time = System.currentTimeMillis();
        p.bfs(0);
        System.out.println((System.currentTimeMillis() - time));
    }
}
