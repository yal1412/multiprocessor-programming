import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {

    public static BST Tree;

    public static void main(String[] args) {
        Tree = new BST();
//        Tree.insert(5);
//        Tree.insert(6);
//        Tree.insert(7);
//        Tree.insert(4);
//        Tree.remove(6);
//        Tree.remove(4);
//
//        System.out.println("finish");

        int numThreads = 3;
        Future[] threads = new Future[numThreads];

        ExecutorService pool = Executors.newFixedThreadPool(numThreads);
        for (int i = 0; i < numThreads; i++) {
            threads[i] = pool.submit(Main::baz);
        }

        List<Integer> counts = new ArrayList<>();
        for (Future t: threads) {
            try {
                counts.add((Integer) t.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        for (Integer c:
             counts) {
            System.out.println(c + " ");
        }

        pool.shutdown();
    }

    private static int baz() {
        int counter = 0;
        Random r = new Random();
        long startTime = System.nanoTime();
        while ((System.nanoTime() - startTime) / 1_000_000 < 5_000) {
            System.out.println("step");
            counter++;
            int key = r.nextInt(10000);
            int c = r.nextInt(3);
            switch (c) {
                case 0:
//                    System.out.println("ins");
                    Tree.insert(key);

                    break;
                case 1:
//                    System.out.println("ser");
                    Tree.contains(key);

                    break;
                case 2:
//
                    Tree.remove(key);

                    break;
            }
        }
        System.out.println("finished\n");
        return counter;
    }
}
