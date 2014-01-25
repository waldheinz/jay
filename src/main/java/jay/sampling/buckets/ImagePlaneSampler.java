/*
 * ImagePlaneSampler.java
 *
 * Created on 3. Juli 2007, 10:53
 */

package jay.sampling.buckets;

import jay.sampling.Film;

/**
 *
 * @author Matthias Treydte <waldheinz@gmail.com>
 */
public class ImagePlaneSampler {
    
    public final static int BUCKET_WIDTH  = 16;
    public final static int BUCKET_HEIGHT = 16;
    
    private BucketOrder bucketOrder = new BucketOrderSpiral();
    private int[] order;
    private int numX, numY;
    private Film film;
    private int minX, maxX, minY, maxY;
    private int currentBucket;
    
    /** Counts the pass we are currently doing */
    private int pass;
    
    /** Creates a new instance of ImagePlaneSampler */
    public ImagePlaneSampler(Film film) {
        this.film = film;
        init();
    }
    
    private void init() {
        int[] extents = film.getSamplingExtent();
        minX = extents[0]; maxX = extents[1];
        minY = extents[2]; maxY = extents[3];
        final int nx = (int)Math.ceil((float)(maxX - minX) / BUCKET_WIDTH);
        final int ny = (int)Math.ceil((float)(maxY - minY) / BUCKET_HEIGHT);
        order = bucketOrder.getSequence(nx, ny);
        pass = currentBucket = 0;
    }
    
    public void setBucketOrder(BucketOrder bucketOrder) {
        this.bucketOrder = bucketOrder;
        init();
    }
    
    public synchronized Bucket nextBucket() {
        final int x = minX + order[currentBucket++] * BUCKET_WIDTH;
        final int y = minY + order[currentBucket++] * BUCKET_HEIGHT;
        final int w = Math.min(BUCKET_WIDTH, maxX  - x);
        final int h = Math.min(BUCKET_HEIGHT, maxY - y);
        
        if (currentBucket >= order.length) {
            currentBucket = 0;
            pass++;
        }
        
        return new Bucket(x, y, w, h);
    }
    
    public static class Bucket {
        private final int x, y, w, h;
        private int px, py;
        
        public Bucket(int x, int y, int w, int h) {
            this.x = x; this.y = y;
            this.w = w; this.h = h;
            px = x;
            py = y;
        }
        
        public boolean nextPixel(int[] pixel) {
            pixel[0] = px;
            pixel[1] = py;
            
            if (++px > x+w) {
                px = x;
                py++;
            }
            
            if (py > y+h)
                return false;
            
            return true;
        }
        
        public String toString() {
            return "Bucket [x=" + x + ", y=" + y + "]";
        }
    }
}
