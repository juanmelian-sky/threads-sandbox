package uk.sky.ovp.kronos;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Supplier;

public class Main {
    public static void main(String[] args) throws ExecutionException, InterruptedException {

        /*
         *  ----------------------------------------------------------------
         *                 OVP KRONOS - VIRTUAL THREADS DEMO
         *  ----------------------------------------------------------------
         * 1 - Fixed size pool  =>  Executors.newFixedThreadPool(10)
         * 2 - ForkJoinPool     =>  ForkJoinPool.commonPool()
         * 3 - Virtual Threads  =>  Executors.newVirtualThreadPerTaskExecutor()
         *  ----------------------------------------------------------------
         */
        generateNTaskWithExecutor(1_000_000, DemoExecutorType.FIXED_CACHED_POOL);
    }

    private static void generateNTaskWithExecutor(int numberOfTasks, DemoExecutorType executorType) throws InterruptedException, ExecutionException {

        List<CompletableFuture<Integer>> futures = new ArrayList<>();
        try (ExecutorService service = executorForType(executorType)) {

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

    private static ExecutorService executorForType(DemoExecutorType executorType) {
        return switch (executorType) {
            case FORK_JOIN_POOL -> ForkJoinPool.commonPool();
            case FIXED_CACHED_POOL ->  Executors.newFixedThreadPool(10);
            case VIRTUAL_THREADS ->  Executors.newVirtualThreadPerTaskExecutor();
        };
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

    private enum DemoExecutorType {
        FIXED_CACHED_POOL,
        FORK_JOIN_POOL,
        VIRTUAL_THREADS
    }
}
