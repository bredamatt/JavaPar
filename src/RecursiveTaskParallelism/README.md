# See this first
This folder contains different examples of Task level parallelism that extends the `RecursiveAction` class in Java. These examples requires a ForkJoinPool and some Task to be scheduled in parallel. The task is usually defined as a class with a function called `compute()` where the main logic handling the invocation of the `fork()` and `join()` methods are defined.

A general example for how the Fork-Join pattern is as follows in Java psuedocode.

``class AsyncArraySum extends RecursiveAction {

    final int[] array;
    final int LO;
    final int HI;

    // Threshold to determine whether to Fork, or not. Can be dynamic.
    int THRESHOLD;

    // Constructor 
    AsyncArraySum(int[] array, int lo, int hi) {
        this.array = array;
        this.LO = lo;
        this.HI = hi;
    }

    // Computation to be made asynchronously
    protected void compute() {
        Print("Array contents:")
        Print(array)

        if (HI - LO < THRESHOLD) {
            compute() // continue with sequential execution
        }

        else {
            // Divide problem into subtasks based on midpoint of array,
            // and invoke compute on both in parallel.

            int mid = abs((LO-HI) / 2); 

            AsyncArraySum a1 = new AsyncArraySum(array, lo, mid)
            AsyncArraySum a2 = new AsyncArraySum(array, mid, hi)

            invokeall(a1, a2);
        }
    }
}``

Here, the invokeall() approach is used. 