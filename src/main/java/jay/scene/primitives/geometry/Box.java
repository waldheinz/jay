/*
 * Box.java
 *
 * Created on 17. Dezember 2005, 13:13
 */

package jay.scene.primitives.geometry;

import jay.maths.*;
import jay.scene.primitives.DifferentialGeometry;
import static jay.maths.Utils.*;

/**
 * @author Matthias Treydte
 */
public final class Box extends Geometry {
    
    /** The bounds of this box */
    private AABB box;

    /** The inverse of the size of this box. */
    private Vector invSize;

    /**
     * Creates a new cube with an edge length of 1.
     */
    public Box() {
        this(1, 1, 1);
    }

    /**
     * Creates a new box matching the given bounds.
     *
     * @param bounds the bounds of the new box.
     */
    public Box(AABB bounds) {
        this.box = bounds;
        this.invSize = bounds.diagonal().inv();
    }

    /**
     * Creates a new box with the given sizes.
     *
     * @param dx
     * @param dy
     * @param dz
     */
    public Box(float dx, float dy, float dz) {
        assert (dx > 0 && dy > 0 && dz > 0) : "Negative size given."; //NOI18N
        setExtents(dx, dy, dz);
    }
    
    public void setExtents(float dx, float dy, float dz) {
        float maxx = dx / 2.0f; float minx = - maxx;
        float maxy = dy / 2.0f; float miny = - maxy;
        float maxz = dz / 2.0f; float minz = - maxz;
        
        invSize = new Vector(1.0f / dx, 1.0f / dy, 1.0f / dz);
        
        box = new AABB(
              new Point(minx, miny, minz),
              new Point(maxx, maxy, maxz));
    }
    
    @Override
    public DifferentialGeometry nearestIntersection(final Ray _ray) {
        final Ray ray = w2g.apply(_ray);
        
        float[] is = box.intersections(ray);
        
        if (is == null) return null;
        
        final boolean inside = (is[0] < 0.0);
        
        float dist = (inside)?is[1]:is[0];
        float u, v;
        final Point pi = ray.at(dist);
        Vector dpdu, dpdv;
        
        /* der Schnittpunkt wird in einen Punkt auf der
         * Oberfläche des Einheitswürfels transformiert,
         * dann bestimmt die "längste" Achse die Normale
         */
        final Vector invPi = pi.vectorTo().mul(invSize);
        switch (invPi.dominantAxis()) {
            case 0:
                dpdu = new Vector(0.0f, 0.0f, box.d());
                dpdv = new Vector(0.0f, box.h(), 0.0f);
                u = invPi.y; v = invPi.z;
                break;
            case 1:
                dpdu = new Vector(box.w(), 0.0f, 0.0f);
                dpdv = new Vector(0.0f, 0.0f, box.d());
                u = invPi.x; v = invPi.z;
                break;
            default:
                dpdu = new Vector(box.w(), 0.0f, 0.0f);
                dpdv = new Vector(0.0f, box.h(), 0.0f);
                u = invPi.x; v = invPi.y;
                break;
        }
        
        return new DifferentialGeometry(
              g2w.apply(pi),
              dist,
              g2w.apply(dpdu),
              g2w.apply(dpdv),
              u, v, this);
    }
    
    public AABB localBounds() {
        return box;
    }
    
    public String getName() {
        return "Box";
    }
    
    @Override
    public boolean intersects(Ray ray) {
        Ray r = w2g.apply(ray);
        return box.intersects(r);
    }
    
    public float getArea() {
        return 2.0f * (
              box.w() * box.h() +
              box.h() * box.d() +
              box.d() * box.w());
    }
    
    /**
     * Wählt aus den übergebenden Flächen eine aus.
     *
     * @param faces die Seiten, aus denen gewählt werden soll
     * @param area die Gesamtfläche der in faces enthaltenen Seiten
     * @return eine Sample
     */
    private GeometrySample selectFace(Face[] faces, float area,
          float u, float v) {
        
        float chosen = lerp(u, 0, area);
        float sum = faces[0].area;
        float last = 0;
        int off = 0;
        
        while (chosen > sum) {
            last = sum;
            sum += faces[++off].area;
        }
        
        /* u neu skalieren */
        u = (chosen - last) / (sum - last);

        return new GeometrySample(
            g2w.apply(faces[off].sample(u, v)),
            g2w.apply(faces[off].normal()));
    }
    
