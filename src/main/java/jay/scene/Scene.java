/*
 * Scene.java
 *
 * Created on 15. Dezember 2005, 17:00
 */

package jay.scene;

import java.util.*;
import java.util.logging.Logger;
import jay.cameras.*;
import jay.integrators.*;
import jay.lights.Light;
import jay.lights.skylight.SkyLight;
import jay.maths.*;
import jay.sampling.Film;
import jay.sampling.ImageFilm;
import jay.scene.primitives.Group;
import jay.scene.primitives.accelerators.*;

/**
 * @author Matthias Treydte <waldheinz at gmail.com>
 */
public class Scene extends Group {
    
    protected SurfaceIntegrator si;
    protected List<Light> lights = new ArrayList<Light>();
    protected Camera camera;
    protected Film film = new ImageFilm(1024, 768);
    
    private static Logger log = Logger.getLogger(Scene.class.getName());

    public void setSurfaceIntegrator(SurfaceIntegrator si) {
        this.si = si;
        si.setFilm(film);
    }
    
    public void setCamera(final Camera camera) {
        this.camera = camera;
        if (film != null)
            camera.setFilm(film);
        else
            film = camera.getFilm();
    }
    
    public void setFilm(Film film) {
        this.film = film;
        
        if (camera != null)
            camera.setFilm(film);
        
        if (si != null)
            si.setFilm(film);
    }
    
    public Film getFilm() {
        return film;
    }
    
    public List<Light> getLights() {
        return lights;
    }
    
    public Light getLight(int idx) {
        return lights.get(idx);
    }
    
    public void addLight(Light l) {
        lights.add(l);
    }

    public int getLightCount() {
        return lights.size();
    }
    
    public Camera getCamera() {
        return camera;
    }
    
    /**
     * Prepares the scene so it is ready for rendering. Must be called
     * before {@link #render} may be called.
     */
    public void prepare() {
        log.info("Preparing scene.");
        
        if (accel == null) {
            log.info("using default KdTree accelerator");
            accel = new KdTree(this);
        }
        
        if (si == null) {
            log.info("using pathtracing render mode");
            si = new PathIntegrator(camera.getFilm());
        }
        
        if (camera == null) {
            log.warning("no camera specified, using default camera");
            camera = new PerspectiveCamera();
        }
        
        if (getLights().size() == 0) {
            log.warning("scene contains no light sources, using a skylight");
            this.addLight(new SkyLight());
        }
        
        accel.rebuild();
        
        si.prepare(this, camera.getFilm());
    }

    public SurfaceIntegrator getSurfaceIntegrator() {
        return si;
    }
    
    @Override
    public AABB worldBounds() {
        return accel.worldBounds();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Scene [prims=");
        sb.append(getChildCount());
        sb.append(",\ncamera=");
        sb.append(camera);
        sb.append(",\ns_integrator=");
        sb.append(si);
        sb.append(",\naccel=");
        sb.append(accel);
        sb.append("]");
        return sb.toString();
    }
    
}
