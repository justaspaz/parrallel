import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ForkJoinPool;
import java.util.Arrays;

public class Main {
    // Method to perform parallel merge sort with a specified number of threads
    public static void parallelMergeSort(int[] array, int numThreads) {
        ForkJoinPool pool = new ForkJoinPool(numThreads); // Use specified number of threads
        pool.invoke(new MergeSortTask(array, 0, array.length - 1));
        pool.shutdown();
    }

    static class MergeSortTask extends RecursiveTask<Void> {
        private final int[] array;
        private final int left;
        private final int right;

        MergeSortTask(int[] array, int left, int right) {
            this.array = array;
            this.left = left;
            this.right = right;
        }

        @Override
        protected Void compute() {
            if (right - left < 10) {
                Arrays.sort(array, left, right + 1); // Fallback to sequential sort
            } else {
                int mid = (left + right) / 2;

                // Divide and conquer: Parallel tasks for left and right halves
                MergeSortTask leftTask = new MergeSortTask(array, left, mid);
                MergeSortTask rightTask = new MergeSortTask(array, mid + 1, right);

                invokeAll(leftTask, rightTask); // Execute tasks in parallel
                merge(array, left, mid, right);
            }
            return null;
        }

        private void merge(int[] array, int left, int mid, int right) {
            int[] temp = new int[right - left + 1];
            int i = left, j = mid + 1, k = 0;

            while (i <= mid && j <= right) {
                if (array[i] <= array[j]) {
                    temp[k++] = array[i++];
                } else {
                    temp[k++] = array[j++];
                }
            }
            while (i <= mid) temp[k++] = array[i++];
            while (j <= right) temp[k++] = array[j++];

            System.arraycopy(temp, 0, array, left, temp.length);
        }
    }
    static boolean isSorted(int[] array) {
        for (int i = 0; i < array.length - 1; i++) {
            if (array[i] > array[i + 1])
                return false;
        }
        return true;
    }

    public static void main(String[] args) {
        int size = 1 << 24; // Array size: 2^28 (268435456 elements)
        int[] array = new int[size];

        // Initialize array with random values
        for (int i = 0; i < size; i++) {
            array[i] = (int) (Math.random() * size);
        }

        // Test with different numbers of threads
        int[] threadCounts = {1, 2, 4, 8, 16, 32, 64, 128, 256}; // Specify the number of threads to test

        for (int numThreads : threadCounts) {
            int[] arrayCopy = Arrays.copyOf(array, array.length); // Create a fresh copy of the array for each test

            long start = System.currentTimeMillis();
            parallelMergeSort(arrayCopy, numThreads);
            long end = System.currentTimeMillis();
            System.out.println(isSorted(arrayCopy));
            System.out.println("Threads: " + numThreads + ", Time: " + (end - start) + "ms");
        }
    }
}
