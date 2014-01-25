/*
 * NearestIntersection.java
 *
 * Created on 27. Dezember 2005, 00:00
 */

package jay.scene.primitives;

/**
 * Hilft, aus einer Reihe von Intersections die Näheste zu bestimmen.
 *
 * @author Matthias Treydte
 */
public final class NearestIntersection {
    
    Intersection ni = null;
    
    /**
     * Merkt die übergebene Intersection als die Näheste, wenn sie es ist
     *
     * @param is die zu überprüfende Intersection, kann <code>null</code> sein
     */
    public void set(final Intersection is) {
        if ((is != null) && ((ni == null) || (is.dg.t < ni.dg.t))) {
            ni = is;
        }
    }
    
    /**
     * Gibt die bisher näheste Intersection zurück.
     *
     * @return die näheste Intersection. Kann <code>null</code> liefern.
     */
    public Intersection get() {
        return ni;
    }
}
