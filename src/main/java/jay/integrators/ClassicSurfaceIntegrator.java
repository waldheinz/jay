/*
 * ClassicSurfaceIntegrator.java
 *
 * Created on 5. März 2006, 21:06
 */

package jay.integrators;

import jay.cameras.Camera;
import jay.maths.Ray;
import jay.sampling.*;
import jay.sampling.buckets.ImagePlaneSampler;
import jay.scene.Scene;
import jay.utils.Spectrum;

/**
 *
 * @author Matthias Treydte
 */
public abstract class ClassicSurfaceIntegrator extends SurfaceIntegrator {
    
    protected ImagePlaneSampler ips;
    
    public ClassicSurfaceIntegrator(final Film film) {
        super(film);
        setFilm(film);
    }
    
    @Override
    public void setFilm(Film film) {
        super.setFilm(film);
        ips = new ImagePlaneSampler(film);
    }
    
    public void exposeFilm(Camera cam, Scene scene, Film film) {
        Stratified2D offset = new Stratified2D(2, 2);
  
        float[] off = new float[2];
        int[] pixelXY = new int[2];
        
        while (true) {
            ImagePlaneSampler.Bucket b = ips.nextBucket();
            
            while (b.nextPixel(pixelXY)) {
                offset.reset();
            
                while (offset.nextSample(off)) {
                    float fx = (float)pixelXY[0] + off[0] - 0.5f;
                    float fy = (float)pixelXY[1] + off[1] - 0.5f;
                
                    Ray ray = cam.fireRay(fx, fy);
                    Spectrum li = traceRay(ray, scene);
                    film.addSample(fx, fy, ray, li);
                }
            }
        }
    }
    
}
