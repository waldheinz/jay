/*
 * InstantGIIntegrator.java
 *
 * Created on 24. Juli 2007, 18:39
 */

package jay.integrators;

import java.util.ArrayList;
import java.util.logging.Logger;
import jay.lights.Light;
import jay.lights.LightRaySample;
import jay.lights.VisibilityTester;
import jay.materials.BSDF;
import jay.materials.BxDFSample;
import jay.materials.bxdfs.BxDF;
import jay.maths.*;
import jay.sampling.Film;
import jay.sampling.Stratified2D;
import jay.scene.Scene;
import jay.scene.primitives.Intersection;
import jay.utils.Spectrum;

/**
 * Instant global illumination as described by Kollig and Keller
 * in "Illumination in the Presence of Weak Singularities". This
 * includes bias compensation by doing path tracing steps.
 *
 * @author Matthias Treydte <waldheinz@gmail.com>
 */
public final class InstantGIIntegrator extends ClassicSurfaceIntegrator {
    
    private float c = 0.005f;
    
    /**
     * Threshold on the irradiance caused by an VPL.
     * if the irradiance's y value is smaller than this,
     * the shadow ray to the corresponding VPL is skipped
     * with a hard-coded probability.
     */
    protected final float rrThreshold = 0.05f;
    private int vplCount = 24;
    private int setCount = 4;
    private VPL[][] vpls;
    
    private static Logger log = Logger.getLogger(InstantGIIntegrator.class.getName());

    /** square root of the number of bias samples to do */
    private int sqBiasSamples = 3;
    
    /** Creates a new instance of InstantGIIntegratot */
    public InstantGIIntegrator(final Film film) {
        super(film);
    }

    public Spectrum traceRay(final Ray ray, Scene s) {
        
        final Intersection is = s.nearestIntersection(ray);
        if (is == null) return Spectrum.BLACK;
        
        final BSDF bsdf = is.getBSDF();
        Vector wo = ray.d.neg();
        final float b = c / bsdf.rho(wo, BxDF.ALL_REFLECTION).getMaximum();
        
        /* sample direct illumination and VPLs */
        Spectrum L = L(is, wo, s, bsdf, b);
        
        /* respect directly hit light sources */
        L = L.add(is.le(ray.d.neg()));
        
        /* bias compensation */
        Stratified2D samples = new Stratified2D(sqBiasSamples, sqBiasSamples);
        float uv[] = new float[2];
        final float invBiasSamples = 1.0f / (sqBiasSamples * sqBiasSamples);
        bias: while (samples.nextSample(uv)) {
            BxDFSample bsmp = bsdf.sample(ray.d.neg(), uv[0], uv[1], BxDF.ALL);
            Point bp = is.dg.p;
            Ray bray = new Ray(bp, bsmp.wi);
            float cosThetaX = Math.abs(is.dg.nn.dot(bsmp.wi));
            bray.tmax = (float)Math.sqrt(cosThetaX / b);
            Intersection bis = s.nearestIntersection(bray);
            Spectrum bthru = bsdf.eval(ray.d.neg(), bsmp.wi).scale(invBiasSamples / bsmp.pdf);
            
            while (bis != null) {
                
                /* check if this event was accounted for before */
                final Vector bd = bp.sub(bis.dg.p);
                final float d2 = bd.lengthSquared();
                final float cosThetaY = Math.abs(bis.dg.nn.dot(bsmp.wi));
                final float bg = (cosThetaX * cosThetaY) / d2;
                if (bg < b)
                    continue bias;
                
                final BSDF bbsdf = bis.getBSDF();
                wo = bray.d.neg();
                L = L.add(L(bis, wo, s, bbsdf, b)
                        .scale(bthru).scale((bg - b) / bg));
                
                bsmp = bbsdf.sample(wo, Utils.rand(), Utils.rand(), BxDF.ALL);
                bray = new Ray(bis.dg.p, bsmp.wi);
                bthru = bsmp.f.scale(bthru.scale(1.0f / bsmp.pdf));
                cosThetaX = bis.dg.nn.dot(wo);
                bis = s.nearestIntersection(bray);
                
                /* russian roulette */
                final float rrProb = 0.2f;
                if (Utils.rand() > rrProb)
                    continue bias;
                
                bthru = bthru.scale(1.0f / rrProb);
            }
        }
        
        return L;
    }

