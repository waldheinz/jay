/*
 * BxDF.java
 *
 * Created on 16. Dezember 2005, 19:10
 */

package jay.materials.bxdfs;

import jay.materials.*;
import jay.maths.Utils;
import jay.maths.Vector;
import jay.utils.Spectrum;

/**
 * Alle Berechnungen finden in einem eigenen Koordinatensystem statt.
 * Die Umrechnung in dieses übernimmt die {@link BSDF}. Die z - Achse
 * entspricht der Normalen der Geometrie.
 *
 * Theta = Winkel mit der z - Achse;
 * Phi   = Winkel mit der x - Achse, nachdem der Vektor in die
 * xy - Ebene projeziert wurde.
 *
 * @author Matthias Treydte
 */
public abstract class BxDF {
    
    public final static int REFLECTION       = 1 << 0;
    public final static int TRANSMISSION     = 1 << 1;
    public final static int DIFFUSE          = 1 << 2;
    public final static int GLOSSY           = 1 << 3;
    public final static int SPECULAR         = 1 << 4;
    public final static int ALL_TYPES        = DIFFUSE | GLOSSY | SPECULAR;
    public final static int ALL_REFLECTION   = ALL_TYPES | REFLECTION;
    public final static int ALL_TRANSMISSION = ALL_TYPES | TRANSMISSION;
    public final static int ALL              = ALL_REFLECTION | ALL_TRANSMISSION;
    
    protected final int type;
    
    public BxDF(int type) {
        this.type = type;
    }
    
    public boolean matches(int otherType) {
        return ((type & otherType) == type);
    }
    
    /**
     * Wieviel Licht wird von wi nach wo reflektiert?
     */
    public abstract Spectrum eval(final Vector wi, final Vector wo);
    
    public BxDFSample sample(final Vector wo, float u1, float u2) {
        BxDFSample sample = new BxDFSample();
        sample.wi = Utils.cosineSampleHemisphere(u1, u2);
        if (wo.z < 0.0f) sample.wi = new Vector(
                sample.wi.x, sample.wi.y, -sample.wi.z);
        sample.pdf = pdf(wo, sample.wi);
        sample.type = type;
        sample.f = eval(wo, sample.wi);
        return sample;
    }
    
    public float pdf(final Vector wo, final Vector wi) {
        if (BSDF.sameHemisphere(wo, wi))
            return Math.abs(wi.z) * Utils.INV_PI;
        else return 0.0f;
    }
    
    static float sinTheta(final Vector v) {
        return (float)Math.sqrt(Math.max(0.0, 1.0 - v.z * v.z));
    }
    
    /*
     * sin^2(theta)
     */
    static float sinTheta2(final Vector v) {
        //return 1.0f - cosTheta(v) * cosTheta(v);
        return v.x * v.x + v.y * v.y;
    }
    
    static float cosTheta(final Vector v) { return v.z; }
    
    static float cosPhi(final Vector w) { return w.x / sinTheta(w); }
    
    static float sinPhi(final Vector w) { return w.y / sinTheta(w); }
    
    public Spectrum rho() {
        final int nSamples = 16;
        final float pdf_o = Utils.INV_2PI;
        final float[] samples = new float[4 * nSamples];
        jay.sampling.Utils.latinHyperCube(samples, nSamples, 4);
        
        Spectrum r = Spectrum.BLACK;
        for (int i = 0; i < nSamples; ++i) {
            // Estimate one term of $\rho_{hh}$
            Vector wo = jay.sampling.Utils.uniformSampleHemisphere(
                    samples[4*i], samples[4*i+1]);
            
            
            BxDFSample smp = sample(wo, samples[4*i+2], samples[4*i+3]);
            
            if (smp.pdf > 0.0) {
                r = r.add( smp.f.scale(Math.abs(wo.z * smp.wi.z) / 
                        (pdf_o / smp.pdf)));
            }
        }
        
        return r.scale(1.0f / ((float)Math.PI * nSamples));
    }

    public Spectrum rho(final Vector wo) {
        final int nSamples = 16;
        final float[] samples = new float[2 * nSamples];
        jay.sampling.Utils.latinHyperCube(samples, nSamples, 2);
        
        Spectrum r = Spectrum.BLACK;
        for (int i=0; i < nSamples; i++) {
            BxDFSample smp = sample(wo, samples[i*2], samples[i*2+1]);
            if (smp.pdf > 0)
                r = r.add(smp.f.scale(1.0f / smp.pdf).scale(cosTheta(wo)));
        }
        
        return r.scale(1.0f / nSamples);
    }
    
}
