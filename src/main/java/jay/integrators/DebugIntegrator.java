/*
 * DebugIntegrator.java
 *
 * Created on 15. Dezember 2005, 17:17
 */

package jay.integrators;

import jay.materials.BSDF;
import jay.maths.Ray;
import jay.sampling.Film;
import jay.scene.primitives.DifferentialGeometry;
import jay.scene.primitives.Intersection;
import jay.scene.Scene;
import jay.utils.Spectrum;

/**
 * @author Matthias Treydte
 */
public class DebugIntegrator extends ClassicSurfaceIntegrator {
    
    public enum Mode { NORMALS, ZBUFFER, POS, CELLS, DPDU, SEC_RAYS };
    
    private Mode mode = Mode.SEC_RAYS;
    
    private ClassicSurfaceIntegrator childIntegrator;
    
    public DebugIntegrator(Film film) {
        super(film);
        childIntegrator = new PathIntegrator(film);
    }
    
    public DebugIntegrator(SurfaceIntegrator old) {
        this(old.film);
    }
    
    public void setMode(Mode m) {
        this.mode = m;
    }
    
    public Spectrum traceRay(final Ray ray, Scene s) {
        Intersection is = s.nearestIntersection(ray);
        
        if (is != null) {
            //System.out.println(".");
            BSDF bsdf = is.getBSDF();
            DifferentialGeometry dg = is.dg;
            
            switch (mode) {
                case SEC_RAYS:
                    childIntegrator.traceRay(ray, s);
                    return new Spectrum(ray.secondaryRays);
                    
                case DPDU:
                    return Spectrum.fromRGB(
                          Math.max(0,dg.dpdu.x),
                          Math.max(0,dg.dpdu.y),
                          Math.max(0,dg.dpdu.z)
                          );
                    
                case CELLS:
                    return Spectrum.fromRGB(
                          ray.cellsVisited,
                          ray.intersections, 0);
                    
                case NORMALS:
                    return Spectrum.fromRGB(
                            Math.max(0, bsdf.dgS.nn.x),
                            Math.max(0, bsdf.dgS.nn.y),
                            Math.max(0, bsdf.dgS.nn.z));
                    
                case ZBUFFER:
                    if (bsdf.dgS.nn.dot(ray.d) > 0)
                        return Spectrum.fromRGB(is.dg.t,0,0);
                    else
                        return Spectrum.fromRGB(0, is.dg.t, 0);
                    
                case POS:
                    return Spectrum.fromRGB(
                          Math.abs(is.dg.p.x),
                          Math.abs(is.dg.p.y),
                          Math.abs(is.dg.p.z));
            }
        }
//        return new Spectrum(ray.d.x, ray.d.y, ray.d.z);
        return Spectrum.BLACK;
//        return Spectrum.fromRGB(Math.abs(ray.d.x), Math.abs(ray.d.y),
//                Math.abs(ray.d.z));
    }
    
}
