/*
 * ImageSample.java
 *
 * Created on 21. Juni 2007, 15:56
 */

package jay.sampling;

import jay.maths.Ray;
import jay.utils.Spectrum;

/**
 * The basic bucket of irradiance which can be added
 * to a film.
 *
 * @author Matthias Treydte <waldheinz@gmail.com>
 */
public class ImageSample {
    
    /** x - coordinate in image plane */
    public final float px;
    
    /** y - coordinate in image plane */
    public final float py;
    
    /** The camera ray which led to this sample */
    public final Ray ray;
    
    /** The measured scene radiance */
    public final Spectrum c;
    
    /** Creates a new instance of ImageSample */
    public ImageSample(float px, float py, final Ray ray, final Spectrum c) {
        this.px = px;
        this.py = py;
        this.ray = ray;
        this.c = c;
    }
    
}
