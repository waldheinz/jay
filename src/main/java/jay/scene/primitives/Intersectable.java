/*
 * Intersectable.java
 *
 * Created on 25. Dezember 2005, 15:12
 */

package jay.scene.primitives;

import java.util.List;
import jay.maths.AABB;
import jay.maths.Ray;

/**
 * Alles, was mit einem Strahl geschnitten werden kann.
 *
 * @author Matthias Treydte <waldheinz@gmail.com>
 */
public interface Intersectable {
    
    /**
     * Entscheidet, ob dieses Objekt von dem übergebenen
     * Strahl geschnitten wird. Der Strahl wird dabei nicht
     * verändert.
     *
     * @param ray der auf Schnitte zu überprüfende Strahl
     * @return true, wenn das Objekt den Strahl schneidet
     */
    public boolean intersects(final Ray ray);
    
    /**
     * Gibt den nähesten Schnitt des Objektes mit dem Strahl
     * zurück. {@link Ray#tmax} wird dabei auf die Entfernung
     * zu diesem Schnitt verkleinert.
     *
     * @param ray der mit diesem Objekt zu schneidende Strahl
     * @return der näheste Schnitt oder <code>null</code>, wenn
     *      kein Schnitt auftritt
     */
    public Intersection nearestIntersection(final Ray ray);
    
    /**
     * Determines, if this {@literal Intersectable} can be directly
     * intersected, or its {@link #refine(java.util.List)} method sould
     * be used instead.
     *
     * @return if this {@literal Intersectable} can be intersected directly.
     */
    public boolean canIntersect();
    
    /**
     * Wenn ein Primitiv nicht direkt geschnitten werden kann,
     * so sollte es diese Methode unterstützen und die
     * übergebene Liste mit (einfacheren) Primtiven auffüllen,
     * welche dann (hoffentlich) direkt geschnitten werden
     * können.
     *
     * @param plist the primitives
     */
    public void refine(List<Primitive> plist);
    
    /**
     * Gibt die AABB des Primitivs im Welt - Koordinatensystem zur�ck.
     * Sollte das Primitiv keine Ausdehnung haben (z.B. eine leere
     * {@link Group} wird <code>null</code> zur�ckgegeben.
     *
     * @return AABB die Bounding Box dieses Primitivs
     */
    public AABB worldBounds();
    
    /**
     * Liegt irgend ein Teil dieses Primitivs in der AABB?
     * TODO: damit kann man bestimmt bessere beschleunigungs-
     * dinger bauen
     */
//    public boolean intersects(final AABB bbox) {
//        return worldBounds().intersects(bbox);
//    }
}
