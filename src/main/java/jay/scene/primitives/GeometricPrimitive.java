/*
 * GeometricPrimitive.java
 *
 * Created on 15. Dezember 2005, 19:29
 */

package jay.scene.primitives;

import java.util.List;
import jay.lights.GeometricLight;
import jay.materials.BSDF;
import jay.maths.AABB;
import jay.maths.Ray;
import jay.maths.Transform;
import jay.scene.Scene;
import jay.scene.primitives.geometry.Geometry;
import jay.utils.GeometryList;
import jay.utils.Spectrum;

/**
 * @author Matthias Treydte
 */
public class GeometricPrimitive extends Primitive {
    
    private final Geometry g;
    
    public GeometricPrimitive(final Geometry g) {
        this.g = g;
    }
    
    public AABB worldBounds() {
        return g.worldBounds();
    }
    
    @Override
    public boolean intersects(final Ray ray) {
        return g.intersects(ray);
    }
    
    @Override
    public Intersection nearestIntersection(final Ray ray) {
        final DifferentialGeometry dg = g.nearestIntersection(ray);
        if (dg == null) return null;
        
        final Intersection is = new Intersection(this);
        is.dg = dg;
        is.w2o = g.w2g;
        ray.tmax = dg.t;
        
        return is;
    }
    
    @Override
    public boolean usableAsLight() {
        return g.canEmit();
    }
    
    public void makeLight(final Spectrum i, final Scene s) {
        if ((i != null) && (s != null)) {
            GeometricLight l = new GeometricLight(g, i);
            s.addLight(l);
            this.light = l;
        }
    }

    @Override
    public boolean canIntersect() {
        return g.canIntersect();
    }

    @Override
    public void refine(final List<Primitive> plist) {
        GeometryList glist = new GeometryList();
        g.refine(glist);

        for (Geometry g : glist) {
            GeometricPrimitive gp = new GeometricPrimitive(g);
            gp.setLight(this.getLight());
            gp.setMaterials(this.materials);
            plist.add(gp);
        }
            
    }

    @Override
    public String toString() {
        return "GeometricPrimitive [" + g + "]";
    }
    
    public BSDF getBSDF(final DifferentialGeometry dg, final Transform w2o, int i) {
        final DifferentialGeometry dgS =
              g.getShadingGeometry(dg, w2o.getInverse());
        return materials.get(i).getBSDF(dg, dgS);
    }
    
    @Override
    public BSDF getBSDF(final DifferentialGeometry dg, final Transform w2o) {
        return getBSDF(dg, w2o, g.getMaterialIndex());
    }
}
