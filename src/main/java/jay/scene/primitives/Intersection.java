/*
 * Intersection.java
 *
 * Created on 15. Dezember 2005, 16:08
 */

package jay.scene.primitives;

import jay.lights.GeometricLight;
import jay.materials.BSDF;
import jay.maths.*;
import jay.utils.Spectrum;

/**
 * @author Matthias Treydte
 */
public class Intersection {
    
    public Primitive prim;
    public DifferentialGeometry dg;
    public Transform w2o;
    
    Intersection(Primitive p) { prim = p; }
    
    Intersection() { }
    
    boolean isMiss() { return (prim == null); }
    
    public void setPrimitive(Primitive p) { prim = p; }
    
    public BSDF getBSDF() {
        return prim.getBSDF(dg, w2o);
    }

    /**
     * Licht, das am Schnittpunkt erzeugt und in Richtung w
     * abgestrahlt wird.
     */
    public Spectrum le(final Vector w) {
        GeometricLight l = prim.getLight();
        if (l == null) return Spectrum.BLACK;
        return l.l(dg.p, dg.nn, w);
    }

}
