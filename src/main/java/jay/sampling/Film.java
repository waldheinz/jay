/*
 * Film.java
 *
 * Created on 29. Dezember 2005, 16:55
 */

package jay.sampling;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jay.maths.Ray;
import jay.utils.Spectrum;

/**
 *
 * @author Matthias Treydte
 */
public abstract class Film {
    
    public final int xRes;
    public final int yRes;
    
    private boolean busy;
    
    public Film(int xRes, int yRes) {
        if (xRes <= 0 || yRes <= 0)
            throw new IllegalArgumentException(
                    "Invalid Film Resolution (<=0).");
        
        this.xRes = xRes;
        this.yRes = yRes;
        this.busy = false;
    }

    /**
     * Tries to add the given samples to this Film. If another thread
     * is currently flushing samples to this film, this method returns
     * immediately and no samples are flushed.
     *
     * @param samples the samples to add to this film.
     * @return {@literal true}, if the samples were successfully flushed.
     * @see #addSamples(java.util.List) 
     */
    public boolean tryAddSamples(List<ImageSample> samples) {
        synchronized (this) {
            if (busy) return false;
            busy = true;
        }
        
        for (ImageSample s : samples)
            addSample(s.px, s.py, s.ray, s.c);
            
        busy = false;

        synchronized (this) {
            this.notify();
        }
        
        return true;
    }

    /**
     * Adds the given samples to this film. If another thread currently
     * flushes samples, this method blocks until the operation completed.
     *
     * @param samples the samples to add to this film.
     */
    public void addSamples(List<ImageSample> samples) {
        synchronized (this) {
            while (busy) {
                try {
                    this.wait();
                } catch (InterruptedException ex) {
                    Logger.getLogger(Film.class.getName()).log(
                            Level.SEVERE, null, ex);
                }
            }
            
            busy = true;
        }

        for (ImageSample s : samples)
            addSample(s.px, s.py, s.ray, s.c);

        busy = false;

        synchronized (this) {
            this.notify();
        }
    }

    public abstract int[] getSamplingExtent();

    /**
     * Adds a sample to this film. This method is not synchronized.
     *
     * @param x
     * @param y
     * @param ray
     * @param c
     */
    public abstract void addSample(float x, float y, Ray ray, Spectrum c);
}