    /**
     * Samples the virtual point lights to compute the
     * radiance emitted along the given Ray. Additionally,
     * a light source is sampled for direct illumination.
     */
    private Spectrum L(Intersection is, Vector wo, Scene s, BSDF bsdf, float b) {
        final int set = (int)(Utils.rand() * vpls.length);
        Spectrum L = uniformSampleOneLight(s, wo, is.dg.p, is.dg.nn, bsdf);
        final float lightNumCompensate = 1.0f / vpls[set].length;

        for (final VPL vpl : vpls[set]) {
            final float d2 = vpl.p.sub(is.dg.p).lengthSquared();
            final Vector wi = vpl.p.sub(is.dg.p).mul(1.0f / (float)Math.sqrt(d2));
            final float g = (Utils.absdot(wi, is.dg.nn) *
                  Utils.absdot(wi, vpl.n)) / d2;
            
            Spectrum f = bsdf.eval(wi, wo);
            
            Spectrum l = vpl.l.scale(vpl.bsdf.eval(vpl.wi, wi.neg()));
            l = l.scale(lightNumCompensate).scale(f).scale(Math.min(b, g));
            if (l.isBlack()) continue;
            
            if (l.y() < rrThreshold) {
                final float prob = 0.1f;
                if (Utils.rand() > prob) continue;
                l = l.scale(1.0f / prob);
            }
            
            if (new VisibilityTester(vpl.p, is.dg.p).isVisible(s))
                L = L.add(l);
        }
        
        return L;
    }
    
    @Override
    public void prepare(Scene scene, Film film) {
        super.prepare(scene, film);
        log.info("Preparing instant GI integrator");
        
        vpls = new VPL[setCount][];
        
        for (int s=0; s < setCount; s++) {
            ArrayList<VPL> svpls = new ArrayList<VPL>();
            Light l = scene.getLight(0);
            
            for (int num=0; num < vplCount; num++) {
                LightRaySample lrs = l.sampleRay(scene, Utils.rand(),
                      Utils.rand(), Utils.rand(), Utils.rand());
                
                Spectrum a = lrs.l.scale(1.0f / lrs.pdf);
                if (a.isBlack()) continue;
                Ray ray = lrs.ray;
                Intersection is = scene.nearestIntersection(ray);
                
                while (is != null) {
                    Vector wo = ray.d.neg();
                    BSDF bsdf = is.getBSDF();
                    
                    VPL vpl = new VPL();
                    vpl.l = a;
                    vpl.p = is.dg.p;
                    vpl.wi = wo;
                    vpl.n = is.dg.nn;
                    vpl.bsdf = bsdf;
                    
                    svpls.add(vpl);
                    BxDFSample smp = bsdf.sample(wo, Utils.rand(), Utils.rand(), BxDF.ALL);
                    Spectrum anew = a.scale(smp.f).scale(Utils.absdot(smp.wi, bsdf.dgS.nn) / smp.pdf);
                    float r = anew.y() / a.y();
                    System.out.println(r);
                    if (Utils.rand() > r)
                        break;
                    
                    a = anew.scale(1.0f / r);
                    
                    ray = new Ray(is.dg.p, smp.wi);
                    is = scene.nearestIntersection(ray);
                }

            }
            
            vpls[s] = new VPL[svpls.size()];
            svpls.toArray(vpls[s]);
        }
        
        float lightArea = 0.0f;
        for (final Light l : scene.getLights()) lightArea += l.getArea(scene);
        c = 1.0f / (lightArea * 160);
    }
    
    /**
     * A virutal point light source.
     */
    private static class VPL {
        /** Position of the point light */
        public Point p;
        
        /** Normal of surface that was hit */
        public Normal n;
        
        /** incident direction */
        public Vector wi;
        
        /** emitted light */
        public Spectrum l;
        
        /** BSDF at light position */
        public BSDF bsdf;
    }
}
