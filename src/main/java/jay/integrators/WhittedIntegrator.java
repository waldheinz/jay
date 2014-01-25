/*
 * WhittedIntegrator.java
 *
 * Created on 16. Dezember 2005, 01:13
 */

package jay.integrators;

import jay.lights.*;
import jay.materials.*;
import jay.materials.bxdfs.BxDF;
import jay.maths.*;
import jay.sampling.Film;
import jay.scene.primitives.Intersection;
import jay.scene.Scene;
import jay.utils.*;
import static jay.maths.Utils.*;

/**
 * @author Matthias Treydte
 */
public class WhittedIntegrator extends ClassicSurfaceIntegrator {
    
    public WhittedIntegrator(Film film) {
        super(film);
    }
    
    public Spectrum traceRay(final Ray ray, Scene s) {
        Intersection is = s.nearestIntersection(ray);
        
        if (is == null) {
            return Spectrum.BLACK;
        }
        
        Spectrum color = Spectrum.BLACK;
        
//        if (is.prim.getLight() != null)
//            color = color.add(is.prim.getLight().direct(ray));
        
        color = color.add(is.le(ray.d.neg()));
        
        final Vector wi = ray.d.neg();
        
        BSDF bsdf = is.getBSDF();
        
        for (Light light : s.getLights()) {
            
            int sampleCount;
            if (light.isDeltaLight()) sampleCount = 1;
            else sampleCount = 4;
            
            float weight = 1.0f / sampleCount;
            
            while (sampleCount-- > 0) {
                
                LightSample ls = light.sample(is.dg.p, bsdf.dgS.nn,
                        Utils.rand(), Utils.rand());
  
                Spectrum lcol = ls.r.scale(bsdf.
                        eval(wi, ls.wo, BxDF.DIFFUSE | BxDF.GLOSSY).
                        scale(weight));
                
                if (!lcol.isBlack() && ls.vt.isVisible(s))
                    color = color.add(lcol);
                
            }
        }
        
        return color;
    }
    
}
