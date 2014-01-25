/*
 * VisibilityTester.java
 *
 * Created on 16. Dezember 2005, 01:30
 */

package jay.lights;

import jay.maths.*;
import static jay.maths.Utils.*;
import jay.scene.Scene;

/**
 * @author Matthias Treydte
 */
public final class VisibilityTester {
    /** der Sehstrahl, welcher die Sichtbarkeit entscheidet */
    protected Ray ray;
    
    public VisibilityTester() { }
    
    public VisibilityTester(final Point p1, final Point p2) {
        init(p1, p2);
    }
    
    /**
     * Testet zwei Punkte auf gegenseitige Sichtbarkeit
     */
    public void init(final Point p1, final Point p2) {
        final Vector dir = p2.sub(p1);
        final float length = dir.length();
        final float invlen = 1.0f / length;
        final Vector ndir = new Vector(
              dir.x * invlen, dir.y * invlen, dir.z * invlen);
        ray = new Ray(p1, ndir);
        ray.tmax = length - 2.0f * EPSILON;
        ray.tmin = EPSILON;
    }
    
    public void init(final Ray ray) {
        this.ray = new Ray(ray.at(EPSILON),
                ray.d, ray.tmax - EPSILON,
                ray.tmin);
    }
    
    public boolean isVisible(final Scene scene) {
        return !scene.intersects(ray);
    }
    
}
