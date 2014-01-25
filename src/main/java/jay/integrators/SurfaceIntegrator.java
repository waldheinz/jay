/*
 * SurfaceIntegrator.java
 *
 * Created on 15. Dezember 2005, 17:02
 */

package jay.integrators;

import jay.cameras.Camera;
import jay.lights.*;
import jay.materials.BSDF;
import jay.materials.BxDFSample;
import jay.materials.bxdfs.BxDF;
import jay.maths.*;
import jay.scene.primitives.DifferentialGeometry;
import jay.scene.primitives.Intersection;
import jay.sampling.*;
import jay.scene.Scene;
import jay.utils.*;

/**
 * @author Matthias Treydte
 */
public abstract class SurfaceIntegrator implements Integrator {
    
    protected Film film;
    
    public SurfaceIntegrator(Film film) {
        this.film = film;
    }
    
    public abstract void exposeFilm(Camera cam, Scene scene, Film film);
    
    public Film getFilm() {
        return film;
    }
    
    public void exposeFilm(Camera cam, Scene scene) {
        exposeFilm(cam, scene, getFilm());
    }
    
    public void setFilm(Film film) {
        this.film = film;
    }
    
    public void prepare(Scene scene, Film film) { }
    
    /**
     * Gibt eine Abschätzung der direkten Beleuchtung an dg.p zurück,
     * welche darauf beruht <em>alle</em> Lichter der Szene zu sampeln.
     */
    public static Spectrum uniformSampleAllLights(
            Scene scene,
            Vector wo,
            DifferentialGeometry dg,
            BSDF bsdf) {
        
        Spectrum c = Spectrum.BLACK;
        
        for (Light l : scene.getLights()) {
            c = c.add(estimateDirectMIS(scene, wo, dg.p, dg.nn, bsdf, l));
        }
        
        return c;
    }
    
    public static Spectrum uniformSampleOneLight(
            Scene scene,
            Vector wo,
            DifferentialGeometry dg,
            BSDF bsdf) {
        
        int lCount = scene.getLights().size();
        int lNum = Math.min(lCount - 1, (int)(jay.maths.Utils.rand() * lCount));
        if (lNum == -1) return Spectrum.BLACK;
        Light l = scene.getLight(lNum);
        
        return estimateDirectMIS(scene, wo, dg.p, dg.nn, bsdf, l).scale(lCount);
    }
    
    public static Spectrum uniformSampleOneLight(
            Scene scene,
            Vector wo,
            Point p,
            Normal n,
            BSDF bsdf) {
        
        int lCount = scene.getLights().size();
        int lNum = Math.min(lCount - 1, (int)(jay.maths.Utils.rand() * lCount));
        Light l = scene.getLight(lNum);
        
        return estimateDirectMIS(scene, wo, p, n, bsdf, l).scale(lCount);
    }
    
    /**
     * Eine Lichtquelle testen.
     */
    public static Spectrum estimateDirectLight(
            Scene scene,
            Vector wo,
            Point p,
            Normal n,
            BSDF bsdf,
            Light light) {
        
        
        LightSample ls = light.sample(p, n, jay.maths.Utils.rand(), jay.maths.Utils.rand());
        Vector wi = ls.wo;
        if (ls.pdf > 0.0f && !ls.r.isBlack()) {
            Spectrum f = bsdf.eval(wo, wi);
            
            if (!f.isBlack() && ls.vt.isVisible(scene)) {
                return ls.r.scale(f).scale(jay.maths.Utils.absdot(wi, n) / ls.pdf);
            }
        }
        
        return Spectrum.BLACK;
    }
    
    /**
     * Ein Licht mit multiple importance sampling testen.
     */
    public static Spectrum estimateDirectMIS(
            Scene scene,
            Vector wo,
            Point p,
            Normal n,
            BSDF bsdf,
            Light light) {
        
        Spectrum ld = Spectrum.BLACK;
        
        /* Lichtquelle sampeln */
        
        LightSample ls = light.sample(p, n, jay.maths.Utils.rand(), jay.maths.Utils.rand());
        Vector wi = ls.wo;
        
        if (ls.pdf > 0.0f && !ls.r.isBlack()) {
            Spectrum f = bsdf.eval(wo, wi);
            
            if (!f.isBlack() && ls.vt.isVisible(scene)) {
                if (light.isDeltaLight()) {
                    ld = ld.add(f.scale(ls.r.scale(
                            jay.maths.Utils.absdot(wi, n) / ls.pdf)));
                } else {
                    float bsdfpdf = bsdf.pdf(wo, wi);
                    float weight = powerHeuristic(1, ls.pdf, 1, bsdfpdf);
                    ld = ld.add(f.scale(ls.r.scale(
                            jay.maths.Utils.absdot(wi, n) * weight / ls.pdf)));
                }
            }
        }
        
        /* BSDF sampeln */
        
        if (!light.isDeltaLight()) {
            final int flags = BxDF.DIFFUSE | BxDF.GLOSSY;
            
            BxDFSample bs = bsdf.sample(wo, jay.maths.Utils.rand(), jay.maths.Utils.rand(), flags);
            wi = bs.wi;
            
            if (!bs.f.isBlack() && (bs.pdf > 0.0f)) {
                float lightpdf = light.pdf(p, n, wi);
                
                if (lightpdf > 0.0f) {
                    float weight = powerHeuristic(1, bs.pdf, 1, lightpdf);
                    Ray r = new Ray(p, wi);
                    Intersection is = scene.nearestIntersection(r);
                    Spectrum li = Spectrum.BLACK;
                    if (is != null && is.prim.getLight() == light) {
                        li = is.le(wi.neg());
                    } else {
                        li = light.direct(r);
                    }
                    
                    if (!li.isBlack()) {
                        /* TODO: transmittance */
                        ld = ld.add(bs.f.scale(li).scale(jay.maths.Utils.absdot(
                                wi, n) * weight / bs.pdf));
                    }
                    
                }
            }
        }
        
        return ld;
    }
    
    /**
     * <a href="http://graphics.stanford.edu/papers/combine/">
     * siehe</a>
     * 525
     */
    public static float powerHeuristic(
            int nf, float fpdf,
            int ng, float gpdf) {
        
        float f = nf * fpdf;
        float g = ng * gpdf;
        return (f*f) / (f*f + g*g);
    }
    
}
