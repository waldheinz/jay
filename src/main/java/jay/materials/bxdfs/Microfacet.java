/*
 * Microfacet.java
 *
 * Created on 31. Dezember 2005, 15:00
 */

package jay.materials.bxdfs;

import jay.materials.*;
import jay.maths.Utils;
import jay.maths.Vector;
import jay.utils.Spectrum;

/**
 *
 * @author Matthias Treydze
 */
public class Microfacet extends BxDF {
    
    Spectrum r = new Spectrum(0.6f);
    MicrofacetDistribution dist = new Blinn(4);
    Fresnel fresnel = new FresnelDielectric(1, 1);
                                                    
    /** Erstellt eine neue Instanz von Microfacet */
    public Microfacet() {
        super(BxDF.GLOSSY | BxDF.REFLECTION);
    }
    
    public Microfacet(final Spectrum refl, final Fresnel fresnel,
          final MicrofacetDistribution dist) {
        
        super(BxDF.GLOSSY | BxDF.REFLECTION);
        this.r = refl;
        this.fresnel = fresnel;
        this.dist = dist;
    }
    
    public Spectrum eval(final Vector wo, final Vector wi) {
        float ctO = Math.abs(cosTheta(wo));
        float ctI = Math.abs(cosTheta(wi));
        Vector wh = wi.add(wo).normalized();
        Spectrum f = fresnel.eval(wi.dot(wh));

        return r.scale(f.scale(
                1.0f / (4.0f * ctO * ctI)).scale(
                g(wo, wi, wh) * dist.d(wh)));
    }
    
    /**
     * Der Term für die geometrische Verdeckung.
     */
    public float g(final Vector wo, final Vector wi, final Vector wh) {
        float ndoth = Math.abs(cosTheta(wh));
        float ndotwo = Math.abs(cosTheta(wo));
        float ndotwi = Math.abs(cosTheta(wi));
        float wodoth = Utils.absdot(wo, wh);
        
        return Math.min(1.0f, Math.min(
                2.0f * ndoth * ndotwo / wodoth,
                2.0f * ndoth * ndotwi / wodoth));
    }

    public BxDFSample sample(final Vector wo, float u1, float u2) {
        final VectorSample tmp = dist.sample(wo, u1, u2);
        final BxDFSample smp = new BxDFSample();
        
        smp.wi = tmp.wi;
        smp.pdf = tmp.pdf;
        smp.f = eval(wo, smp.wi);
        
        return smp;
    }
    
}
