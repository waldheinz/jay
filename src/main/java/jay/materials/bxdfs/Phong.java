/*
 * Phong.java
 *
 * Created on 16. Dezember 2005, 18:04
 */

package jay.materials.bxdfs;

import jay.materials.*;
import jay.maths.Utils;
import jay.maths.Vector;
import jay.scene.primitives.DifferentialGeometry;
import jay.utils.Spectrum;

/**
 * Modifiziertes Phong - Beleuchtungsmodell.
 *
 * @author Matthias Treydte
 */
public class Phong extends BxDF {
    
    /**
     * vorskaliert: Kd / Pi
     */
    private final Spectrum kds;
    private final Spectrum kd;
    private final Spectrum ks;
    private final float exp;
    
    public Phong(final Spectrum kd, final Spectrum ks, float exp) {
        super((exp > 0.0f)?BxDF.GLOSSY:BxDF.DIFFUSE);
        this.kd = kd;
        this.kds = kd.scale(Utils.INV_PI);
        this.ks = ks;
        this.exp = exp;
    }
    
    @Override
    public Spectrum eval(final Vector wi, final Vector wo) {
        
        if (exp > 0.0f) {
            Vector h = wi.add(wo).normalized();
            
            Spectrum specular = kds.scale(
                (float)((exp+2)/(2) *
                schlick(h.z, exp)));
            return kds.add(specular);
        } else {
            return kds;
        }
    }
    
    /**
     * Schlick's Approximation. Gibt ca. x^n zurück.
     * FIXME: Is this approximation really that bad or have
     *      I got something wrong?
     */
    public static float schlick(float x, float n) {
        return (float)Math.pow(x, n);
        //return (x / (n - n*x + x));
    }
    
    @Override
    public BxDFSample sample(final Vector wo, float u1, float u2) {
        BxDFSample sample = new BxDFSample();
        
        final float pd = kd.y();
        final float ps = ks.y();
        final float u = Utils.rand();
        
        if (u < pd) {
            /* diffus */
            sample.wi = Utils.cosineSampleHemisphere(u1, u2);
            if (wo.z < 0.0f) sample.wi = new Vector(sample.wi.x,
                    sample.wi.y, -sample.wi.z);
            
            sample.type = BxDF.DIFFUSE;
            sample.pdf = Math.abs(wo.z) * Utils.INV_PI;
            sample.f = eval(wo, sample.wi);
            
            return sample;
        } else if (u < (pd + ps)) {
            /* spekular */
            sample.type = BxDF.GLOSSY;
            float costheta = (float)Math.pow(u1, 1.0f / (0.8f * (exp + 1)));
            float sintheta = (float)Math.sqrt(
                    Math.max(1 - costheta * costheta, 0));
            float phi = u2 * 2.0f * (float)Math.PI;
            
            Vector R = new Vector(-wo.x, -wo.y, wo.z);
            Vector Ruv[] = Utils.coordinateSystem(R);

            sample.wi = Utils.sphericalDirection(
                    sintheta, costheta, phi, Ruv[0], Ruv[1],
                    R);
            
            if (sample.wi.z < 0) {
                sample.pdf = 0.0f;
                sample.f = Spectrum.BLACK;
                return sample;
            } else {
                sample.pdf = (0.8f * exp + 1) * (float)Math.pow(costheta, exp);
                sample.f = eval(wo, sample.wi);
            }
            
            return sample;
        } else {
            /* nichts */
            sample.f = Spectrum.BLACK;
            sample.type = 0;
            sample.wi = wo;
            return sample;
        }
        
    }
    
}