    @Override
    public GeometrySample sample(float u, float v) {
        Face[] faces = {
            new Face(AABB.OUT_BACK),
                  new Face(AABB.OUT_BOTTOM),
                  new Face(AABB.OUT_FRONT),
                  new Face(AABB.OUT_LEFT),
                  new Face(AABB.OUT_RIGHT),
                  new Face(AABB.OUT_TOP)
        };
        
        return selectFace(faces, getArea(), u, v);
    }
    
    @Override
    public GeometrySample sample(float u, float v, Point p) {
        p = w2g.apply(p);

        /* bestimmen, welche Seiten von p aus sichtbar sind */
        
        if (true) return sample(u, v);
        
        final int outCode = box.getOutcode(p);
        
        /* der Punkt befindet sich in der Box; von dort aus
         * sind natürlich alle Seiten sichtbar
         */
        if (outCode == 0) return sample(u, v);
        
        float area = 0;
        
        Face[] faces = new Face[3];
        int off = 0;
        for (int i=0; i < 6; i++) {
            if ((outCode & (1 << i)) != 0) {
                faces[off] = new Face(1 << i);
                area += faces[off++].area;
            }
        }

        return selectFace(faces, area, u, v);
    }
    
    @Override
    public boolean canEmit() {
        return true;
    }
    
    public float pdf1(Point p, Vector dir) {
        final Point p1 = w2g.apply(p);
        final int outCode = box.getOutcode(p1);
        if (outCode == 0) return super.pdf(p, dir);
        
        Ray ray = new Ray(p, w2g.apply(dir));
        
        DifferentialGeometry dg = nearestIntersection(ray);
        
        if (dg == null) return 0.0f;
        Normal dn = dir.normalized();
        float ad = Utils.absdot(dg.nn, dn.neg());
        if (ad == 0.0f) return Float.POSITIVE_INFINITY;
        
        float allArea = 0;
        
        for (int i=0; i < 6; i++) {
            if ((outCode & (1 << i)) != 0) {
                allArea += new Face(1 << i).area;
            }
        }
        
        return p.sub(ray.at(dg.t)).lengthSquared() / ad * getArea();
        
//        float hitArea = new Face(box.firstHitFace(
//              new Ray(p1, w2g.apply(dir)))).area;
//        
//        float pFace = allArea / hitArea;
//        float pP = hitArea;
//        
//        return (pFace * pP);
    }

    @Override
    public String toString() {
        return "Box [extents=" + worldBounds() + "]";
    }
    
    /**
     * A face of this box.
     */
    private class Face {
        
        final int outCode;
        final float area;
        
        public Face(int outCode) {
            this.outCode = outCode;
            this.area = calcArea();
        }
        
        float calcArea() {
            switch(outCode) {
                case AABB.OUT_TOP: case AABB.OUT_BOTTOM:
                    return box.w() * box.d();
                case AABB.OUT_BACK: case AABB.OUT_FRONT:
                    return box.w() * box.h();
                default:
                    return box.d() * box.h();
            }
        }
        
        public Normal normal() {
            switch (outCode) {
                case AABB.OUT_TOP: return new Normal(0, 1, 0);
                case AABB.OUT_BOTTOM: return new Normal(0, -1, 0);
                case AABB.OUT_FRONT: return new Normal(0, 0, -1);
                case AABB.OUT_BACK: return new Normal(0, 0, 1);
                case AABB.OUT_LEFT: return new Normal(-1, 0, 0);
                default: return new Normal(1, 0, 0);
            }
        }
        
        public Point sample(float u, float v) {
            final Point p1 = box.min;
            final Point p2 = box.max;
            
            switch (outCode) {
                case AABB.OUT_BOTTOM:
                    return new Point(
                          lerp(u, p1.x, p2.x), p1.y, lerp(v, p1.z, p2.z));
                case AABB.OUT_TOP:
                    return new Point(
                          lerp(u, p1.x, p2.x), p2.y, lerp(v, p1.z, p2.z));
                case AABB.OUT_FRONT:
                    return new Point(
                          lerp(u, p1.x, p2.x), lerp(v, p1.y, p2.y), p1.z);
                case AABB.OUT_BACK:
                    return new Point(
                          lerp(u, p1.x, p2.x), lerp(v, p1.y, p2.y), p2.z);
                case AABB.OUT_LEFT:
                    return new Point(
                          p1.x, lerp(u, p1.y, p2.y), lerp(v, p1.z, p2.z));
                default:
                    return new Point(
                          p2.x, lerp(u, p1.y, p2.y), lerp(v, p1.z, p2.z));
            }
        }
    }
    
}
