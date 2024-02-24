import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;

class mergeThread extends Thread {
    int[] array;
    int start;
    int end;
    int numThreads;

    mergeThread(int[] array, int start, int end, int numThreads){
        this.array = array;
        this.start = start;
        this.end = end;
        this.numThreads = numThreads;
    }

    public void run(){
        parallelMergeSort(array, start, end, numThreads);
    }

    public void parallelMergeSort(int[] array, int start, int end, int numThreads){
        if (numThreads <= 1){
            mergesort.mergeSort(array, start, end);
        }
        else {
            int middle = start + (end - start)/2;
            Thread mergeLeft = new mergeThread(array, start, middle, numThreads / 2);
            Thread mergeRight = new mergeThread(array, middle + 1, end, numThreads / 2);

            mergeLeft.start();
            mergeRight.start();

            try {
                mergeLeft.join();
                mergeRight.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            mergesort.merge(array, start, middle, end);
        }
    }
}

class mergeExecutor implements Runnable {

    int[] array;
    int start;
    int end;
    int numThreads;

    mergeExecutor(int[] array, int start, int end, int numThreads){
        this.array = array;
        this.start = start;
        this.end = end;
        this.numThreads = numThreads;
    }

    @Override
    public void run() {
        parallelMergeSort(array, start, end, numThreads);
    }

    public void parallelMergeSort(int[] array, int start, int end, int numThreads){
        if (numThreads <= 1){
            mergesort.mergeSort(array, start, end);
        }
        else{
            ExecutorService executor = Executors.newFixedThreadPool(numThreads);
            int middle = start + (end - start)/2;

            mergeExecutor mergeLeft = new mergeExecutor(array, start, middle, numThreads / 2);
            mergeExecutor mergeRight = new mergeExecutor(array, middle + 1, end, numThreads / 2);

            Future<?> mergeLeftFuture = executor.submit(mergeLeft);
            Future<?> mergeRightFuture = executor.submit(mergeRight);

            try {
                mergeLeftFuture.get();
                mergeRightFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            executor.shutdown();
            mergesort.merge(array, start, middle, end);

        }
    }
}

class mergeStreams implements Runnable {
    int[] array;
    int start;
    int end;
    int numThreads; 

    mergeStreams(int[] array, int start, int end, int numThreads) {
        this.array = array;
        this.start = start;
        this.end = end;
        this.numThreads = numThreads;
    }

    @Override
    public void run() {
        parallelMergeSort(array, start, end, numThreads);
    }

    public void parallelMergeSort(int[] array, int start, int end, int numThreads) {
        if (numThreads <= 1) {
            mergesort.mergeSort(array, start, end);
        }
        else {
            int middle = start + (end - start)/2;

            // Parallel execution of the two halves of the array
            IntStream.range(0, 2).parallel().forEach(i -> {
                if (i == 0) {
                    parallelMergeSort(array, start, middle, numThreads / 2);
                } else {
                    parallelMergeSort(array, middle + 1, end, numThreads / 2);
                }
            });

            // Merging the sorted halves
            mergesort.merge(array, start, middle, end);
        }
    }

}

class mergeForkJoin extends RecursiveAction {
    int[] array;
    int start;
    int end;
    int numThreads; 

    mergeForkJoin(int[] array, int start, int end, int numThreads) {
        this.array = array;
        this.start = start;
        this.end = end;
        this.numThreads = numThreads;
    }

    @Override
    protected void compute() {
        if (numThreads <= 1) {
            mergesort.mergeSort(array, start, end); // Use sequential sort for small segments or when numThreads is low
        } else {
            int middle = start + (end - start) / 2;
            mergeForkJoin leftTask = new mergeForkJoin(array, start, middle, numThreads / 2);
            mergeForkJoin rightTask = new mergeForkJoin(array, middle + 1, end, numThreads / 2);

            invokeAll(leftTask, rightTask); // Process sub-tasks in parallel

            mergesort.merge(array, start, middle, end); // Merge the sorted halves
        }
    }
}



public class mergesort {

    public static int[] createRandomArray(int size){
        int[] randomArray = new int[size];
        
        Random random = new Random(); 

        int min = 0;
        int max = 100_000;

        for (int i = 0; i < size; i++){
            randomArray[i] = random.nextInt(max - min) + min;
        }

        return randomArray;
    }

        public static boolean isArraySorted(int[] array) {
        // If the array has 0 or 1 element, it is considered sorted
        if (array.length <= 1) {
            return true;
        }

        // Iterate through the array and check if each element is less than or equal to the next
        for (int i = 0; i < array.length - 1; i++) {
            // If the current element is greater than the next, the array is not sorted
            if (array[i] > array[i + 1]) {
                return false;
            }
        }

        // If we reach here, all elements were in non-decreasing order
        return true;
    }

    public static void mergeSort(int[] array, int start, int end){
        if (end - start < 1) return;

        int middle = start + (end - start)/2;

        mergeSort(array, start, middle);
        mergeSort(array, middle + 1, end);

        merge(array, start, middle, end);
    }

    public static void merge(int[] array, int start, int middle, int end){
        int i = start;
        int j = middle + 1;
        int k = 0;
        int[] sorted = new int[end - start + 1];
        while (i <= middle && j <= end){
            if (array[i] <= array[j]){
                sorted[k++] = array[i++];
            }
            else{
                sorted[k++] = array[j++];
            }
        }

        while (i <= middle){
            sorted[k++] = array[i++];
        }

        while (j <= end){
            sorted[k++] = array[j++];
        }
        
        k = 0;

        while (k < sorted.length){
            array[start++] = sorted[k++];
        }
    }

    


    public static void main(String[] args) {
        // Example array to sort
        int[] array = createRandomArray(100_000_000);  // Assuming this method creates a random array

        // Number of threads to use
        int numThreads = 8;

        mergeForkJoin task = new mergeForkJoin(array, 0, array.length - 1, numThreads);
        ForkJoinPool pool = new ForkJoinPool(numThreads);
        pool.invoke(task);
        pool.shutdown();

        // Check if the array is sorted
        System.out.println("Is array sorted: " + isArraySorted(array));
    }
}