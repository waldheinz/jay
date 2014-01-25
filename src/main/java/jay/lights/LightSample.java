/*
 * LightSample.java
 *
 * Created on 27. Dezember 2005, 23:08
 */

package jay.lights;

import jay.maths.Vector;
import jay.utils.Spectrum;

/**
 *
 * @author Matthias Treydte
 */
public class LightSample {
    
    /** Strahlungsintensität */
    public Spectrum r;
    
    public Vector wo;
    
    public float pdf;
    
    public boolean deltaLight;
    
    public VisibilityTester vt = new VisibilityTester();
}
