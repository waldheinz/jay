/*
 * SimpleSampler.java
 *
 * Created on 1. März 2006, 16:06
 */

package jay.sampling.film;

/**
 *
 * @author Matthias Treydte
 */
public class SimpleSampler extends FilmSampler {
    
    int posX, posY;
    
    /** Creates a new instance of SimpleSampler */
    public SimpleSampler(int[] extent) {
        super(extent);
        posX = minX;
        posY = minY;
    }

    public void nextSample(int[] pixel) {
        pixel[0] = posX;
        pixel[1] = posY;
        
        ++posX;
        
        if (posX > maxX) {
            posX = minX;
            ++posY;
        }
        
        if (posY > maxY) {
            posY = minY;
        }
    }
    
}
