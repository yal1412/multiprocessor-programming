// Java program to print BFS traversal from a given source vertex.
// BFS(int s) traverses vertices reachable from s.
import java.util.*;

// This class represents a directed graph using adjacency list
// representation
class Sequential
{
    private int numberOfVertex; // No. of vertices
    private Graph graph;

    // Constructor
    Sequential(Graph graph)
    {
        this.graph = graph;
        this.numberOfVertex = graph.getSize();
    }

    // prints BFS traversal from a given source s
    void BFS(int startVertex)
    {
        // Mark all the vertices as not visited(By default
        // set as false)
        boolean[] visited = new boolean[numberOfVertex];

        // Create a queue for BFS
        LinkedList<Integer> vertexQueue = new LinkedList<Integer>();

        // Mark the current node as visited and enqueue it
        visited[startVertex] = true;
        vertexQueue.add(startVertex);

        while (vertexQueue.size() != 0)
        {
            // Dequeue a vertex from queue and print it
            startVertex = vertexQueue.poll();

            // Get all adjacent vertices of the dequeued vertex s
            // If a adjacent has not been visited, then mark it
            // visited and enqueue it

            // Получить всех соседей вершины, если в них еще не были, пометить их и добавить в очередь
            List<Integer> adjacencyList = graph.getVertexNeighbours(startVertex);
            for (Integer vertex : adjacencyList) {
                if (!visited[vertex]) {
                    visited[vertex] = true;
                    vertexQueue.add(vertex);
                }
            }
        }
    }

    // Driver method to
    public static void main(String args[])
    {
        Graph graph = new Cube(500);
        Sequential s = new Sequential(graph);

        // ПОПРАВИТЬ НА НАНО
        long time = System.currentTimeMillis();
        s.BFS(0);
        System.out.println((System.currentTimeMillis() - time));
    }
}
