import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

class quickThread extends Thread {
    int[] array;
    int low;
    int high;
    int numThreads;

    quickThread(int[] array, int low, int high, int numThreads) {
        this.array = array;
        this.low = low;
        this.high = high;
        this.numThreads = numThreads;
    }

    @Override
    public void run() {
        parallelQuickSort(array, low, high, numThreads);
    }

    public void parallelQuickSort(int[] array, int low, int high, int numThreads){

        if (numThreads <= 1) {
            quicksort.quickSort(array, low, high);
        } else {
            // Divide the array into segments and sort each segment in a separate thread
            Thread[] threads = new Thread[numThreads];
            for (int i = 0; i < numThreads; i++) {
                int start = low + i * (high - low) / numThreads;
                int end = low + (i + 1) * (high - low) / numThreads;
                // Adjust the end for the last segment
                if (i == numThreads - 1) {
                    end = high;
                }

                threads[i] = new quickThread(array, start, end, 1); // Set numThreads to 1 for these threads
                threads[i].start();
            }

            // Wait for all threads to complete
            for (int i = 0; i < numThreads; i++) {
                try {
                    threads[i].join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            quicksort.mergeSort(array, low, high);
        }
    }
}

class quickExecutor implements Runnable {
    int[] array;
    int low;
    int high;
    int numThreads;

    quickExecutor(int[] array, int low, int high, int numThreads) {
        this.array = array;
        this.low = low;
        this.high = high;
        this.numThreads = numThreads;
    }

    @Override
    public void run() {
        parallelQuickSort(array, low, high, numThreads);
    }

    public void parallelQuickSort(int[] array, int low, int high, int numThreads){

        if (numThreads <= 1) {
            quicksort.quickSort(array, low, high);
        } else {
            // Divide the array into segments and sort each segment in a separate thread
            ExecutorService executor = Executors.newFixedThreadPool(numThreads);
            
            for (int i = 0; i < numThreads; i++) {
                int start = low + i * (high - low) / numThreads;
                int end = low + (i + 1) * (high - low) / numThreads;
                // Adjust the end for the last segment
                if (i == numThreads - 1) {
                    end = high;
                }

                executor.submit(new quickExecutor(array, start, end, 1)); // Set numThreads to 1 for these threads
            }
            
            executor.shutdown();
            while (!executor.isTerminated()){

            }

            quicksort.mergeSort(array, low, high);
        }
    }
}

class quickForkJoin extends RecursiveAction {
    int[] array;
    int low;
    int high;
    int numThreads;

    quickForkJoin(int[] array, int low, int high, int numThreads) {
        this.array = array;
        this.low = low;
        this.high = high;
        this.numThreads = numThreads;
    }

    @Override
    public void compute() {
        parallelQuickSort(array, low, high, numThreads);
    }

    public void parallelQuickSort(int[] array, int low, int high, int numThreads){

        if (numThreads <= 1) {
            quicksort.quickSort(array, low, high);
        } else {
            // Divide the array into segments and sort each segment in a separate thread
            ForkJoinPool pool = new ForkJoinPool(numThreads);          
            for (int i = 0; i < numThreads; i++) {
                int start = low + i * (high - low) / numThreads;
                int end = low + (i + 1) * (high - low) / numThreads;
                // Adjust the end for the last segment
                if (i == numThreads - 1) {
                    end = high;
                }

                pool.invoke(new quickForkJoin(array, start, end, 1)); // Set numThreads to 1 for these threads
            }
            
            pool.shutdown();
            while (!pool.isTerminated()){

            }

            quicksort.mergeSort(array, low, high);
        }
    }
}



public class quicksort {

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

    // Swaps two elements in the array
    private static void swap(int[] array, int i, int j) {
        int temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }

    // Partitions the array around the pivot
    static int partition(int[] array, int low, int high) {
        int pivot = array[high];
        int smallerElementIndex = low - 1;

        for (int j = low; j < high; j++) {
            if (array[j] <= pivot) {
                smallerElementIndex++;
                swap(array, smallerElementIndex, j);
            }
        }

        swap(array, smallerElementIndex + 1, high);
        return smallerElementIndex + 1;
    }

    // Recursively applies QuickSort
    public static void quickSort(int[] array, int low, int high) {
        if (low < high) {
            int partitionIndex = partition(array, low, high);

            quickSort(array, low, partitionIndex - 1);
            quickSort(array, partitionIndex + 1, high);
        }
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

    public static int[] quickSortUsingStreams(int[] array) {
        if (array.length <= 1) {
            return array;
        }

        int pivot = array[0]; // Choosing the first element as the pivot for simplicity

        // Partitioning the array into three parts: less than, equal to, and greater than the pivot
        int[] less = Arrays.stream(array).filter(i -> i < pivot).toArray();
        int[] equal = Arrays.stream(array).filter(i -> i == pivot).toArray();
        int[] greater = Arrays.stream(array).filter(i -> i > pivot).toArray();

        // Recursively sort the 'less' and 'greater' partitions and concatenate the results
        return Stream.of(
                quickSortUsingStreams(less),
                equal,
                quickSortUsingStreams(greater)
        ).flatMapToInt(Arrays::stream).toArray();
    }

    public static void main(String[] args) {
        final int SIZE = 100_000_00; // 100 million elements
        int numThreads = 1; // Based on available processors
    
        // Create a random array
        int[] originalArray = createRandomArray(SIZE);
    
        // QuickThread
        int[] arrayForQuickThread = Arrays.copyOf(originalArray, originalArray.length);
        long startTimeQuickThread = System.currentTimeMillis();
        quickThread quickThreadSorter = new quickThread(arrayForQuickThread, 0, arrayForQuickThread.length - 1, numThreads);
        quickThreadSorter.start();
        try {
            quickThreadSorter.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long endTimeQuickThread = System.currentTimeMillis();
        System.out.println("QuickThread sorting took: " + (endTimeQuickThread - startTimeQuickThread) + " ms");
        System.out.println("Is array sorted (QuickThread): " + isArraySorted(arrayForQuickThread));
    
        // QuickExecutor
        int[] arrayForQuickExecutor = Arrays.copyOf(originalArray, originalArray.length);
        long startTimeQuickExecutor = System.currentTimeMillis();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        executor.execute(new quickExecutor(arrayForQuickExecutor, 0, arrayForQuickExecutor.length - 1, numThreads));
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long endTimeQuickExecutor = System.currentTimeMillis();
        System.out.println("QuickExecutor sorting took: " + (endTimeQuickExecutor - startTimeQuickExecutor) + " ms");
        System.out.println("Is array sorted (QuickExecutor): " + isArraySorted(arrayForQuickExecutor));
    
        // QuickForkJoin
        int[] arrayForQuickForkJoin = Arrays.copyOf(originalArray, originalArray.length);
        ForkJoinPool forkJoinPool = new ForkJoinPool(numThreads);
        quickForkJoin quickForkJoinTask = new quickForkJoin(arrayForQuickForkJoin, 0, arrayForQuickForkJoin.length - 1, numThreads);
        long startTimeQuickForkJoin = System.currentTimeMillis();
        forkJoinPool.invoke(quickForkJoinTask);
        forkJoinPool.shutdown();
        long endTimeQuickForkJoin = System.currentTimeMillis();
        System.out.println("QuickForkJoin sorting took: " + (endTimeQuickForkJoin - startTimeQuickForkJoin) + " ms");
        System.out.println("Is array sorted (QuickForkJoin): " + isArraySorted(arrayForQuickForkJoin));
    
        // QuickSortUsingStreams
        int[] arrayForQuickSortUsingStreams = Arrays.copyOf(originalArray, originalArray.length);
        long startTimeQuickSortUsingStreams = System.currentTimeMillis();
        int[] sortedArrayUsingStreams = quickSortUsingStreams(arrayForQuickSortUsingStreams);
        long endTimeQuickSortUsingStreams = System.currentTimeMillis();
        System.out.println("QuickSortUsingStreams took: " + (endTimeQuickSortUsingStreams - startTimeQuickSortUsingStreams) + " ms");
        System.out.println("Is array sorted (QuickSortUsingStreams): " + isArraySorted(sortedArrayUsingStreams));
    }
    
}
