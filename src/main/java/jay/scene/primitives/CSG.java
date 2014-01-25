/*
 * CSG.java
 *
 * Created on 6. Januar 2006, 01:00
 */

package jay.scene.primitives;

import java.util.Iterator;
import java.util.List;
import jay.maths.*;

/**
 *
 * @author Matthias Treydze
 */
public abstract class CSG extends Primitive {
    
    public static enum Mode { UNION, INTERSECTION, DIFFERENCE };
    
    Intersectable a = null;
    Intersectable b = null;
    Mode mode = Mode.UNION;
    
    /** Erstellt eine neue Instanz von CSG */
    public CSG() {
    }
    
    public CSG(Primitive a, Primitive b, Mode mode) {
        this.a = a;
        this.b = b;
        this.mode = mode;
    }
    
    public AABB worldBounds() {
        
        switch (mode) {
            case UNION:
                return AABB.join(a.worldBounds(), a.worldBounds());
            case INTERSECTION:
                return AABB.intersect(a.worldBounds(), b.worldBounds());
            default:
                return a.worldBounds();
        }
        
    }
    
    @Override
    public boolean intersects(Ray ray) {
        switch (mode) {
            case UNION:
                return (a.intersects(ray) || b.intersects(ray));
            case INTERSECTION:
                return intersectionIntersects(ray);
            default:
                return false;
        }
    }
    
    public boolean intersectionIntersects(Ray ray) {
        float oldMin = ray.tmin;
        float oldMax = ray.tmax;
        
        ray.tmin -= worldBounds().boundingSphereRadius() * 2;
        
        IntersectionIterator ii = new IntersectionIterator(ray);
        final Vector d = ray.d.normalized();
        Intersection last = null;
        boolean lastEnter = false;
        
        while (ii.hasNext()) {
            Intersection next = ii.next();
            
            if (last == null || !lastEnter) {
                last = next;
                lastEnter = (last.dg.nn.dot(d)) < 0.0f;
                continue;
            }
            
            if (next.dg.nn.dot(d) < 0.0f) {
                ray.tmin = oldMin;
                ray.tmax = oldMax;
                return true;
            } else {
                lastEnter = false;
            }
            
        }
        
        ray.tmin = oldMin;
        ray.tmax = oldMax;
        return false;
    }
    
    @Override
    public Intersection nearestIntersection(Ray ray) {
        switch (mode) {
            case UNION:
                return nearOfUnion(ray);
            case INTERSECTION:
                return nearOfIntersection(ray);
            default:
                return nearOfDifference(ray);
        }
    }
    
    public Intersection nearOfDifference(Ray ray) {
        float oldMin = ray.tmin;
        float oldMax = ray.tmax;
        
        ray.tmin -= worldBounds().boundingSphereRadius() * 2;
        
        IntersectionIterator ii = new IntersectionIterator(ray);
        final Vector d = ray.d.normalized();
        Intersection last = null;
        
        while (ii.hasNext()) {
            Intersection next = ii.next();
            boolean entering = (next.dg.nn.dot(d) < 0.0f);
            
            if ((last == null) && (next.prim == a) && entering) {
                /* a wurde zuerst betreten */
                return next;
            }
            
            if ((last == null) && (next.prim == b) && entering) {
                /* b zuerst */
                last = next;
                continue;
            }
            
            if (!entering && (last != null) && (last.prim == b) && (next.prim == a)) {
                //last.dg.nn = last.dg.nn.neg();
                return last;
            } else {
                //last = null;
            }
        }
        
        ray.tmin = oldMin;
        ray.tmax = oldMax;
        return null;
    }
    
    public Intersection nearOfIntersection(Ray ray) {
        float oldMin = ray.tmin;
        float oldMax = ray.tmax;
        
        ray.tmin -= worldBounds().boundingSphereRadius() * 2;
        
        IntersectionIterator ii = new IntersectionIterator(ray);
        final Vector d = ray.d.normalized();
        Intersection last = null;
        boolean lastEnter = false;
        
        while (ii.hasNext()) {
            Intersection next = ii.next();
            boolean entering = (next.dg.nn.dot(d) < 0.0f);
            if (last == null || !lastEnter) {
                last = next;
                lastEnter = entering;
                continue;
            }
            
            if (entering) {
                ray.tmin = oldMin;
                ray.tmax = next.dg.t;
                
                if (next.dg.t <= oldMin) {
                    return next;
                } else {
                    last = next;
                    lastEnter = entering;
                }
                
            } else {
                lastEnter = false;
            }
            
        }
        
        ray.tmin = oldMin;
        ray.tmax = oldMax;
        return null;
    }
    
    public Intersection nearOfUnion(Ray ray) {
        Intersection isA = a.nearestIntersection(ray);
        if (isA == null) return b.nearestIntersection(ray);
        Intersection isB = b.nearestIntersection(ray);
        if (isB == null) return isA;
        if (isA.dg.t < isB.dg.t) return isA;
        else return isB;
    }

    @Override
    public boolean canIntersect() {
        return true;
    }

    public void refine(List<Primitive> plist) {
        throw new UnsupportedOperationException("not supported");
    }

    class IntersectionIterator implements Iterator<Intersection> {
        
        private Ray rayA, rayB;
        private Intersection nextA, nextB;
        private float tmax;
        
        public IntersectionIterator(Ray ray) {
            this.rayA = new Ray(ray);
            this.rayB = new Ray(ray);
            this.tmax = ray.tmax;
            nextA = a.nearestIntersection(rayA);
            nextB = b.nearestIntersection(rayB);
        }
        
        public boolean hasNext() {
            return ((nextA != null) || (nextB != null));
        }
        
        public Intersection next() {
            Intersection ret = null;
            
            /* nächsten Schnittpunkt wählen */
            if (nextA == null) {
                ret = nextB;
            } else {
                if (nextB == null) {
                    ret = nextA;
                } else {
                    if (nextA.dg.t < nextB.dg.t) {
                        ret = nextA;
                    } else {
                        ret = nextB;
                    }
                }
            }
            
            /* diesen eines weitersetzen */
            if ((ret == nextA) && (ret != null)) {
                rayA.tmin = rayA.tmax + Ray.EPSILON;
                rayA.tmax = tmax;
                nextA = a.nearestIntersection(rayA);
            } else if ((ret == nextB) && (ret != null)) {
                rayB.tmin = rayB.tmax + Ray.EPSILON;
                rayB.tmax = tmax;
                nextB = b.nearestIntersection(rayB);
            }
            
            return ret;
        }
        
        public void remove() {
            throw new UnsupportedOperationException(
                    "you can try to ignore me, but you can't " +
                    "change the truth!");
        }
        
    }
    
}