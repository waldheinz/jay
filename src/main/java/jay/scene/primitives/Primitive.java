/*
 * Primitive.java
 *
 * Created on 15. Dezember 2005, 15:46
 */

package jay.scene.primitives;

import java.util.ArrayList;
import java.util.List;
import jay.lights.GeometricLight;
import jay.materials.BSDF;
import jay.materials.Material;
import jay.maths.*;
import jay.scene.Scene;
import jay.utils.Spectrum;

/**
 * @author Matthias Treydte <waldheinz@gmail.com>
 */
public abstract class Primitive implements Intersectable {
    
    /** das Material dieses Primitivs */
    protected List<Material> materials;
    
    protected GeometricLight light;
    
    public Primitive() {
        materials = new ArrayList<Material>(1);
        materials.add(Material.DEFAULT);
        light = null;
    }
    
    public boolean canIntersect() { return true; }
    
    public void refine(List<Primitive> plist) {
        throw new UnsupportedOperationException(
                "unimplemented Primitive.refine() called");
    }
    
    /**
     * Zerlegt das Primitiv so lange, bis es nur noch aus
     * Primitiven besteht welche direkt geschnitten werden
     * können.
     */
    public static void recursiveRefine(List<Primitive> plist, Primitive prim) {
        List<Primitive> todo = new ArrayList<Primitive>();
        todo.add(prim);
        
        while (todo.size() > 0) {
            Primitive p = todo.remove(todo.size() - 1);
            
            if (p.canIntersect()) plist.add(p);
            else p.refine(todo);
        }
    }
    
    /**
     * Sollte true zurückgeben, wenn das Primitiv den
     * Strahl schneidet.
     */
    public boolean intersects(final Ray ray) {
        throw new UnsupportedOperationException(
                "unimplemented Primitive::intersects() called!");
    }
    
    /**
     * Gibt den nähesten Schnitt des Primitivs mit dem Strahl
     * zurück.
     */
    public Intersection nearestIntersection(final Ray ray) {
        throw new UnsupportedOperationException(
                "unimplemented Primitive::nearestIntersection() called!");
    }
    
    /**
     * Weisst dieses Primitiv an zu leuchten.
     *
     * @param i die Intensität
     */
    public void makeLight(Spectrum i, Scene s) {
        throw new UnsupportedOperationException(
                "unimplemented Primitive.makeLight() called!");
    }
        
    /**
     * Hiermit wird dem Primitiv mitgeteilt, daß es instatiiert
     * wurde. So bekommt z.B. eine PrimGroup die Chance den
     * Kd - Baum zu bauen.
     */
    public void makeIntersectable(boolean inst) {
        throw new UnsupportedOperationException(
              "unimplemented Primitive.makeIntersectable() called");
    }
    
    /**
     * Gibt das Material des Primitivs zurück.
     * 
     * @return the material of this primitive.
     */
    public Material getMaterial() {
        return getMaterial(0);
    }
    
    public Material getMaterial(int idx) {
        return materials.get(idx);
    }
    
    /**
     * Sets the "main" material (with index 0) for this
     * primitive.
     */
    public void setMaterial(final Material mat) {
        materials.set(0, mat);
    }
    
    public void setMaterials(final List<Material> mats) {
        this.materials = mats;
    }
    
    public void addMaterial(final Material mat) {
        materials.add(mat);
    }
    
    public abstract BSDF getBSDF(final DifferentialGeometry dg,
          final Transform w2o);
    
    /**
     * Wenn dieses Primitiv eine Lichtquelle ist, so gibt
     * diese Methode das zugehörige Licht zurück. Sonst
     * <code>null</code>.
     * 
     * @return a geometric light source, or {@literal null}.
     */
    public GeometricLight getLight() {
        return light;
    }
    
    public void setLight(GeometricLight light) {
        this.light = light;
    }
    
    /**
     * Wenn diese Methode <code>false</code> zurückgibt darf
     * {@link #makeLight} nicht aufgerufen werden.
     * 
     * @return if this primtive is usable as a light source.
     */
    public boolean usableAsLight() {
        return false;
    }
}
