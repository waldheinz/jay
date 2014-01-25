/*
 * Blinn.java
 *
 * Created on 31. Dezember 2005, 15:14
 */

package jay.materials.bxdfs;

import jay.maths.Utils;
import jay.maths.Vector;

/**
 *
 * @author Matthias Treydte <waldheinz@gmail.com>
 */
public class Blinn extends MicrofacetDistribution {
    
    final float e;
    
    /**
     * Erstellt eine neue Instanz von Blinn
     *
     * @param e der Exponent der Verteilung
     */
    public Blinn(float e) {
        this.e = e;
    }
    
    public float d(final Vector wh) {
        float costhetah = Math.abs(BxDF.cosTheta(wh));
        return (e+2) * Utils.INV_2PI * (float)Math.pow(
                Math.max(0.0f, costhetah), e);
    }
    
    public float pdf(final Vector wo, final Vector wi) {
        Vector h = wo.add(wi).normalized();
        float costheta = Math.abs(h.z);
        float blinn_pdf = ((e + 2.0f) *
                (float)Math.pow(costheta, e)) /
                (2.f * (float)Math.PI * 4.0f * (wo.dot(h)));
        return blinn_pdf;
    }
    
    public VectorSample sample(final Vector wo, float u1, float u2) {
        // Compute sampled half-angle vector $\wh$ for Blinn distribution
        float costheta = (float)Math.pow(u1, 1.0f / (e+1));
        float sintheta = (float)Math.sqrt(Math.max(0.0f, 1.0f -
                costheta*costheta));
        float phi = u2 * 2.0f * (float)Math.PI;
        
        /* Compute incident direction by reflecting about wh */
        final VectorSample smp = new VectorSample();
        
        Vector h = Utils.sphericalDirection(sintheta, costheta, phi);
        if (wo.dot(h) < 0.0f) h = h.neg();
        
        smp.wi = wo.neg().add(h.mul(2.0f * wo.dot(h)));
        
        // Compute PDF for \wi from Blinn distribution
        smp.pdf = ((e + 2.0f) * (float)Math.pow(costheta, e)) /
                (2.0f * (float)Math.PI * 4.0f * wo.dot(h));
        
        return smp;
    }
    
}
