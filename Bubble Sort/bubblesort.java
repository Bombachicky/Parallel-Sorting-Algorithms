import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

class bubbleThread extends Thread {
    int[] array;
    int low;
    int high;
    int numThreads;

    bubbleThread(int[] array, int low, int high, int numThreads) {
        this.array = array;
        this.low = low;
        this.high = high;
        this.numThreads = numThreads;
    }

    @Override
    public void run() {
        parallelbubbleSort(array, low, high, numThreads);
    }

    public void parallelbubbleSort(int[] array, int low, int high, int numThreads){

        if (numThreads <= 1) {
            bubblesort.bubbleSort(array);
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

                threads[i] = new bubbleThread(array, start, end, 1); // Set numThreads to 1 for these threads
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

            bubblesort.mergeSort(array, low, high);
        }
    }
}

class bubbleExecutor implements Runnable {
    int[] array;
    int low;
    int high;
    int numThreads;

    bubbleExecutor(int[] array, int low, int high, int numThreads) {
        this.array = array;
        this.low = low;
        this.high = high;
        this.numThreads = numThreads;
    }

    @Override
    public void run() {
        parallelbubbleSort(array, low, high, numThreads);
    }

    public void parallelbubbleSort(int[] array, int low, int high, int numThreads){

        if (numThreads <= 1) {
            bubblesort.bubbleSort(array);
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

                executor.submit(new bubbleExecutor(array, start, end, 1)); // Set numThreads to 1 for these threads
            }
            
            executor.shutdown();
            while (!executor.isTerminated()){

            }

            bubblesort.mergeSort(array, low, high);
        }
    }
}

class bubbleStreams implements Runnable {

    int[] array;
    int numThreads;

    bubbleStreams(int[] array, int numThreads) {
        this.array = array;
        this.numThreads = numThreads;
    }

    @Override
    public void run() {
        parallelbubbleSort(array, numThreads);
    }

    // bubble sort for a chunk
    private static void bubbleSort(int[] array, int start, int end) {
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
    public static void parallelbubbleSort(int[] array, int numThreads) {
        int divisions = array.length / numThreads;

        // Sort each chunk using parallel streams
        IntStream.range(0, (array.length + divisions - 1) / divisions)
                .parallel()
                .forEach(i -> {
                    int start = i * divisions;
                    int end = Math.min(start + divisions, array.length);
                    bubbleSort(array, start, end);
                });

        // Merge sorted divisions - this simplistic approach is sequential
        // For a fully parallel solution, a more complex parallel merge would be needed
        int[] tempArray = new int[array.length];
        bubblesort.mergeSort(tempArray, 0, tempArray.length - 1);
        System.arraycopy(tempArray, 0, array, 0, array.length);
    }
    
}

class bubbleForkJoin extends RecursiveAction {
    int[] array;
    int low;
    int high;
    int numThreads;

    bubbleForkJoin(int[] array, int low, int high, int numThreads) {
        this.array = array;
        this.low = low;
        this.high = high;
        this.numThreads = numThreads;
    }

    @Override
    protected void compute() {
        if (numThreads <= 1) {
            bubblesort.bubbleSort(array); 
        } else {
            int mid = low + (high - low) / 2;
            invokeAll(new bubbleForkJoin(array, low, mid, numThreads / 2),
                      new bubbleForkJoin(array, mid + 1, high, numThreads / 2));
        }
    }
}


public class bubblesort {

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

    // Method to perform Bubble Sort
    public static void bubbleSort(int[] arr) {
        int n = arr.length;
        boolean swapped;
        for (int i = 0; i < n - 1; i++) {
            swapped = false;
            for (int j = 0; j < n - i - 1; j++) {
                if (arr[j] > arr[j + 1]) {
                    // Swap arr[j] and arr[j+1]
                    int temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                    swapped = true;
                }
            }
            // If no two elements were swapped by inner loop, then break
            if (!swapped)
                break;
        }
    }

    public static void main(String[] args) {
        final int SIZE = 100_000; // Use a smaller size for practical execution
        int numThreads = 1;

        // Create a random array
        int[] originalArray = createRandomArray(SIZE);

        // bubbleThread
        int[] arrayForbubbleThread = Arrays.copyOf(originalArray, originalArray.length);
        long startTimebubbleThread = System.currentTimeMillis();
        bubbleThread bubbleThreadSorter = new bubbleThread(arrayForbubbleThread, 0, arrayForbubbleThread.length - 1, numThreads);
        bubbleThreadSorter.start();
        try {
            bubbleThreadSorter.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long endTimebubbleThread = System.currentTimeMillis();
        System.out.println("bubbleThread sorting took: " + (endTimebubbleThread - startTimebubbleThread) + " ms");
        System.out.println("Is array sorted (bubbleThread): " + isArraySorted(arrayForbubbleThread));

        // bubbleExecutor
        int[] arrayForbubbleExecutor = Arrays.copyOf(originalArray, originalArray.length);
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        long startTimebubbleExecutor = System.currentTimeMillis();
        executor.execute(new bubbleExecutor(arrayForbubbleExecutor, 0, arrayForbubbleExecutor.length - 1, numThreads));
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long endTimebubbleExecutor = System.currentTimeMillis();
        System.out.println("bubbleExecutor sorting took: " + (endTimebubbleExecutor - startTimebubbleExecutor) + " ms");
        System.out.println("Is array sorted (bubbleExecutor): " + isArraySorted(arrayForbubbleExecutor));

        // bubbleStreams
        int[] arrayForbubbleStreams = Arrays.copyOf(originalArray, originalArray.length);
        long startTimebubbleStreams = System.currentTimeMillis();
        bubbleStreams bubbleStreamsSorter = new bubbleStreams(arrayForbubbleStreams, numThreads);
        bubbleStreamsSorter.run(); // Directly call run for simplicity in demonstration
        long endTimebubbleStreams = System.currentTimeMillis();
        System.out.println("bubbleStreams sorting took: " + (endTimebubbleStreams - startTimebubbleStreams) + " ms");
        System.out.println("Is array sorted (bubbleStreams): " + isArraySorted(arrayForbubbleStreams));

        // bubbleForkJoin
        int[] arrayForbubbleForkJoin = Arrays.copyOf(originalArray, originalArray.length);
        ForkJoinPool forkJoinPool = new ForkJoinPool(numThreads);
        long startTimebubbleForkJoin = System.currentTimeMillis();
        bubbleForkJoin bubbleForkJoinSorter = new bubbleForkJoin(arrayForbubbleForkJoin, 0, arrayForbubbleForkJoin.length - 1, numThreads);
        forkJoinPool.invoke(bubbleForkJoinSorter);
        forkJoinPool.shutdown();
        long endTimebubbleForkJoin = System.currentTimeMillis();
        System.out.println("bubbleForkJoin sorting took: " + (endTimebubbleForkJoin - startTimebubbleForkJoin) + " ms");
        System.out.println("Is array sorted (bubbleForkJoin): " + isArraySorted(arrayForbubbleForkJoin));
    }
}
