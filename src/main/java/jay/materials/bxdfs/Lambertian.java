/*
 * Lambertian.java
 *
 * Created on 1. Januar 2006, 17:44
 */

package jay.materials.bxdfs;

import jay.maths.*;
import jay.utils.Spectrum;

/**
 *
 * @author Matthias Treydte <waldheinz@gmail.com>
 */
public class Lambertian extends BxDF {
    
    private final Spectrum rDivPi;
    private final Spectrum r;
    
    /** Erstellt eine neue Instanz von Lambertian */
    public Lambertian(final Spectrum r) {
        super(BxDF.DIFFUSE | BxDF.REFLECTION);
        this.r = r;
        this.rDivPi = r.scale(Utils.INV_PI);
    }

    public Spectrum eval(final Vector wi, final Vector wo) {
        return rDivPi;
    }

    public Spectrum rho() {
        return r;
    }
    
}
