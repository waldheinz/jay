/*
 * PerspectiveCamera.java
 *
 * Created on 15. Dezember 2005, 17:36
 */

package jay.cameras;

import jay.maths.*;
import jay.sampling.Film;
import jay.sampling.ImageFilm;

/**
 * @author Matthias Treydte
 */
public class PerspectiveCamera extends ProjectiveCamera {
    
    protected float fieldOfView;
    
    public PerspectiveCamera() {
        this(Transform.IDENTITY,
                null,
                0.0f,  
                1.0f, 
                90.0f,  
                1e-10f,   
                1e10f,                        
                new ImageFilm(1024, 768)   
                );
    }
    
    public PerspectiveCamera(
            final Transform world2cam,
            final float screen[],
            float lensr, float focald,
            float fov, 
            float near, float far,
            final Film film) {
        
        super(world2cam,
                Transform.perspective(fov, near, far),
                screen,
                lensr, focald,
                near, far,
                film);
        
        fieldOfView = fov;
    }
    
    public void setFieldOfView(float fov) {
        this.fieldOfView = fov;
        super.setProjectiveTransform(Transform.perspective(fov, near, far));
    }
    
    public Ray fireRay(float x, float y) {
        Point pRas = new Point(x, y, 0.0f);
        Point pCamera = raster2cam.apply(pRas);
        
        Ray ray = new Ray(pCamera, pCamera.vectorTo());
        
        /* Tiefenunschärfe */
        if (lensRadius > 0.0f) {
            /* Punkt auf der Linse sampeln */
            float lensUV[] = Utils.concentricSampleDisk(
                  Utils.rand(), Utils.rand());
            
            lensUV[0] *= lensRadius;
            lensUV[1] *= lensRadius;
            
            /* Punkt in der Fokus - Ebene */
            float ft = (focalLength - near) / ray.d.z;
            Point pfocus = ray.at(ft);
            
            /* Strahl modifizieren */
            ray = ray.setOrigin(new Point(ray.o.x + lensUV[0],
                  ray.o.y + lensUV[1], ray.o.z));
            
            ray = ray.setDirection(pfocus.sub(ray.o));
            
        }
        
        ray = ray.normalized();
        ray.tmin = 0.0f;
        ray.tmax = (far - near) / ray.d.z;
        ray = cam2world.apply(ray);

        return ray;
    }
    
}
