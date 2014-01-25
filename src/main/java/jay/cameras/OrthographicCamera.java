/*
 * OrthographicCamera.java
 *
 * Created on 29. Januar 2006, 15:22
 */

package jay.cameras;

import jay.maths.*;
import jay.sampling.Film;
import jay.sampling.ImageFilm;

/**
 *
 * @author Matthias Treydze
 */
public class OrthographicCamera extends ProjectiveCamera {
    
    /** Erstellt eine neue Instanz von OrthographicCamera */
    public OrthographicCamera() {
        this(Transform.IDENTITY,
                null,
                0.1f,
                1000f,
                0.0f,
                1e30f, 
                new ImageFilm(640, 480));
    }
    
    public OrthographicCamera(
            final Transform world2cam,
            final float screen[],
            float near, float far,
            float lensr, float focald,
            final Film film) {
        
        super(world2cam,
                Transform.orthographic(near, far),
                screen,
                lensr, focald,
                near, far, film);
    }
    
    public Ray fireRay(float x, float y) {
        // Generate raster and camera samples
        Point pRas = new Point(x, y, 0);
        Point pCamera = raster2cam.apply(pRas);
        
        Ray ray = new Ray(pCamera, new Vector(0, 0, 1));
        
        // Modify ray for depth of field
        if (lensRadius > 0.0f) {
            // Sample point on lens
            
            float lensUV[] = Utils.concentricSampleDisk(
                    Utils.rand(), Utils.rand());

            lensUV[0] *= lensRadius;
            lensUV[1] *= lensRadius;
            
            // Compute point on plane of focus
            float ft = (focalLength - near) / ray.d.z;
            Point pFocus = ray.at(ft);
            // Update ray for effect of lens

            ray = ray.setOrigin(new Point(
                    ray.o.x + lensUV[0], ray.o.y + lensUV[1],
                    ray.o.z));

            ray = ray.setDirection(pFocus.sub(ray.o));
        }
        
        ray.tmin = 0.0f;
        ray.tmax = far - near;
        ray = ray.normalized();
        ray = cam2world.apply(ray);
        
        return ray;
    }
    
}
