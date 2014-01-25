/*
 * NonAccelerator.java
 *
 * Created on 25. Dezember 2005, 15:16
 */

package jay.scene.primitives.accelerators;

import java.util.ArrayList;
import java.util.List;
import jay.maths.Ray;
import jay.scene.primitives.Group;
import jay.scene.primitives.Intersection;
import jay.scene.primitives.NearestIntersection;
import jay.scene.primitives.Primitive;

/**
 * Ein Beschleuniger, der gar nicht beschleunigt. Die erwartete
 * Laufzeit für eine Gruppe mit n Primitiven beträgt O(n).
 *
 * @author Matthias Treydte
 */
public class NonAccelerator extends Accelerator {
    
    private List<Primitive> prims = new ArrayList<Primitive>();
    
    public NonAccelerator(Group group) {
        super(group);
        rebuild();
    }
    
    public boolean intersects(final Ray ray) {
        for (Primitive p : prims) {
            if (p.intersects(ray)) return true;
        }
        
        return false;
    }
    
    public Intersection nearestIntersection(final Ray ray) {
        NearestIntersection ni = new NearestIntersection();
        
        for (Primitive p : prims) {
            ni.set(p.nearestIntersection(ray));
        }
        
        return ni.get();
    }
    
    public void rebuild() {
        prims.clear();
        for (Primitive p : group.getPrimitives())
            Primitive.recursiveRefine(prims, p);
    }
    
}
