// The class ASum 
public static class ASum extends RecursiveAction {
    int[] A; // Input array 
    int LO, HI; // subrange 
    int SUM; // return value 

    @Override 
    protected void compute() {
        SUM = 0;
        for (int i = LO; i <= HI; i++) SUM +=A[i]; 
    } // compute() method 
}