/*
 * SpecularTransmission.java
 *
 * Created on 4. Januar 2006, 02:32
 */

package jay.materials.bxdfs;

import jay.materials.*;
import jay.maths.Vector;
import jay.utils.Spectrum;

/**
 *
 * @author Matthias Treydze
 */
public class SpecularTransmission extends BxDF {
    
    /**
     * Leiter sind niemals durchsichtig, daher kann man sich hier
     * auf die Dielectric - Variante von Fresnel festlegen.
     */
    final FresnelDielectric fresnel;
    final Spectrum trans;
    final float etai;
    final float etat;
    
    /** Erstellt eine neue Instanz von SpecularTransmission */
    public SpecularTransmission(final Spectrum t, float ei, float et) {
        super(BxDF.SPECULAR);
        fresnel = new FresnelDielectric(ei, et);
        etai = ei;
        etat = et;
        trans = t;
    }
    
    public Spectrum eval(final Vector wi, final Vector wo) {
        return Spectrum.BLACK;
    }
    
    public float pdf(final Vector wo, final Vector wi) {
        return 0.0f;
    }
    
    public BxDFSample sample(final Vector wo, float u1, float u2) {
        BxDFSample smp = new BxDFSample();
        smp.type = type;
        // Figure out which $\eta$ is incident and which is transmitted
        boolean entering = cosTheta(wo) > 0.0;
        float ei, et;
        
        if (entering) {
            ei = etai;
            et = etat;
        } else {
            ei = etat;
            et = etai;
        }
        // Compute transmitted ray direction
        float sini2 = sinTheta2(wo);
        float eta = ei / et;
        float sint2 = eta * eta * sini2;
        // Handle total internal reflection for transmission
        
        if (sint2 > 1.0) {
            /* totale interne Reflektion */
            smp.f = Spectrum.BLACK;
            smp.wi = new Vector();
            return smp;
        }
        
        float cost = (float)Math.sqrt(Math.max(0.0f, 1.0f - sint2));
        if (entering) cost = -cost;
        float sintOverSini = eta;
        smp.wi = new Vector(
                sintOverSini * -wo.x,
                sintOverSini * -wo.y,
                cost);
        smp.pdf = 1.0f;
        
        Spectrum F = fresnel.eval(cosTheta(wo));
        
        smp.f = Spectrum.WHITE.sub(F).scale(trans).scale(
                (ei*ei)/(et*et) / Math.abs(cosTheta(smp.wi)));
        
//        return (ei*ei)/(et*et) * (Spectrum(1.)-F) * T /
//                fabsf(CosTheta(*wi));
        
        return smp;
    }
    
    
}
