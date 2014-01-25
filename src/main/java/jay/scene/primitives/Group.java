/*
 * Group.java
 *
 * Created on 16. Dezember 2005, 00:18
 */

package jay.scene.primitives;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jay.materials.BSDF;
import jay.maths.*;
import jay.scene.primitives.accelerators.Accelerator;

/**
 * Eine Gruppe von Primitiven. Diese können gemeinsam transformiert
 * werden. Ausserdem kann man eine Gruppe anweisen, für die in ihr
 * enthaltenen Primitive eine Datenstruktur zur Beschleunigung der
 * Schnittberechnung zu erstellen, was z.B. nützlich ist wenn man
 * eine Instanz einer Gruppe in der Szene platzieren möchte.
 *
 * @author Matthias Treydte <waldheinz@gmail.com>
 */
public class Group extends Primitive {
    
    private final ArrayList<Primitive> children;

    /**
     * The acceleration structure used by this group. Can be
     * {@literal null} if not initialized yet.
     */
    protected Accelerator accel = null;
    
    /** Anzahl der Instanzen dieser Gruppe */
    protected int instances = 0;

    /**
     * Creates a new group without any children.
     */
    public Group() {
        children = new ArrayList<Primitive>();
    }
    
    public AABB worldBounds() {
        final List<Primitive> prims = getPrimitives();
        if (prims.size() == 0) return AABB.EMPTY;
        
        AABB aabb = AABB.EMPTY;
        
        for (Primitive p : prims) 
            aabb.extend(p.worldBounds());
        
        return aabb;
    }
    
    public void setInstanced(boolean onoff) {
        if (onoff) instances++;
        else instances--;
    }
    
    @Override
    public boolean canIntersect() {
        return (instances > 0);
    }
    
    @Override
    public Intersection nearestIntersection(final Ray ray) {
        return accel.nearestIntersection(ray);
    }
    
    @Override
    public boolean intersects(final Ray ray) {
        return accel.intersects(ray);
    }
    
    @Override
    public void refine(List<Primitive> plist) {
        plist.addAll(children);
    }

    /**
     * Returns the primitives that are cildren of this group. The returned
     * list can not be modified. Use the
     * {@link #addChild(jay.scene.primitives.Primitive)} method instead.
     *
     * @return the children of this group.
     */
    public List<Primitive> getPrimitives() {
        return Collections.unmodifiableList(children);
    }
    
    public void addChild(Primitive child) {
        children.add(child);
    }
    
    public int getChildCount() {
        return children.size();
    }

    public boolean hasAccelerator() {
        return (accel != null);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("Group [children=[\n");
        
        /* this is a bit complicated to handle nested groups properly */
        for (int i=0; i < getChildCount(); i++) {
            String cdesc[] = children.get(i).toString().split("\n");
            for (int j=0; j < cdesc.length; j++) {
                if (j > 0) sb.append("\n");
                sb.append("   ");
                sb.append(cdesc[j]);
            }
            
            if (i < getChildCount() - 1)
                sb.append(",\n");
        }
        
        sb.append("\n]]");
        
        return sb.toString();
    }

    public void addChildren(List<? extends Primitive> prims) {
        for (Primitive p : prims)
            addChild(p);
    }

    public BSDF getBSDF(final DifferentialGeometry dg, final Transform w2o) {
        throw new UnsupportedOperationException(
              "should have gone to a geometric primitive");
    }
}
