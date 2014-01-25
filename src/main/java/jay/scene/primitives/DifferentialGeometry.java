/*
 * DifferentialGeometry.java
 *
 * Created on 15. Dezember 2005, 16:25
 */

package jay.scene.primitives;

import jay.maths.*;
import jay.scene.primitives.geometry.Geometry;

/**
 * @author Matthias Treydte
 */
public final class DifferentialGeometry {
    
    public final Point p;
    
    /** Parametric distance along the ray to the intersection point */
    public final float t;
    public final Normal nn;
    public final Vector dpdu, dpdv;
    public final float u, v;
    public final Geometry g;
    
    public Vector dpdx, dpdy;
    public float dudx, dudy;
    public float dvdx, dvdy;
    public Vector dndu, dndv;
    
    public DifferentialGeometry(final Point p, final float t,
          final Vector dpdu, final Vector dpdv,
          float u, float v, final Geometry g) {
        
        this.p = p;
        this.t = t;
        this.dpdu = dpdu;
        this.dpdv = dpdv;
        this.u = u;
        this.v = v;
        this.g = g;
        
        final Normal n = dpdu.cross(dpdv).normalized();
        if (g.invertNormals ^ g.transformSwapsHandedness)
            nn = n.neg();
        else
            nn = n;
    }
    
}
