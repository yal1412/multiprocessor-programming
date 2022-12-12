import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Cube implements Graph {

    private final int sideLength;
    private final int size;

    public Cube(int s) {
        sideLength = s;
        size = s * s * s;
    }

    // По номеру вершины возвращает номера соседей
    @Override
    public List<Integer> getVertexNeighbours(int vertexNumber){
        Vertex startVertex = new Vertex(vertexNumber, sideLength);
        LinkedList<Integer> neighbours = new LinkedList<>();
        int i = startVertex.getI();
        int j = startVertex.getJ();
        int k = startVertex.getK();

        if (i + 1 < sideLength) {
            neighbours.add((new Vertex(i + 1, j, k, sideLength)).getNumber());
        }
        if (j + 1 < sideLength) {
            neighbours.add((new Vertex(i, j + 1, k, sideLength)).getNumber());
        }
        if (k + 1 < sideLength) {
            neighbours.add((new Vertex(i, j, k + 1, sideLength)).getNumber());
        }
        return neighbours;
    }

    public int getSideLength() {
        return sideLength;
    }

    @Override
    public int getSize() {
        return size;
    }
}
