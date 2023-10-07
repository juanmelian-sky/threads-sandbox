package uk.sky.ovp.kronos;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class Main {
    public static void main(String[] args) throws ExecutionException, InterruptedException {

        /*
         *               OVP KRONOS - VIRTUAL THREADS DEMO
         *
         * 1 - Fixed size pool  =>  Executors.newFixedThreadPool(10)
         * 2 - ForkJoinPool     =>  ForkJoinPool.commonPool()
         * 3 - Virtual Threads  =>  Executors.newVirtualThreadPerTaskExecutor()
         */

        generateNTaskWithExecutor(1_000_000, Executors.newVirtualThreadPerTaskExecutor());
    }

    private static void generateNTaskWithExecutor(int numberOfTasks, ExecutorService executor) throws InterruptedException, ExecutionException {

        List<CompletableFuture<Integer>> futures = new ArrayList<>();
        try (ExecutorService service = executor) {

            for (int i = 0; i < numberOfTasks; i++) {
                System.out.println("Submitting task " + i);
                CompletableFuture<Integer> future = CompletableFuture.supplyAsync(getTask(i), service)
                                .thenApply(taskNumber -> {
                                    System.out.println("Task " + taskNumber + " completed!");
                                    return taskNumber;
                                });
                futures.add(future);
            }
             CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]))
                    .thenAccept(taskNumber -> System.out.println("Program completed!"))
                    .get();
        }
    }

    private static Supplier<Integer> getTask(int taskNumber) {
        return () -> {
            try {
                Thread.sleep(100);
                return taskNumber;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
