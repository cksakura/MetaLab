package bmi.med.uOttawa.metalab.core.tools;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ExecutorServiceExample {

    public static void main(String[] args) {
        // Create a ThreadPoolExecutor with a fixed number of threads
        ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);

        // Submit some tasks
        for (int i = 0; i < 20; i++) {
            executorService.submit(new Task());
        }

        // Monitor the ExecutorService
        monitorExecutorService(executorService);

        // Shutdown the ExecutorService
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private static void monitorExecutorService(ThreadPoolExecutor executorService) {
        System.out.println("Core pool size: " + executorService.getCorePoolSize());
        System.out.println("Maximum pool size: " + executorService.getMaximumPoolSize());
        System.out.println("Current pool size: " + executorService.getPoolSize());
        System.out.println("Active thread count: " + executorService.getActiveCount());
        System.out.println("Number of tasks completed: " + executorService.getCompletedTaskCount());
        System.out.println("Number of tasks in queue: " + executorService.getQueue().size());
        System.out.println("Total number of tasks submitted: " + executorService.getTaskCount());
    }

    static class Task implements Runnable {
        @Override
        public void run() {
            try {
                // Simulate a task by sleeping for a while
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}

