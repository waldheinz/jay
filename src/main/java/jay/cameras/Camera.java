/*
 * Camera.java
 *
 * Created on 15. Dezember 2005, 17:01
 */

package jay.cameras;

import jay.maths.*;
import jay.sampling.Film;
import jay.sampling.ImageFilm;

/**
 * @author Matthias Treydte
 */
public abstract class Camera implements Cloneable {
    
    /**
     * The transformation which converts from world space
     * to camera space. This is the inverse of {@link #cam2world}.
     */
    protected Transform world2cam;
    
    /** The transformation which places this camera in the world. */
    protected Transform cam2world;
    
    /** Maximum distance. */
    protected float far;
    
    /** Minimum distance. */
    protected float near;
    
    /** The film this camera exposes */
    protected Film film;
    
    public Camera() {
        this.near = 0.0f;
        this.far = Float.POSITIVE_INFINITY;
        this.film = new ImageFilm(640, 480);
        setTransform(Transform.IDENTITY);
    }
    
    public Camera(final Transform world2cam, float near, float far, final Film film) {
        this.near = near;
        this.far = far;
        this.film = film;
        this.world2cam = world2cam;
        this.cam2world = world2cam.getInverse();
    }
    
    public void setTransform(final Transform world2cam) {
        this.world2cam = world2cam;
        this.cam2world = world2cam.getInverse();
    }

    public void setFilm(final Film film) {
        this.film = film;
    }
    
    /**
     * Returns the {@link Film} this camera works on.
     */
    public Film getFilm() {
        return film;
    }
    
    /**
     * Gibt den Strahl zur�ck, welcher f�r den entsprechenden
     * Bildpunkt "zust�ndig" ist. Die Koorinaten werden im
     * Raster - Koorinatensystem des Bildes angegeben.
     * <p>
     * Die Richtungs - Komponente des zur�ckgegebenen Strahls
     * ist normalisiert.
     *
     * @param x x - Koordinate des Bildpunktes
     * @param y y - Koordinate des Bildpunktes
     * @return der erzeugte Sehstrahl
     */
    public abstract Ray fireRay(float x, float y);
    
}
