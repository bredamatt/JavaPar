# See this first
This folder contains different examples of Task level parallelism that extends the `RecursiveAction` class in Java. These examples requires a ForkJoinPool and some Task to be scheduled in parallel to be tested. For example, in your `main()` method, you may need to create the ForkJoinPool and assign the Task (class) to it. 

The task is usually defined as a class with a method called `compute()` where the main logic handling the invocation of the `fork()` and `join()` methods are defined. The fork(), or join() methods can either be declared directly, or they may be handled automatically by the JVM using the `invoke()` method. For multiple tasks to be scheduled together, use `invokeall(task1, task2, ... taskN)`

A general example for how the Fork-Join pattern is as follows in Java psuedocode.

```java
    class AsyncArraySum extends RecursiveAction {

    final int[] array;
    final int LO;
    final int HI;
    final int SUM; 

    // Threshold to determine whether to Fork, or not. Can be dynamic.
    int THRESHOLD;

    // Constructor 
    AsyncArraySum(int[] array, int lo, int hi) {
        this.array = array;
        this.LO = lo;
        this.HI = hi;
    }

    // Computation to be made asynchronously
    @Override
    protected void compute() {
        SUM = 0;

        if (HI - LO < THRESHOLD) {
            for (int i = LO; i <= HI; i++)
                SUM += array[i];
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
}
```

Above, the `invokeall(task1, task2, ..., taskN) `approach is shown. An alternative example using `fork()` and `join()` would look like this: 

```java
  // Computation to be made asynchronously
    @Override
    protected void compute() {
        SUM = 0;
        if (HI - LO < THRESHOLD) {
            for (int i = LO; i <= HI; i++)
                SUM += array[i];
        }
        else {
            // Divide problem into subtasks based on midpoint of array,
            // and invoke compute on both in parallel.
            int mid = LO + ((LO-HI)/ 2);
            AsyncArraySum a1 = new AsyncArraySum(array, lo, mid);
            AsyncArraySum a2 = new AsyncArraySum(array, mid, hi);
            a1.fork(); // async
            a2.compute(); // RECURSIVE ^^
            a1.join(); // finish 
            SUM = a1.sum + a2.sum; // Sum the sums of a1 and a2
        }
    }
```

Another neat feature is the ability to use the `ForkJoinPool`property and define the number of cores we want to use for parallelism. This can be done as follows:

```java 

System.setproperty("java.util.concurrent.ForkJoinPool.common.Parallelism", "numcores");

```