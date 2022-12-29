import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {

    private static final int numThreads = 4;  // 1, 2, 3, 4
    private static final int x = 50;   // 0, 10, 50
    private static long startTime;
    private static long stopTime;
    private static List<Integer> keys;
    private static Tree bst;
    private static CopyOnWriteArrayList<Integer> nodesArray;
    private static final boolean needCheck = false;

    public static void main(String[] args) {
        Future<Double>[] threads = new Future[numThreads];

        nodesArray = new CopyOnWriteArrayList<>();
        bst = new Tree(Integer.MAX_VALUE);
        if (needCheck) nodesArray.add(Integer.MAX_VALUE);
        ExecutorService pool = Executors.newFixedThreadPool(numThreads);

        keys = IntStream.range(1, (int) 1e5 + 1).boxed().collect(Collectors.toList());
        Collections.shuffle(keys);
        Random r = new Random();
        keys.forEach(k -> {
            if (r.nextInt(2) == 0) {
                bst.insert(k);
                if (needCheck) nodesArray.add(k);
            }
        });

        startTime = System.currentTimeMillis();
        stopTime = System.currentTimeMillis() + 5000;

        for (int i = 0; i < numThreads; i++) {
            if (needCheck) {
                threads[i] = pool.submit(Main::runOperationsWithCheck);
            } else {
                threads[i] = pool.submit(Main::runOperations);
            }
        }

        List<Double> operationsPerSecond = new ArrayList<>();
        for (Future<Double> t: threads) {
            try {
                operationsPerSecond.add(t.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        CopyOnWriteArrayList<Integer> nodePars = bst.inorderTraversal();
        if (needCheck) {
            Collections.sort(nodesArray);
            Assertions.assertEquals(nodesArray, nodePars);
        }

        double operations = 0;
        for (Double ops: operationsPerSecond) {
            operations += ops;
        }
        System.out.println(operations);

        pool.shutdown();
    }

    private static double runOperations() {
        int counter = 0;
        Random r = new Random();
        while (System.currentTimeMillis() < stopTime) {
            Integer key = keys.get(r.nextInt(keys.size()));
            int p = r.nextInt(101);

            if (p < x) {
                bst.insert(key);
            } else if (p >= x && p < 2 * x) {
                bst.delete(key);
            } else if (p >= 2 * x && p <= 100) {
                bst.contains(key);
            }
            counter++;
        }
        double operationsPerSecond = (double) counter / (System.currentTimeMillis() - startTime) * 1000;
        System.out.println("finished\n");
        return operationsPerSecond;
    }

    private static double runOperationsWithCheck() {
        int counter = 0;
        Random r = new Random();
        while (System.currentTimeMillis() < stopTime) {
            Integer key = keys.get(r.nextInt(keys.size()));
            int p = r.nextInt(101);

            if (p < x) {
                if (bst.insert(key)) {
                    nodesArray.add(key);
                }
            } else if (p >= x && p < 2 * x) {
                if (bst.delete(key)) {
                    nodesArray.remove(key);
                }
            } else if (p >= 2 * x && p <= 100) {
                bst.contains(key);
            }
            counter++;
        }
        double operationsPerSecond = (double) counter / (System.currentTimeMillis() - startTime) * 1000;
        System.out.println("finished\n");
        return operationsPerSecond;
    }
}
