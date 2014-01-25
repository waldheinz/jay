/*
 * Anisotropic.java
 *
 * Created on 31. Dezember 2005, 15:23
 */

package jay.materials.bxdfs;

import jay.maths.Utils;
import jay.maths.Vector;

/**
 *
 * @author Matthias Treydte <waldheinz@gmail.com>
 */
public class Anisotropic extends MicrofacetDistribution {
    
    final float ex;
    final float ey;
    
    /** Erstellt eine neue Instanz von Anisotropic */
    public Anisotropic(float x, float y) {
        ex = x;
        ey = y;
    }
    
    public float d(final Vector wh) {
        float costhetah = Math.abs(BxDF.cosTheta(wh));
        float e = (ex * wh.x * wh.x + ey * wh.y * wh.y) /
                (1.0f - costhetah * costhetah);
        return (float)Math.sqrt((ex+2) * (ey+2)) * Utils.INV_2PI *
                (float)Math.pow(costhetah, e);
    }
    
    public float pdf(final Vector wo, final Vector wi) {
        Vector h = wo.add(wi).normalized();
        // Compute PDF for \wi from Anisotropic distribution
        return d(h) / (4.0f * wo.dot(h));
    }
    
    public VectorSample sample(final Vector wo, float u1, float u2) {
        // Sample from first quadrant and remap to hemisphere to sample \wh
        
        float[] res;
        if (u1 < 0.25f) {
            res = sampleFirstQuadrant(4.0f * u1, u2);
        } else if (u1 < 0.5f) {
            u1 = 4.f * (0.5f - u1);
            res = sampleFirstQuadrant(u1, u2);
            res[0] = (float)Math.PI - res[0];
        } else if (u1 < 0.75f) {
            u1 = 4.0f * (u1 - 0.5f);
            res = sampleFirstQuadrant(u1, u2);
            res[0] += (float)Math.PI;
        } else {
            u1 = 4.0f * (1.0f - u1);
            res = sampleFirstQuadrant(u1, u2);
            res[0] = 2.0f * (float)Math.PI - res[0];
        }
        
        float phi = res[0], costheta = res[1];
        
        float sintheta = (float)Math.sqrt(Math.max(0.0f, 1.0f -
                costheta*costheta));
        Vector h = Utils.sphericalDirection(sintheta, costheta, phi);
        if (wo.dot(h) < 0.0f) h = h.neg();
        VectorSample smp = new VectorSample();
        
        // Compute incident direction by reflecting about $\wh$
        smp.wi = wo.neg().add(h.mul(2.0f * wo.dot(h)));
        
        // Compute PDF for \wi from Anisotropic distribution
        smp.pdf = d(h) / (4.0f * wo.dot(h));
        return smp;
    }
    
    /**
     * @return f[0] = phi, f[1] = costheta
     */
    float[] sampleFirstQuadrant(float u1, float u2) {
        float phi, cost;
        if (ex == ey)
            phi = (float)Math.PI * u1 * 0.5f;
        else
            phi = (float)Math.atan(Math.sqrt((ex+1)/(ey+1)) *
                    Math.tan(Math.PI * u1 * 0.5f));
        float cosphi = (float)Math.cos(phi), sinphi = (float)Math.sin(phi);
        cost = (float)Math.pow(u2, 1.0f/(ex * cosphi * cosphi +
                ey * sinphi * sinphi + 1));
        
        return new float[] {phi, cost};
    }
}
