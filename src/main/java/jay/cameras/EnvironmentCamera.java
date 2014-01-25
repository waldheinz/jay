/*
 * EnvironmentCamera.java
 *
 * Created on 29. Januar 2006, 14:33
 */

package jay.cameras;

import jay.maths.*;
import jay.sampling.Film;
import jay.sampling.ImageFilm;

/**
 *
 * @author Matthias Treydze
 */
public class EnvironmentCamera extends Camera {
    
    protected Point rayOrigin;
    
    public EnvironmentCamera() {
        this(Transform.IDENTITY, 0.0f, Float.POSITIVE_INFINITY,
                new ImageFilm(640, 480));
    }
    
    /** Erstellt eine neue Instanz von EnvironmentCamera */
    public EnvironmentCamera(
            final Transform world2cam,
            float near, float far,
            final Film film) {
        
        super(world2cam, near, far, film);
        rayOrigin = cam2world.apply(new Point(0, 0, 0));
    }
    
    public Ray fireRay(float x, float y) {
        
        /* Richtung */
        float theta = (float)Math.PI * y / film.yRes;
        float phi = 2 * (float)Math.PI * x / film.xRes;
        
        Vector dir = new Vector(
                (float)(Math.sin(theta) * Math.cos(phi)),
                (float)(Math.cos(theta)),
                (float)(Math.sin(theta) * Math.sin(phi)));
        
        Ray ray = new Ray(rayOrigin, cam2world.apply(dir));
        
        ray.tmin = near;
        ray.tmax = far;
        
        return ray;
    }
    
}
