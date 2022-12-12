import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Parallel {

    private Graph graph;

    public Parallel(Graph graph) {
        this.graph = graph;
    }

    private int degree(Integer i) {
        return 0;
    }

    public void bfs(int startVertex) {

        List<Integer> frontier = new ArrayList<>();   //поправить
        AtomicBoolean[] visited = new AtomicBoolean[100];  //по количеству вершин

        frontier.add(startVertex);

        while (frontier.size() != 0) {
            int[] d = new int[frontier.size()];
            List<Integer> finalFrontier1 = frontier;
            IntStream.range(0, frontier.size()).forEach(i ->
                            d[i] = degree(finalFrontier1.get(i))
            );

            Arrays.parallelPrefix(d, Integer::sum);
            List<Integer> frontier_p = new ArrayList<>(d[d.length - 1]);

            List<List<Integer>> neighbours = new ArrayList<>(frontier.size());
            List<Integer> finalFrontier = frontier;
            IntStream.range(0, frontier.size()).forEach(i -> {
                List<Integer> N = graph.getVertexNeighbours(finalFrontier.get(i));
                neighbours.set(i, N);
                IntStream.range(0, degree(finalFrontier.get(i))).forEach(j -> {
                            if (visited[neighbours.get(i).get(j)].compareAndExchange(false, true)) {
                                frontier_p.set(d[i] + j, neighbours.get(i).get(j));
                            }
                        }
                    );
                    }
            );
            frontier = frontier_p.parallelStream().filter(Objects::nonNull).collect(Collectors.toList());
        }
    }

    public static void main(String[] args) {
        Graph graph = new Cube(2);
        Parallel p = new Parallel(graph);
    }
}
