/*
 * BxDFSample.java
 *
 * Created on 28. Dezember 2005, 20:41
 */

package jay.materials;

import jay.maths.Vector;
import jay.utils.Spectrum;

/**
 *
 * @author Matthias Treydte
 */
public class BxDFSample {
    public float pdf;
    public Spectrum f;
    public Vector wi;
    
    /**
     * One of the constants from {@link BxDF}.
     */
    public int type;
}
