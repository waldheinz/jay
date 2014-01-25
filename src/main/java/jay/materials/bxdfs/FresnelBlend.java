/*
 * FresnelBlend.java
 *
 * Created on 31. Dezember 2005, 14:55
 */

package jay.materials.bxdfs;

import jay.materials.BSDF;
import jay.materials.BxDFSample;
import jay.maths.Utils;
import jay.maths.Vector;
import jay.utils.Spectrum;
import jay.utils.SpectrumCalc;

/**
 * 
 *
 * @author Matthias Treydte <waldheinz@gmail.com>
 */
public class FresnelBlend extends BxDF {
    
    final Spectrum rd;
    final Spectrum rs;
    final MicrofacetDistribution dist;
    
    /** Erstellt eine neue Instanz von FresnelBlend */
    public FresnelBlend(final Spectrum rd,final Spectrum rs,
            final MicrofacetDistribution dist) {
        
        super(BxDF.GLOSSY);
        this.rd = rd;
        this.rs = rs;
        this.dist = dist;
    }
    
    public Spectrum eval(final Vector wo, final Vector wi) {
        
        /* diffuse part */
        SpectrumCalc d = new SpectrumCalc(rd.scale(28.0f / 23.0f * (float)Math.PI));
        d.scale(rd);
        d.scale(Spectrum.WHITE.sub(rs));
        d.scale(1.0f - (float)Math.pow(1.0f - 0.5f * Math.abs(BxDF.cosTheta(wi)), 5));
        d.scale(1.0f - (float)Math.pow(1.0f - 0.5f * Math.abs(BxDF.cosTheta(wo)), 5));

        Vector h = wi.add(wo).normalized();

        Spectrum spec =
                schlickFresnel(wi.dot(h)).scale(
                8 * (float)Math.PI * Utils.absdot(wi, h) *
                Math.max(
                Math.abs(BxDF.cosTheta(wi)), Math.abs(BxDF.cosTheta(wo)))).invDiv(dist.d(h));

        return d.s.add(spec);
    }
    
    /**
     * FIXME
     */
    public Spectrum schlickFresnel(float costheta) {
        return rs.add(Spectrum.WHITE.sub(rs).scale(
                (float)Math.pow(1.0f - costheta, 0.5f)));
    }
    
    public float pdf(final Vector wo, final Vector wi) {
        if (!BSDF.sameHemisphere(wo, wi)) return 0.0f;
        return 0.5f * (Math.abs(wi.z) * Utils.INV_PI + dist.pdf(wo, wi));
    }
    
    public BxDFSample sample(final Vector wo, float u1, float u2) {
        BxDFSample smp = new BxDFSample();
        smp.type = this.type;
        
        if (u1 < 0.5) {
            u1 = 2.0f * u1;
            /* Cosine-sample the hemisphere, flipping the direction if necessary */
            smp.wi = Utils.cosineSampleHemisphere(u1, u2);
            if (wo.z < 0.0f) smp.wi = new Vector(
                    smp.wi.x, smp.wi.y, -smp.wi.z);
        } else {
            u1 = 2.0f * (u1 - 0.5f);
            VectorSample s = dist.sample(wo, u1, u2);
            smp.wi = s.wi;
            
            if (!BSDF.sameHemisphere(s.wi, wo)) {
                smp.f = Spectrum.BLACK;
                return smp;
            }
            
        }
        
        smp.pdf = pdf(wo, smp.wi);
        smp.f = eval(wo, smp.wi);
        
        return smp;
    }
}
