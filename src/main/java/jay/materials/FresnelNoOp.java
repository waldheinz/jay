/*
 * FresnelNoOp.java
 *
 * Created on 28. Dezember 2005, 21:52
 */

package jay.materials;

import jay.utils.Spectrum;

/**
 *
 * @author Matthias Treydte
 */
public class FresnelNoOp extends Fresnel {
    
    /** Creates a new instance of FresnelNoOp */
    public FresnelNoOp() {
    }

    public Spectrum eval(float cosi) {
        return new Spectrum(1, 1, 1);
    }
    
}
