/*
 * SpecularReflection.java
 *
 * Created on 28. Dezember 2005, 19:45
 */

package jay.materials.bxdfs;

import jay.materials.*;
import jay.maths.Vector;
import jay.utils.Spectrum;

/**
 *
 * @author Matthias Treydte <waldheinz@gmail.com>
 */
public class SpecularReflection extends BxDF {
    
    private final Fresnel fresnel;
    private final Spectrum r;
    
    /** Creates a new instance of SpecularReflection */
    public SpecularReflection(Spectrum r, Fresnel f) {
        super(BxDF.SPECULAR);
        fresnel = f;
        this.r = r;
    }
    
    /**
     * Gibt immer 0 zurück, da es unmöglich ist, dass zwei zufällige
     * Winkel den exakten Winkel für die Reflektion ergeben.
     */
    @Override
    public Spectrum eval(final Vector wi, final Vector wo) {
        return Spectrum.BLACK;
    }
    
    @Override
    public BxDFSample sample(final Vector wo, float u1, float u2) {
        BxDFSample sample = new BxDFSample();
        
        sample.pdf = 1.0f;
        sample.wi = new Vector(-wo.x, -wo.y, wo.z);
        sample.f = fresnel.eval(cosTheta(wo)).scale(r).
                scale(1.0f / Math.abs(cosTheta(sample.wi)));
        sample.type = type;
        
        return sample;
    }
    
}
