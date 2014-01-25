/*
 * BucketOrder.java
 *
 * Created on 3. Juli 2007, 10:38
 */

package jay.sampling.buckets;

/**
 *
 * @author Matthias Treydte <waldheinz@gmail.com>
 */
public interface BucketOrder {
    
    /**
     * Returns the order in which the buckets shall be rendered.
     * These are interleaved x, y coordinates.
     */
    public int[] getSequence(int nWidth, int nHeight);
    
}
