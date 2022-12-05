import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class Parallel extends RecursiveTask<Integer> {

    private static ForkJoinPool poolPar;
    int left, right;
    int[] a;

    public Parallel() {}

    public Parallel(int left, int right, int[] a) {
        this.a = a;
        this.left = left;
        this.right = right;
    }

    static int partition(int[] a, int left, int right) {
        int v = a[(right + left) / 2];
        int i = left, j = right;
        while (i <= j) {
            while (a[i] < v) {
                i++;
            }
            while (a[j] > v) {
                j--;
            }
            if (i >= j)
                break;
            a[i] = a[i] ^ a[j] ^ (a[j] = a[i]);        //swap
            i++;
            j--;
        }
        return j;
    }

    @Override
    protected Integer compute() {
        return quicksort();
    }

    public Integer quicksort() {
        int q = 0;
        if (left < right) {
            q = partition(a, left, right);
            Parallel leftSide = new Parallel(left, q, a);
            Parallel rightSide = new Parallel(q + 1, right, a);
            rightSide.fork();
            leftSide.compute();
            rightSide.join();
        }
        return null;
    }

    public static void testQuicksort() {
        int[] a = {4, 2, 76, 1, 1, 87, 49, 33, 5};
        int[] aSorted = {1, 1, 2, 4, 5, 33, 49, 76, 87};

        ForkJoinPool poolPar = new ForkJoinPool(4);
        poolPar.invoke(new Parallel(0, a.length - 1, a));
        for (int i = 0; i < a.length; i++) {
            if (a[i] != aSorted[i]) {
                System.out.println("Mistake!\n");
                return;
            }
        }
        System.out.println("Correct!\n");
    }

    public static void warmUp() {
        for (int j = 0; j < 5; j++) {
            int[] a = new int[10_000_000];
            for (int i = 0; i < a.length; i++) {
                a[i] = (int) (Math.random() * 10000);
            }
            poolPar.invoke(new Parallel(0, a.length - 1, a));
        }
    }

    public static void main(String[] args) {

        poolPar = new ForkJoinPool(4);

        testQuicksort();

        warmUp();

        int[] a = new int[100_000_000];
        for (int i = 0; i < a.length; i++) {
            a[i] = (int) (Math.random() * 10000);
        }

        long time = System.currentTimeMillis();
        poolPar.invoke(new Parallel(0, a.length - 1, a));
        System.out.println(System.currentTimeMillis() - time);
    }
}
