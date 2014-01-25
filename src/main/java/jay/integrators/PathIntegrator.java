/*
 * PathIntegrator.java
 *
 * Created on 25. Dezember 2005, 17:30
 */

package jay.integrators;

import jay.lights.Light;
import jay.materials.BSDF;
import jay.materials.BxDFSample;
import jay.materials.bxdfs.BxDF;
import jay.maths.*;
import jay.sampling.Film;
import jay.scene.primitives.Intersection;
import jay.scene.Scene;
import jay.utils.Spectrum;

/**
 *
 * @author Matthias Treydte
 */
public class PathIntegrator extends ClassicSurfaceIntegrator {
    
    public PathIntegrator(Film film) {
        super(film);
    }
    
    public Spectrum traceRay(final Ray r, Scene s) {
        
        Spectrum throughput = Spectrum.WHITE;
        Spectrum L = Spectrum.BLACK;
        boolean specularBounce = false;
        Ray ray = new Ray(r);
        int length = 0;
        
        for (; ;length++) {
            Intersection is = s.nearestIntersection(ray);
            
            if (is == null) {
                /* direkte auswirkung von Lichtquellen auf diesen
                 * Sehstrahl
                 */
                
                for (Light l : s.getLights())
                    L = L.add(l.direct(ray).scale(throughput));
                
                break;
            }
            
            final Vector wo = ray.d.neg();
            
            if (length == 0 || specularBounce) {
                if (is.prim.getLight() != null)
                    L = L.add(is.le(wo).scale(throughput));
            }
            
            if (length == 0) r.tmax = ray.tmax;
            
            BSDF bsdf = is.getBSDF();
            L = L.add(uniformSampleOneLight(s, wo, bsdf.dgS, bsdf).
                  scale(throughput));
            
            BxDFSample bs = bsdf.sample(
                    wo, 
                    Utils.rand(), Utils.rand(),
                    BxDF.ALL);
            
            if (bs.f.isBlack() || (bs.pdf == 0.0f)) break;
            
            specularBounce = (bs.type & BxDF.SPECULAR) != 0;
            
            throughput = throughput.scale(bs.f.scale(
                    Utils.absdot(bs.wi, bsdf.dgS.nn) / bs.pdf));
            
            if (length > 3) {
                final float prob = 0.5f;
                if (Utils.rand() > prob) break;
                throughput = throughput.scale(1.0f / prob);
            }
            
            ray = new Ray(is.dg.p, bs.wi, Ray.EPSILON, Float.POSITIVE_INFINITY);
        }
        
        r.secondaryRays += length;
        return L;
    }
}
