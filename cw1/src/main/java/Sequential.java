import java.util.concurrent.ForkJoinPool;

public class Sequential {

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

    static void quicksort(int[] a, int left, int right) {
        int q = 0;
        if (left < right) {
            q = partition(a, left, right);
            quicksort(a, left, q);
            quicksort(a, q + 1, right);
        }
    }

    public static void testQuicksort() {
        int[] a = {4, 2, 76, 1, 1, 87, 49, 33, 5};
        int[] aSorted = {1, 1, 2, 4, 5, 33, 49, 76, 87};

        quicksort(a, 0, a.length - 1);
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
            quicksort(a, 0, a.length - 1);
        }
    }

    public static void main(String[] args) {

        testQuicksort();

        warmUp();

        int[] a = new int[100_000_000];
        for (int i = 0; i < a.length; i++) {
            a[i] = (int) (Math.random() * 10000);
        }
        long time = System.currentTimeMillis();
        quicksort(a, 0, a.length - 1);
        System.out.println(System.currentTimeMillis() - time);
    }
}
