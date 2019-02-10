import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

class IncrementTask extends RecursiveAction {
    final long[] array; 
    final int lo;
    final int hi;

    IncrementTask(long[] array, int lo, int hi) {
        this.array = array;
        this.lo = lo;
        this.hi = hi;
    }
    int THRESHOLD = 10000;

    // This compotue function does not require a merge() implementation.
    protected void compute() {
        System.out.println(Arrays.toString(array));
        if (hi - lo < THRESHOLD) {
            for (int i = lo; i < hi; ++i)
                array[i]++;
        }
        else {
            int mid = (lo + hi) >>> 1;
            invokeAll(new IncrementTask(array, lo, mid),
                      new IncrementTask(array, mid, hi));
        }

        System.out.println(Arrays.toString(array));
    }

    public static void main(String[] args) {
        long[] theArr = {1,2,6,5,3,9,11};
        int lo = 0;
        int hi = theArr.length-1;

        IncrementTask it = new IncrementTask(theArr, lo, hi);
        ForkJoinPool fjp = new ForkJoinPool();
        fjp.invoke(it);
    }
}