import java.util.Arrays;
import java.util.List;

public class Vertex {
    private int number;
    private int i, j, k;

    public Vertex(int vertexNumber, int sideLength) {
        number = vertexNumber;
        coordinatesFromNumber(sideLength);
    }

    public Vertex(int i, int j, int k, int sideLength) {
        this.i = i;
        this.j = j;
        this.k = k;
        numberFromCoordinates(sideLength);
    }

    private void coordinatesFromNumber(int sideLength) {
        i = number % sideLength;
        j = (number / sideLength) % sideLength;
        k = (number / sideLength) / sideLength;
    }

    private void numberFromCoordinates(int sideLength) {
        number = (k * sideLength + j) * sideLength + i;
    }

    public int getNumber() {
        return number;
    }

    public int getI() {
        return i;
    }

    public int getJ() {
        return j;
    }

    public int getK() {
        return k;
    }

    public List<Integer> getCoordinates() {
        return Arrays.asList(i, j, k);
    }
}
