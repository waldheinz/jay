/*
 * DirectLightingIntegrator.java
 *
 * Created on 29. Dezember 2005, 14:47
 */

package jay.integrators;

import jay.lights.Light;
import jay.materials.BSDF;
import jay.maths.*;
import jay.sampling.Film;
import jay.scene.primitives.Intersection;
import jay.scene.Scene;
import jay.utils.Spectrum;

/**
 *
 * @author Matthias Treydze
 */
public class DirectLightingIntegrator extends ClassicSurfaceIntegrator {
    
    public static enum Strategy {
        SAMPLE_ALL_UNIFORM,
        SAMPLE_ONE_UNIFORM,
        SAMPLE_ONE_WEIGHTED
    }
    
    public final static int MAX_DEPTH = 3;
    
    protected Strategy strategy;
    
    public DirectLightingIntegrator(Film film) {
        super(film);
        this.strategy = Strategy.SAMPLE_ONE_UNIFORM;
    }
    
    public DirectLightingIntegrator(SurfaceIntegrator old) {
        super(old.film);
        this.strategy = Strategy.SAMPLE_ONE_UNIFORM;
    }
    
    public Spectrum traceRay(final Ray ray, Scene s) {
        Intersection is = s.nearestIntersection(ray);
        Spectrum l = Spectrum.BLACK;
        
        if (is != null) {
            
            Vector wo = ray.d.neg();
            l = l.add(is.le(wo));
            BSDF bsdf = is.getBSDF();
            
            
            if (s.getLights().size() > 0) {
                switch (strategy) {
                    case SAMPLE_ALL_UNIFORM:
                        l = l.add(uniformSampleAllLights(s, wo, bsdf.dgS, bsdf));
                        break;
                    case SAMPLE_ONE_UNIFORM:
                        l = l.add(uniformSampleOneLight(s, wo, bsdf.dgS, bsdf));
                        break;
                }
            }
            
        } else {
            /* nichts getroffen */
            for (Light light : s.getLights()) {
                l = l.add(light.direct(ray));
            }
        }
        
        return l;
    }
    
}
