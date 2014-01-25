/*
 * TiledSampler.java
 *
 * Created on 1. März 2006, 15:57
 */

package jay.sampling.film;

/**
 *
 * @author Matthias Treydte
 */
public class TiledSampler extends FilmSampler {
    
    final int tileWidth, tileHeight;
    int tileNumX, tileNumY;
    int pixelX, pixelY;
    
    public TiledSampler(int[] extent) {
        this(extent, 16, 16);
    }
    
    public TiledSampler(int[] extent, int tileWidth, int tileHeight) {
        super(extent);
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        tileNumX = tileNumY = 0;
        pixelX = extent[0];
        pixelY = extent[1];
    }
    
    public void nextSample(int[] pixel) {
        
    }
}
