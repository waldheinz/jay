/*
 * ProjectiveCamera.java
 *
 * Created on 11. Januar 2006, 20:42
 */

package jay.cameras;

import jay.maths.*;
import jay.sampling.Film;
import static jay.maths.Transform.*;

/**
 * @author Matthias Treydte
 */
public abstract class ProjectiveCamera extends Camera {
    
    protected Transform cam2screen;
    protected Transform world2screen;
    protected Transform raster2cam;
    protected Transform screen2raster;
    protected Transform raster2screen;
    
    protected float screen[];
    protected float focalLength;
    protected float lensRadius;
    
    public ProjectiveCamera(
            final Transform world2cam,
            final Transform proj,
            float screen[],
            float lensr, float focald,
            float near, float far,
            final Film film) {
        
        super(world2cam, near, far, film);
        
        this.lensRadius = lensr;
        this.focalLength = focald;
        this.cam2screen = proj;
        this.screen = screen;
        
        calcTransforms();
    }
    
    protected void setProjectiveTransform(final Transform proj) {
        cam2screen = proj;
        calcTransforms();
    }
    
    @Override
    public void setTransform(final Transform world2cam) {
        super.setTransform(world2cam);
        screen=null;
        calcTransforms();
        System.out.println("cam looks to " + cam2world.apply(new Vector(0, 0, 1)));
    }
    
    @Override
    public void setFilm(final Film film) {
        super.setFilm(film);
        calcTransforms();
    }
    
    private void calcTransforms() {
        
        if (screen == null) {
            final float frame = (float)film.xRes / film.yRes;
            screen = new float[4];
            
            if (frame > 1.f) {
                 screen[0] = -frame;
                 screen[1] =  frame;
                 screen[2] = -1f;
                 screen[3] =  1f;
            } else {
                 screen[0] = -1f;
                 screen[1] =  1f;
                 screen[2] = -1f / frame;
                 screen[3] =  1f / frame;
            }
        }
        
        world2screen = cam2screen.compose(world2cam);

        /* Compute projective camera screen transformations */
        screen2raster = 
                scale(film.xRes, film.yRes, 1.0f).compose(
                scale(1.0f / (screen[1] - screen[0]),
                      1.0f / (screen[2] - screen[3]), 1.0f)).compose(
                translate(new Vector(-screen[0], -screen[3], 0)));
        raster2screen = screen2raster.getInverse();
        raster2cam = cam2screen.getInverse().compose(raster2screen);
    }
    
}
