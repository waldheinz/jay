/*
 * Accelerator.java
 *
 * Created on 25. Dezember 2005, 15:08
 */

package jay.scene.primitives.accelerators;

import java.util.List;
import jay.maths.AABB;
import jay.scene.primitives.*;

/**
 *
 * @author Matthias Treydte
 */
public abstract class Accelerator implements Intersectable {
    
    protected Group group;
    
    public Accelerator(Group group) {
        this.group = group;
    }
    
    /**
     * Wird aufgerufen, wenn sich die Gruppe, welche von diesem
     * Accelerator beschleunigt wird geändert hat. Der Parameter
     * gibt den Pfad zu dem Primitiv an, in welchem die Änderung
     * auftrat. Dieser Parameter kann auch null sein, wenn sich
     * der Pfad nicht feststellen lässt.
     */
    public abstract void rebuild();
    
    /**
     * Es wäre dann doch etwas sinnlos, wenn das nicht ginge.
     */
    public boolean canIntersect() {
        return true;
    }
    
    public void refine(List<Primitive> plist) {
        throw new UnsupportedOperationException("Why would you want to refine" +
              "an accelerator?");
    }
    
    public AABB worldBounds() {
        return group.worldBounds();
    }
}
