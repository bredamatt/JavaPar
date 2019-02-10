import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.Arrays;
// Divide-and-conquer examples
// A ForkJoin sort that sorts a given long[] array
class SortTask extends RecursiveAction {
    final long[] array;
    final int lo;
    final int hi;
    
    // Constructor 
    SortTask(long[] array, int lo, int hi) {
        this.array = array; 
        this.lo = lo;
        this.hi = hi;
    }

    int THRESHOLD = 1000000;
    // The sort task.
    protected void compute() {
        if (hi - lo < THRESHOLD)
            Arrays.sort(array, lo, hi);
        else {
            int mid = (lo + hi) >>> 1;
            invokeAll(new SortTask(array, lo, mid),
                       new SortTask(array, mid, hi));
            merge(array, lo, hi); // needs to be implemented somehow
        }
    }

    public static void main(String[] args) {
        long[] anArray = {12, 35, 36, 25};
        int lo = 0;
        int hi = anArray.length - 1;

        SortTask st = new SortTask(anArray, lo, hi);
        ForkJoinPool fjp = new ForkJoinPool();
        System.out.println("Invoking async sort on this array: ");
        System.out.println(Arrays.toString(anArray));
        fjp.invoke(st);
        System.out.println("Finished.");
        System.out.println(Arrays.toString(anArray));
    }
}