import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

class insertionThread extends Thread {
    int[] array;
    int low;
    int high;
    int numThreads;

    insertionThread(int[] array, int low, int high, int numThreads) {
        this.array = array;
        this.low = low;
        this.high = high;
        this.numThreads = numThreads;
    }

    @Override
    public void run() {
        parallelinsertionSort(array, low, high, numThreads);
    }

    public void parallelinsertionSort(int[] array, int low, int high, int numThreads){

        if (numThreads <= 1) {
            insertionsort.insertionSort(array);
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

                threads[i] = new insertionThread(array, start, end, 1); // Set numThreads to 1 for these threads
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

            insertionsort.mergeSort(array, low, high);
        }
    }
}

class insertionExecutor implements Runnable {
    int[] array;
    int low;
    int high;
    int numThreads;

    insertionExecutor(int[] array, int low, int high, int numThreads) {
        this.array = array;
        this.low = low;
        this.high = high;
        this.numThreads = numThreads;
    }

    @Override
    public void run() {
        parallelInsertionSort(array, low, high, numThreads);
    }

    public void parallelInsertionSort(int[] array, int low, int high, int numThreads){

        if (numThreads <= 1) {
            insertionsort.insertionSort(array);
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

                executor.submit(new insertionExecutor(array, start, end, 1)); // Set numThreads to 1 for these threads
            }
            
            executor.shutdown();
            while (!executor.isTerminated()){

            }

            insertionsort.mergeSort(array, low, high);
        }
    }
}

class insertionStreams implements Runnable {

    int[] array;
    int numThreads;

    insertionStreams(int[] array, int numThreads) {
        this.array = array;
        this.numThreads = numThreads;
    }

    @Override
    public void run() {
        parallelInsertionSort(array, numThreads);
    }

    // Insertion sort for a chunk
    private static void insertionSort(int[] array, int start, int end) {
        for (int i = start + 1; i < end; i++) {
            int key = array[i];
            int j = i - 1;

            while (j >= start && array[j] > key) {
                array[j + 1] = array[j];
                j = j - 1;
            }
            array[j + 1] = key;
        }
    }

    // Parallel sort method
    public static void parallelInsertionSort(int[] array, int numThreads) {
        int divisions = array.length / numThreads;

        // Sort each chunk using parallel streams
        IntStream.range(0, (array.length + divisions - 1) / divisions)
                .parallel()
                .forEach(i -> {
                    int start = i * divisions;
                    int end = Math.min(start + divisions, array.length);
                    insertionSort(array, start, end);
                });

        // Merge sorted chunks - this simplistic approach is sequential
        // For a fully parallel solution, a more complex parallel merge would be needed
        int[] tempArray = new int[array.length];
        insertionsort.mergeSort(tempArray, 0, tempArray.length - 1);
        System.arraycopy(tempArray, 0, array, 0, array.length);
    }
    
}

class insertionForkJoin extends RecursiveAction {
    int[] array;
    int low;
    int high;
    int numThreads;

    insertionForkJoin(int[] array, int low, int high, int numThreads) {
        this.array = array;
        this.low = low;
        this.high = high;
        this.numThreads = numThreads;
    }

    @Override
    protected void compute() {
        if (numThreads <= 1) {
            insertionsort.insertionSort(array); 
        } else {
            int mid = low + (high - low) / 2;
            invokeAll(new insertionForkJoin(array, low, mid, numThreads / 2),
                      new insertionForkJoin(array, mid + 1, high, numThreads / 2));
        }
    }
}


public class insertionsort {

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

    // Static method to perform insertion sort on an array
    public static void insertionSort(int[] arr) {
        for (int i = 1; i < arr.length; i++) {
            int key = arr[i];
            int j = i - 1;

            // Move elements of arr[0..i-1], that are greater than key,
            // to one position ahead of their current position
            while (j >= 0 && arr[j] > key) {
                arr[j + 1] = arr[j];
                j = j - 1;
            }
            arr[j + 1] = key;
        }
    }

    public static void main(String[] args) {
        final int SIZE = 100_000; // Use a smaller size for practical execution
        int numThreads = 8;

        // Create a random array
        int[] originalArray = createRandomArray(SIZE);

        // InsertionThread
        int[] arrayForInsertionThread = Arrays.copyOf(originalArray, originalArray.length);
        long startTimeInsertionThread = System.currentTimeMillis();
        insertionThread insertionThreadSorter = new insertionThread(arrayForInsertionThread, 0, arrayForInsertionThread.length - 1, numThreads);
        insertionThreadSorter.start();
        try {
            insertionThreadSorter.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long endTimeInsertionThread = System.currentTimeMillis();
        System.out.println("InsertionThread sorting took: " + (endTimeInsertionThread - startTimeInsertionThread) + " ms");
        System.out.println("Is array sorted (InsertionThread): " + isArraySorted(arrayForInsertionThread));

        // InsertionExecutor
        int[] arrayForInsertionExecutor = Arrays.copyOf(originalArray, originalArray.length);
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        long startTimeInsertionExecutor = System.currentTimeMillis();
        executor.execute(new insertionExecutor(arrayForInsertionExecutor, 0, arrayForInsertionExecutor.length - 1, numThreads));
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long endTimeInsertionExecutor = System.currentTimeMillis();
        System.out.println("InsertionExecutor sorting took: " + (endTimeInsertionExecutor - startTimeInsertionExecutor) + " ms");
        System.out.println("Is array sorted (InsertionExecutor): " + isArraySorted(arrayForInsertionExecutor));

        // InsertionStreams
        int[] arrayForInsertionStreams = Arrays.copyOf(originalArray, originalArray.length);
        long startTimeInsertionStreams = System.currentTimeMillis();
        insertionStreams insertionStreamsSorter = new insertionStreams(arrayForInsertionStreams, numThreads);
        insertionStreamsSorter.run(); // Directly call run for simplicity in demonstration
        long endTimeInsertionStreams = System.currentTimeMillis();
        System.out.println("InsertionStreams sorting took: " + (endTimeInsertionStreams - startTimeInsertionStreams) + " ms");
        System.out.println("Is array sorted (InsertionStreams): " + isArraySorted(arrayForInsertionStreams));

        // InsertionForkJoin
        int[] arrayForInsertionForkJoin = Arrays.copyOf(originalArray, originalArray.length);
        ForkJoinPool forkJoinPool = new ForkJoinPool(numThreads);
        long startTimeInsertionForkJoin = System.currentTimeMillis();
        insertionForkJoin insertionForkJoinSorter = new insertionForkJoin(arrayForInsertionForkJoin, 0, arrayForInsertionForkJoin.length - 1, numThreads);
        forkJoinPool.invoke(insertionForkJoinSorter);
        forkJoinPool.shutdown();
        long endTimeInsertionForkJoin = System.currentTimeMillis();
        System.out.println("InsertionForkJoin sorting took: " + (endTimeInsertionForkJoin - startTimeInsertionForkJoin) + " ms");
        System.out.println("Is array sorted (InsertionForkJoin): " + isArraySorted(arrayForInsertionForkJoin));
    }
}
