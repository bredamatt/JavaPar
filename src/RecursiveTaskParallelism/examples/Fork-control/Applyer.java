import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

// An example of techniques to improve performance of Task level parallelism

// The Applyer class sums the squares of each element in a double array.
// It subdivides out only the RHS of repeated divisions by two.
// Monitors the the RH-sides using next references. 
// Dynamic threshold is used and excess partitioning is kept in check by
// performing "leaf" actions on unstolen tasks, rather than further subdividing.
class Applyer extends RecursiveAction {
    final double[] array;
    final int lo, hi;
    double result;
    Applyer next;

    // This is the main method taking ForkJoinPool and array of doubles as args
    // and invokes the Applyer task to the pool.
    static double sumOfSquares(ForkJoinPool pool, double[] array) {
        int n = array.length;
        Applyer a = new Applyer(array, 0, n-1, null);
        pool.invoke(a);
        return a.result;
    }

    // Constructor
    Applyer(double[]array, int lo, int hi, Applyer next) {
        this.array = array; 
        this.lo = lo; 
        this.hi = hi;
        this.next = next; 
    }

    // Perform the leftmost base step
    double atLeaf(int l, int h) {
        double sum = 0;

        for (int i = l; i < h; ++i) {
            sum += array[i] * array[i];
        }
        return sum;
    }

    // Relies upon getSurplusQueuedTaskCount() --> Dynamic Threshold
    // Recall that ForkJoinPool uses a work-stealing algorithm
    // The worker thread doing the computation has some queue of tasks,
    // if this surplus exceeds 3, a new thread is initialized with right.fork() 
    
    // This is the ACTUAL Computation that happens :-) 
    @Override
    protected void compute() {
        int l = lo;
        int h = hi;
        Applyer right = null; 

        // in most cases l = 0, hence as long as h-l > 0 and surplusQ <=3
        while (h-l > l && getSurplusQueuedTaskCount() <= 3) {
            int mid = (l +h) >>> 1; 
            right = new Applyer(array, mid, h, right); 
            right.fork(); // create new Task
            h = mid;
        }
        // Declare sum 
        double sum = atLeaf(l, h);
        while (right != null) {
            if (right.tryUnfork()) {
                sum += right.atLeaf(right.lo, right.hi);
            } else {
                right.join();
                sum += right.result;
            }
            right = right.next;
        }
        result = sum;
    }

    public static void main(String[] args) {
        ForkJoinPool fjp = new ForkJoinPool();
        double[] anArray = {1.0, 33.035, 195.3, 1.0356, 13.20};
        double sumSquares = sumOfSquares(fjp, anArray);
        System.out.println(sumSquares);
    }
}