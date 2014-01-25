/*
 * Geometry.java
 *
 * Created on 15. Dezember 2005, 16:15
 */

package jay.scene.primitives.geometry;

import jay.maths.*;
import jay.scene.primitives.DifferentialGeometry;
import jay.utils.GeometryList;


/**
 * @author Matthias Treydte
 */
public abstract class Geometry {
    
    public Transform g2w;
    public Transform w2g;
    public boolean invertNormals;
    public boolean transformSwapsHandedness;

    public Geometry() {
        this(Transform.IDENTITY, false);
    }

    public Geometry(Transform toWorld, boolean invertNormals) {
        this.g2w = toWorld;
        this.w2g = toWorld.getInverse();
        this.transformSwapsHandedness = toWorld.swapsHandedness();
        this.invertNormals = invertNormals;
    }

    /**
     * Gibt die AABB der Geometrie im lokalen Koordinatensystem
     * zurück.
     *
     * @return die Geometrie im lokalen Koordinatensystem
     */
    public abstract AABB localBounds();
    
    /**
     * Gibt die AABB der Geometrie in Weltkoordinaten zurück.
     * Die standard - Implementation transformiert dazu die
     * lokale AABB in Weltkoordinaten und gibt diese zurück. Kann
     * möglicherweise von konkreten geometrischen Objekten
     * genauer gemacht werden.
     *
     * @return die AABB in Weltkoordinaten
     */
    public AABB worldBounds() { return g2w.apply(localBounds()); }
    
    /**
     * Gibt an, ob diese Geometrie direkt geschnitten werden kann
     * oder erst mittels {@link #refine} in einfachere geometrische
     * Objekte zerlegt werden muss.
     *
     * @return true, wenn diese Geometrie direkt geschnitten werden kann
     */
    public boolean canIntersect() { return true; }
    
    /**
     * This function can be used to query for an material index into the
     * material array in the corresponding {@link GeometricPrimitive}.
     * Main use is to be able to assign a different material to the faces
     * of a triangle mesh.
     * 
     * @return the material index to use for this geometry.
     */
    public int getMaterialIndex() {
        return 0;
    }
    
    /**
     * Setzt die Transformation, mit der diese Geometrie in Weltkoordinaten
     * transformiert werden soll.
     *
     * @param toWorld die zu benutzende Transformation
     */
    public void setTransform(final Transform toWorld) {
        g2w = toWorld;
        w2g = toWorld.getInverse();
        transformSwapsHandedness = toWorld.swapsHandedness();
    }
    
    public void refine(final GeometryList glist) {
        throw new UnsupportedOperationException(
                "unimplemented Geometry::refine() called!");
    }
    
    public DifferentialGeometry getShadingGeometry(final DifferentialGeometry dg,
          final Transform o2w) {
        
        return dg;
    }
    
    public void setInvertedNormals(boolean invert) {
        this.invertNormals = invert;
    }
    
    /**
     * Gibt an, ob die diese Geometrie ihre "natürlichen" Normalen
     * oder invertierte zurückgibt.
     */
    public boolean getInvertedNormals() {
        return this.invertNormals;
    }
    
    public boolean intersects(final AABB box) {
        return worldBounds().intersects(box);
    }
    
    /**
     * Tells if the given ray intesects this geometric object.
     *
     * @param The ray to intersect the object with.
     * @return <code>true</code> if the ray is intersected.
     */
    public boolean intersects(final Ray ray) {
        throw new UnsupportedOperationException(
                "unimplemented Geometry::intersects() called!");
    }
    
    public DifferentialGeometry nearestIntersection(final Ray ray) {
        throw new UnsupportedOperationException(
                "unimplemented Geometry::nearestIntersection() called!");
    }

    /**
     * Wahrscheinlichkeit, dass der Schnittpunkt welcher der
     * Strahl (p, dir) mit dem Objekt hat durch einen Aufruf von
     * einer der sample(...) - Methoden zurückgegeben worden wäre.
     */
    public float pdf(final Point p, final Vector wi) {
        final Ray ray = new Ray(p, wi);
        final DifferentialGeometry dg = nearestIntersection(ray);
        if (dg == null) return 0.0f;
        
        float ad = Utils.absdot(dg.nn, wi.neg());
        if (ad == 0.0f) return Float.POSITIVE_INFINITY;
        
        return p.sub(ray.at(dg.t)).lengthSquared() / (ad * getArea());
    }
    
    /**
     * Wahrscheinlichkeit, dass ein Punkt auf der Oberfläche zufällig
     * gewählt wurde. Diese Implementierung gibt einfach 1 / Fläche
     * zurück.
     *
     * @param pShape ein Punkt auf der Oberfläche
     * @return Wahrscheinlichkeit, dass dieser Punkt zufällig gewählt wurde
     */
    public float pdf(final Point pGeom) {
        return 1.0f / getArea();
    }

    /**
     * If it is valid to call the {@link #sample(float, float)} method of
     * this geometry.
     *
     * @return if this geometry can be used as a light source.
     */
    public boolean canEmit() {
        return false;
    }
    
    /**
     * Gibt einen zufälligen Punkt auf der Oberfläche der 
     * Geometrie zurück.
     */
    public GeometrySample sample(float u, float v) {
        throw new UnsupportedOperationException("sample method not supported");
    }
    
    /**
     * Siehe {@link #getSample(float, float) }. Der zusätzliche
     * Punkt kann dazu genutzt werden, nur Punkte zu wählen,
     * die wenigstens eine Chance haben von p aus gesehen zu werden.
     * <p>
     * In dieser Implementierung wird der übergebene Punkt
     * ignoriert und der Aufruf an die sample(u, v) - Methode
     * weitergereicht.
     */
    public GeometrySample sample(float u, float v, final Point p) {
        return sample(u, v);
    }
    
    /**
     * Returns the surface area of this geometric object.
     */
    public abstract float getArea();
    
}
