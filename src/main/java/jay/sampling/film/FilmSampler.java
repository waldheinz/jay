/*
 * FilmSampler.java
 *
 * Created on 1. März 2006, 15:54
 */

package jay.sampling.film;

/**
 *
 * @author trem
 */
public abstract class FilmSampler {
    
    final int minX, minY, maxX, maxY;
    
    /** Creates a new instance of FilmSampler */
    public FilmSampler(int[] samplingExtent) {
        minX = samplingExtent[0];
        maxX = samplingExtent[1];
        minY = samplingExtent[2];
        maxY = samplingExtent[3];
    }
    
    public abstract void nextSample(int[] pixel);
}
