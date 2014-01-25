/*
 * GeometrySet.java
 *
 * Created on 23. Februar 2006, 20:06
 */

package jay.scene.primitives.geometry;

import java.util.List;
import jay.maths.AABB;
import jay.maths.Ray;
import jay.scene.primitives.DifferentialGeometry;

/**
 *
 * @author Matthias Treydte <waldheinz@gmail.com>
 */
public class GeometrySet extends Geometry {
    
    float area;
    final List<Geometry> geometries;
    final float[] areaCDF;
    
    /** Creates a new instance of GeometrySet */
    public GeometrySet(final List<Geometry> geometries) {
        this.geometries = geometries;
        final float[] areas = new float[geometries.size()];
        area = 0.0f;
        
        for (int i=0; i < geometries.size(); ++i) {
            float a = geometries.get(i).getArea();
            area += a;
            areas[i] = a;
        }
        
        float prevCDF = 0.0f;
        areaCDF = new float[geometries.size()];
        
        for (int i = 0; i < geometries.size(); ++i) {
            areaCDF[i] = (prevCDF + areas[i] / area);
            prevCDF = areaCDF[i];
        }
    }
    
    @Override
    public AABB localBounds() {
        AABB b = AABB.EMPTY;
        
        for (Geometry g : geometries)
            b = b.extend(g.localBounds());
        
        return b;
    }
    
    @Override
    public GeometrySample sample(float u1, float u2) {
        float ls = jay.maths.Utils.rand();
        int sn;
        
        for (sn = 0; sn < geometries.size()-1; ++sn)
            if (ls < areaCDF[sn]) break;
        
        return geometries.get(sn).sample(u1, u2);
    }
    
    @Override
    public float getArea() {
        return area;
    }
    
    @Override
    public DifferentialGeometry nearestIntersection(final Ray ray) {
        DifferentialGeometry dg = null;
        
        for (Geometry g : geometries) {
            DifferentialGeometry n = g.nearestIntersection(ray);
            if (n != null) dg = n;
        }
        
        return dg;
    }
    
}
