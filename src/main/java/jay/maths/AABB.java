/*
 * AABB.java
 *
 * Created on 15. Dezember 2005, 14:55
 */

package jay.maths;

import static java.lang.Math.*;

/**
 * Axis Aligned Bounding Box
 *
 * @author Matthias Treydte
 */
public final class AABB {
    
    /** links von der Box (-x) */
    public final static int OUT_LEFT   = 1 << 0;
    
    /** rechts von der Box (+x) */
    public final static int OUT_RIGHT  = 1 << 1;
    
    /** über der Box (+y) */
    public final static int OUT_TOP    = 1 << 2;
    
    /** unter der Box (-y) */
    public final static int OUT_BOTTOM = 1 << 3;
    
    /** vor der Box (-z) */
    public final static int OUT_FRONT  = 1 << 4;
    
    /** hinter der Box (+z) */
    public final static int OUT_BACK   = 1 << 5;
    
    public final static AABB EMPTY = new AABB(
            new Point(Float.POSITIVE_INFINITY,
                    Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
            new Point(Float.NEGATIVE_INFINITY,
                    Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY),
            true);
    
    public final Point min, max;

    /**
     * Gibt den outcode der vom übergebenen Strahl zuerst
     * getroffenen Fläche zurück.
     *
     * @param ray der zu testende Strahl
     * @return outcode der getroffenen Fläche
     */
    public int firstHitFace(Ray ray) {
        float[] is = intersections(ray);
        if (is == null) return 0;
        float t = is[0] > 0?is[0]:is[1];
        Point p = ray.at(t - Ray.EPSILON);
        return getOutcode(p);
    }
    
    public AABB(final Point p1, final Point p2) {
        this(p1, p2, false);
    }
    
    /**
     * Creates a new bounding box, where the minimum
     * and maximum are already sorted.
     * 
     * @param sorted If the parameters are already sorted.
     *      For this to be true, p1 has to be component-wise
     *      less than p2, or unexpected behavior has to
     *      be expected.
     */
    public AABB(final Point p1, final Point p2, boolean sorted) {
        if (sorted) {
            min = p1;
            max = p2;
        } else {
            min = new Point(min(p1.x, p2.x), min(p1.y, p2.y), min(p1.z, p2.z));
            max = new Point(max(p1.x, p2.x), max(p1.y, p2.y), max(p1.z, p2.z));
        }
    }
    
    /**
     * Determines the axis in which this box has the maximum size.
     * 
     * @return The code for the axis of maximum extent.
     */
    public int maximumExtent() {
        return diagonal().dominantAxis();
    }
    
    /**
     * Returns the diagonal vector of the box. This is a vector 
     * from {@link #min} to {@link #max}.
     *
     * @return The diagonal vector.
     */
    public Vector diagonal() {
        return max.sub(min);
    }
    
    public AABB[] split(float t, int axis) {
        if ((axis < 0) || (axis > 2))
            throw new IllegalArgumentException("axis not in range (" + axis +")");
        
        if ((t < min.get(axis)) || (t > max.get(axis)))
            throw new IllegalArgumentException("t not in range (min: " +
                  min.get(axis) + ", max: " + max.get(axis) + ", t: " + t +")");
        
        AABB res[] = new AABB[2];
        res[0] = new AABB(min, max.set(t, axis), true);
        res[1] = new AABB(min.set(t, axis), max, true);
        return res;
    }
    
    public int getOutcode(final Point p) {
        int outcode = 0;
        
        if (p.x > max.x) {
            outcode |= OUT_RIGHT;
        } else if (p.x < min.x) {
            outcode |= OUT_LEFT;
        }
        
        if (p.y > max.y) {
            outcode |= OUT_TOP;
        } else if (p.y < min.y) {
            outcode |= OUT_BOTTOM;
        }
        
        if (p.z > max.z) {
            outcode |= OUT_BACK;
        } else if (p.z < min.z) {
            outcode |= OUT_FRONT;
        }
        
        return outcode;
    }
    
    /**
     * Gibt <code>true</code> zur�ck, wenn diese AABB von b2 �berlappt wird.
     */
    public boolean intersects(final AABB b2) {
        final Point min2 = b2.min;
        final Point max2 = b2.max;
        final Point min1 = min;
        final Point max1 = max;
        
        return
                ((max1.x > min2.x) && (min1.x <= max2.x) &&  /* X - Achse */
                (max1.y > min2.y) && (min1.y <= max2.y) &&  /* Y - Achse */
                (max1.z > min2.z) && (min1.z <= max2.z));   /* Z - Achse */
    }
    
    /**
     * Erweitert die #AABB so, da� sie den übergebenen Punkt
     * enthält (wenn er nicht ohnehin schon enthalten war).
     */
    public AABB extend(final Point p) {
        Point nmin = new Point(
                min(min.x, p.x),
                min(min.y, p.y),
                min(min.z, p.z));
        
        Point nmax = new Point(
                max(max.x, p.x),
                max(max.y, p.y),
                max(max.z, p.z));
        
        return new AABB(nmin, nmax, true);
    }
    
    public Point boundingSphereCenter() {
        return new Point(0,0,0).add(min.add(
                max.vectorTo()).vectorTo().mul(0.5f));
    }
    
    public float boundingSphereRadius() {
        final Point center = boundingSphereCenter();
        return boundingSphereRadius(center);
    }
    
    public float boundingSphereRadius(final Point center) {
        return contains(center) ? center.sub(max).length() : 0.0f;
    }
    
    boolean contains(final Point p) {
        return ((p.x >= min.x) && (p.x < max.x) &&
                (p.y >= min.y) && (p.y < max.y) &&
                (p.z >= min.z) && (p.z < max.z));
    }
    
    
    /**
     * Erweitert diese Box so, dass sie die übergebene mit
     * einschließt.
     */
    public AABB extend(final AABB box) {
        Point nmin = new Point(
                min(min.x, box.min.x),
                min(min.y, box.min.y),
                min(min.z, box.min.z));
        
        Point nmax = new Point(
                max(max.x, box.max.x),
                max(max.y, box.max.y),
                max(max.z, box.max.z));
        
        return new AABB(nmin, nmax, true);
    }
    
    public float w() { return max.x - min.x; }
    public float h() { return max.y - min.y; }
    public float d() { return max.z - min.z; }
    
    /**
     * Gibt eine neue AABB zurück, welche die beiden übergebenen
     * AABBs einschließt.
     */
    public static AABB join(final AABB b1, final AABB b2) {
        if (b1 == null) return b2;
        if (b2 == null) return b1;
        
        return new AABB(
                new Point(
                min(b1.min.x, b2.min.x),
                min(b1.min.y, b2.min.y),
                min(b1.min.z, b2.min.z)),
                
                new Point(
                max(b1.max.x, b2.max.x),
                max(b1.max.y, b2.max.y),
                max(b1.max.z, b2.max.z)),
                true);
    }
    
    public static AABB intersect(final AABB b1, final AABB b2) {
        if (b1 == null || b2 == null) return null;
        
        return new AABB(
                new Point(
                max(b1.min.x, b2.min.x),
                max(b1.min.y, b2.min.y),
                max(b1.min.z, b2.min.z)),
                
                new Point(
                min(b1.max.x, b2.max.x),
                min(b1.max.y, b2.max.y),
                min(b1.max.z, b2.max.z)),
                true);
    }
    
    public float[] intersections(final Ray ray) {
        float tmin, tmax, tymin, tymax, tzmin, tzmax;
        
        final Vector D    = ray.d;
        final Vector D_inv = new Vector(1.0f / D.x, 1.0f / D.y, 1.0f / D.z);
        final Point O     = ray.o;
        
        final Point p0 = min;
        final Point p1 = max;
        
        Point parameters[] = { p0, p1 };
        int sign[] = { (D_inv.x < 0)?1:0, (D_inv.y < 0)?1:0, (D_inv.z < 0)?1:0 };
        
        tmin  = (parameters[    sign[0] ].x - O.x) * D_inv.x;
        tmax  = (parameters[1 - sign[0] ].x - O.x) * D_inv.x;
        
        tymin = (parameters[    sign[1] ].y - O.y) * D_inv.y;
        tymax = (parameters[1 - sign[1] ].y - O.y) * D_inv.y;
        
        if ( (tmin > tymax) || (tymin > tmax) ) return null;
        
        if (tymin > tmin) tmin = tymin;
        if (tymax < tmax) tmax = tymax;
        
        tzmin = (parameters[    sign[2] ].z - O.z) * D_inv.z;
        tzmax = (parameters[1 - sign[2] ].z - O.z) * D_inv.z;
        
        if ( (tmin > tzmax) || (tzmin > tmax) ) return null;
        
        if (tzmin > tmin) tmin = tzmin;
        if (tzmax < tmax) tmax = tzmax;
        
        final boolean hit = ( (tmin < ray.tmax) && (tmax > ray.tmin)  );
        if (hit) return new float[] { tmin, tmax };
        else return null;
    }
    
    public boolean intersects(final Ray ray) {
        float tmin, tmax, tymin, tymax, tzmin, tzmax;
        
        final Vector D    = ray.d;
        final Vector D_inv = new Vector(1.0f / D.x, 1.0f / D.y, 1.0f / D.z);
        final Point O     = ray.o;
        
        final Point p0 = min;
        final Point p1 = max;
        
        Point parameters[] = { p0, p1 };
        int sign[] = { (D_inv.x < 0)?1:0, (D_inv.y < 0)?1:0, (D_inv.z < 0)?1:0 };
        
        tmin  = (parameters[    sign[0] ].x - O.x) * D_inv.x;
        tmax  = (parameters[1 - sign[0] ].x - O.x) * D_inv.x;
        
        tymin = (parameters[    sign[1] ].y - O.y) * D_inv.y;
        tymax = (parameters[1 - sign[1] ].y - O.y) * D_inv.y;
        
        if ( (tmin > tymax) || (tymin > tmax) ) return false;
        
        if (tymin > tmin) tmin = tymin;
        if (tymax < tmax) tmax = tymax;
        
        tzmin = (parameters[    sign[2] ].z - O.z) * D_inv.z;
        tzmax = (parameters[1 - sign[2] ].z - O.z) * D_inv.z;
        
        if ( (tmin > tzmax) || (tzmin > tmax) ) return false;
        
        if (tzmin > tmin) tmin = tzmin;
        if (tzmax < tmax) tmax = tzmax;
        
        return ((tmin < ray.tmax) && (tmax > ray.tmin));
    }

    @Override
    public String toString() {
        return "AABB [min=" + min + ", max=" + max + "]";
    }
    
}
