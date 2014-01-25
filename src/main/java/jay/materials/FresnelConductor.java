/*
 * FresnelConductor.java
 *
 * Created on 28. Dezember 2005, 21:37
 */

package jay.materials;

import jay.utils.Spectrum;


/**
 *
 * @author trem
 */
public class FresnelConductor extends Fresnel {
    
    Spectrum eta;
    Spectrum k;
    
    /** Creates a new instance of FresnelConductor */
    public FresnelConductor(Spectrum eta, Spectrum k) {
        this.eta = eta;
        this.k = k;
    }

    public Spectrum eval(float cosi) {
        return fresnelConductor(Math.abs(cosi), eta, k);
    }
    
}
