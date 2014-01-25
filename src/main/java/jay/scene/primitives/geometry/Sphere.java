/*
 * Sphere.java
 *
 * Created on 15. Dezember 2005, 19:55
 */

package jay.scene.primitives.geometry;

import jay.maths.*;
import static java.lang.Math.*;
import jay.scene.primitives.DifferentialGeometry;

/**
 * @author Matthias Treydte
 */
public class Sphere extends Geometry {
    
    /** The radius of the sphere */
    private float radius;
    
    /** The squared radius of the sphere */
    private float sqradius;
    
    /**
     * Erzeugt eine neue Kugel mit einem Radius von 1.0
     */
    public Sphere() {
        this(1.0f);
    }
    
    /**
     * Creates a new sphere of the given radius.
     */
    public Sphere(float radius) {
        setRadius(radius);
    }
    
    /**
     * Sets the radius of the sphere.
     */
    public void setRadius(float radius) {
        this.radius = radius;
        this.sqradius = radius * radius;
    }
    
    /**
     * Returns the radius of the sphere.
     */
    public float getRadius() {
        return radius;
    }
    
    /**
     * Returns the bounding box of the sphere
     * in object coordinate space.
     */
    public AABB localBounds() {
        return new AABB(
                new Point(-radius, -radius, -radius),
                new Point(radius, radius, radius));
    }
    
    @Override
    public boolean intersects(final Ray ray) {
        final Ray r = w2g.apply(ray);
        final float a = r.d.lengthSquared();
        final float b = 2.0f * r.d.dot(r.o);
        final float c = r.o.vectorTo().lengthSquared() - sqradius;
        
        float[] sol = Utils.solveQuadric(a, b, c);
        if (sol == null) return false;
        if (sol[0] > r.tmax || sol[1] < r.tmin) return false;
        if (sol[0] < r.tmin && sol[1] > r.tmax) return false;
        return true;
    }
    
    @Override
    public DifferentialGeometry nearestIntersection(final Ray _r) {
        
        final Ray r = w2g.apply(_r);
        final float a = r.d.lengthSquared();
        final float b = 2.0f * r.d.dot(r.o);
        final float c = r.o.vectorTo().lengthSquared() - sqradius;
        
        /* Quadratische Gleichung lösen */
        
        float[] t = Utils.solveQuadric(a, b, c);
        
        if (t == null) return null;
        //printf("t1: %.3f\n", t1);
        if (t[0] > r.tmax || t[1] < r.tmin) return null;
        
        final boolean inside = (t[0] <= r.tmin);
        float dist;
        if (!inside) dist = t[0];
        else dist = t[1];
        
        final Point pi = r.at(dist);
        
        double phi = atan2(pi.y, pi.x);
        if (phi < 0.0) phi += 2.0 * PI;
        float u = (float)phi / 360.0f;
        double theta = acos(pi.z / radius);
        float v = (float)theta / 180.0f;
        
        /* dPdu / dPdv bestimmen */
        float zradius = (float)sqrt(pi.x * pi.x + pi.y * pi.y);
        Vector dpdu, dpdv;
        
        if (zradius == 0.0) {
            // Handle hit at degenerate parameterization point
            dpdv = new Vector(0.0f, pi.z, -radius * (float)sin(theta)).mul(180);
            dpdu = dpdv.cross(pi.vectorTo());
        } else {
            float invzradius = 1.0f / zradius;
            float cosphi = pi.x * invzradius;
            float sinphi = pi.y * invzradius;
            dpdu = new Vector(-180.0f * pi.y, 180.0f * pi.x, 0.0f);
            dpdv = new Vector(pi.z * cosphi, pi.z * sinphi,
                    -radius * (float)sin(theta)).mul(180);
        }
        
        return new DifferentialGeometry(
                g2w.apply(pi), dist,
                g2w.apply(dpdu), g2w.apply(dpdv),
                u, v, this);
    }
    
    @Override
    public GeometrySample sample(float u, float v) {
        final Point p = new Point().add(
                Utils.uniformSampleSphere(u, v).mul(radius));
        
        Normal n = g2w.apply(new Normal(p.x, p.y, p.z)).normalized();
        if (invertNormals) n = n.mul(-1.0f).asNormal();
        
        return new GeometrySample(g2w.apply(p), n);
    }
    
    @Override
    public GeometrySample sample(float u, float v, final Point p) {
        final Point pCenter = g2w.apply(new Point(0, 0, 0));
        final float sqdist = pCenter.sub(p).lengthSquared();
        
        /* wenn p im inneren der Kugel liegt ist natürlich die
         * gesamte Kugeloberfläche sichtbar
         */
        if (sqdist - sqradius < 1e-4f) return sample(u, v);
        
        final Vector wc = (pCenter.sub(p)).normalized();
        final Vector[] wcS = Utils.coordinateSystem(wc);
        final Vector wcX = wcS[0];
        final Vector wcY = wcS[1];
        
        final float costhetamax = (float)Math.sqrt(
                Math.max(0.0f, 1.0f - sqradius / sqdist));
        
        final Ray r = new Ray(p, Utils.uniformSampleCone(u, v, costhetamax,
                wcX, wcY, wc));
        
        final DifferentialGeometry dg = nearestIntersection(r);

        Point ps;
        if (dg == null) ps = pCenter.sub(wc.mul(radius));
        else ps = dg.p;
        
        return new GeometrySample(ps, ps.sub(pCenter).normalized());
    }
    
    @Override
    public float pdf(final Point p, final Vector wi) {
        Point Pcenter = g2w.apply(new Point(0,0,0));
        
        /* Punkt ist in der Kugel */
        if ((p.sub(Pcenter).lengthSquared() - sqradius < 1e-4f))
            return super.pdf(p, wi);
        
        float cosThetaMax = (float)Math.sqrt(Math.max(0.0f, 1.0f - sqradius /
                p.sub(Pcenter).lengthSquared()));

        return Utils.uniformConePDF(cosThetaMax);
    }
    
    /**
     * Returns the normal at a point on the sphere
     * surface.
     */
    public Normal getNormal(final Point pi) {
        return pi.vectorTo().normalized();
    }
    
    public float getArea() {
        return 4.0f * sqradius * (float)Math.PI;
    }
    
    @Override
    public String toString() {
        return "Sphere (r=" + radius + ", " +
                "pos=" + g2w.apply(new Point(0, 0, 0)) + ")";
    }
    
}
