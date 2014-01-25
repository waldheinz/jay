/*
 * BucketOrderSpiral.java
 *
 * Created on 4. Juli 2007, 11:02
 */

package jay.sampling.buckets;

/**
 *
 * @author Matthias Treydte <waldheinz@gmail.com>
 */
public class BucketOrderSpiral implements BucketOrder {
    
    /** Creates a new instance of BucketOrderSpiral */
    public BucketOrderSpiral() {
    }

    public int[] getSequence(int nWidth, int nHeight) {
        int[] coords = new int[2 * nWidth * nHeight];
        for (int i = 0; i < nWidth * nHeight; i++) {
            int bx, by;
            int center = (Math.min(nWidth, nHeight) - 1) / 2;
            int nx = nWidth;
            int ny = nHeight;
            
            while (i < (nx * ny)) {
                nx--;
                ny--;
            }
            
            int nxny = nx * ny;
            int minnxny = Math.min(nx, ny);
            
            if ((minnxny & 1) == 1) {
                if (i <= (nxny + ny)) {
                    bx = nx - minnxny / 2;
                    by = -minnxny / 2 + i - nxny;
                } else {
                    bx = nx - minnxny / 2 - (i - (nxny + ny));
                    by = ny - minnxny / 2;
                }
            } else {
                if (i <= (nxny + ny)) {
                    bx = -minnxny / 2;
                    by = ny - minnxny / 2 - (i - nxny);
                } else {
                    bx = -minnxny / 2 + (i - (nxny + ny));
                    by = -minnxny / 2;
                }
            }
            
            coords[2 * i + 0] = bx + center;
            coords[2 * i + 1] = by + center;
        }
        
        return coords;
    }
    
}
