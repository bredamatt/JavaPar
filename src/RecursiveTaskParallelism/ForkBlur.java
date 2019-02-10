/*
Example tutorial for Fork/Join, based on ExecutorService 
See https://docs.oracle.com/javase/tutorial/essential/concurrency/forkjoin.html for more 

As any Fork/Join paradigm, this distributes
tasks to worker threads in a thread pool. 
Fork/Join uses a work-stealing algorithm.
Worker threads that run out of things can
steal tasks from other threads that are busy.

Center of the FW is the ForkJoinPool class,
an extension of the AbstractExecutorServicve class.

ForkJoinPool implments the core work-stealing algorithm
and can execute ForkJoinTask processes. 

ForkJoinTask subclasses:
- RecursiveTask (returns a type)
- RecursiveAction 

Need to define code that does a segment of some work
and wrap it in either of the above subclasses (it becomes a class).
Once the ForkJointask subclass is ready, 
create the object that represents the work, and pass it 
to the invoke() method of a ForkJoinPool instance. 


NB! This is an example of how Fork / Join works.

Another example of a conccurenct algorithm is parallelSort(). 
Another example is in the java.util.streams package. 

*/ 

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import javax.imageio.ImageIO; 

public class ForkBlur extends RecursiveAction {
   
    private int[] mSource;
    private int mStart;
    private int mLength;
    private int[] mDestination; 
    private int mBlurWidth = 15;  // Processing window size, make it odd

    public ForkBlur(int[] src, int start, int length, int[] dst) {
        mSource = src;
        mStart = start;
        mLength = length; 
        mDestination = dst;
    }

    // Averages pixels from the source image, write results into destination file 
    protected void computeDirectly() {
        int sidePixels = (mBlurWidth - 1) / 2; 
        for (int index = mStart; index < mStart + mLength; index++) {
            // calculate the average 
            float rt = 0, gt = 0, bt = 0;
            for (int mi = -sidePixels; mi <= sidePixels; mi++) {
                int mindex = Math.min(Math.max(mi + index, 0), mSource.length -1);
                int pixel = mSource[mindex];
                rt += (float) ((pixel & 0x00ff0000) >> 16) / mBlurWidth;
                gt += (float) ((pixel & 0x0000ff00) >> 8) / mBlurWidth;
                bt += (float) ((pixel & 0x000000ff) >> 0) / mBlurWidth;
            }

            // Reassemble destination pixel.
            int dpixel = (0xff000000) 
            |   (((int) rt) << 16)
            |   (((int) gt) << 8) 
            |    (((int) bt) << 0);
            mDestination[index] = dpixel;
        }

    }

    protected static int sThreshold = 100000;
    
    @Override
    protected void compute() {
        if (mLength < sThreshold) {
            computeDirectly();
            return; 
        }
        int split = mLength / 2;

        // Does both the fork and the join... :-)
        invokeAll(new ForkBlur(mSource, mStart, split, mDestination),
                  new ForkBlur(mSource, mStart + split, mLength - split, mDestination));
    }


    public static BufferedImage blur(BufferedImage srcImage) {
        int w = srcImage.getWidth();
        int h = srcImage.getHeight();

        int[] src = srcImage.getRGB(0, 0, w, h, null, 0, w);
        int[] dst = new int[src.length]; 

        System.out.println("Array size is " + src.length);
        System.out.println("Threshold is " + sThreshold);

        int processors = Runtime.getRuntime().availableProcessors();
        System.out.println(Integer.toString(processors) + " processor" + (processors != 1 ? "s are " : " is ") + "available");
    
        /* 
            If the methods are in a subclas of RecursiveAction class,
            which is the case, setting up the 
            task to run in a ForKJoinPool is straighforward. 
            Follow these steps:
            1. Create a task that represents all the work to be done 
            2. Create the ForkJoinPool that will run the task
            3. Run the task

        */ 

        // Step 1
        ForkBlur fb = new ForkBlur(src, 0, src.length, dst); 
        
        // Step 2
        ForkJoinPool pool = new ForkJoinPool();
        
        long startTime = System.currentTimeMillis();
        
        // Step 3
        pool.invoke(fb);
        
        long endTime = System.currentTimeMillis();

        System.out.println("Image blur took " + (endTime - startTime) + " milliseconds.");

        BufferedImage dstImage = 
            new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            dstImage.setRGB(0, 0, w, h, dst, 0, w);

        return dstImage;
    }  

    // Plumbing follows
    public static void main(String[] args) throws Exception {
        String srcName = "red-tulips.jpg";
        File srcFile = new File(srcName);
        BufferedImage image = ImageIO.read(srcFile);
    
        System.out.println("Source image: " + srcName);

        BufferedImage blurredImage = blur(image); 

        String dstName = "blurred-tulips.jpg";
        File dstFile = new File(dstName);
        ImageIO.write(blurredImage, "jpg", dstFile);

        System.out.println("Output image: " + dstName); 
    }
}
