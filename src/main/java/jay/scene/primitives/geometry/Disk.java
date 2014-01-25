
package jay.scene.primitives.geometry;

import jay.maths.AABB;
import jay.maths.Normal;
import jay.maths.Point;
import jay.maths.Ray;
import jay.maths.Transform;
import jay.maths.Utils;
import jay.maths.Vector;
import jay.scene.primitives.DifferentialGeometry;

/**
 * A Disk with a specific radius and position along the y-axis.
 *
 * @author Matthias Treydte <waldheinz@gmail.com>
 */
public class Disk extends Geometry {

    private final float height;
    private final float radius;
    private final float innerRadius;

    public Disk(Transform o2w, boolean reverse,
            float height, float radius, float innerRadius) {

        super(o2w, reverse);
        
        this.height = height;
        this.radius = radius;
        this.innerRadius = innerRadius;
    }

    @Override
    public AABB localBounds() {
        return new AABB(
                new Point(-radius, -radius, height),
                new Point(radius, radius, height));
    }

    @Override
    public float getArea() {
        return (float)Math.PI * radius * radius;
    }

    @Override
    public boolean canIntersect() {
        return true;
    }

    @Override
    public boolean intersects(Ray ray) {
        ray = w2g.apply(ray);
        if (Math.abs(ray.d.z) < 1e-7) return false; /* parallel - can't hit */

        float tHit = (height - ray.o.z) / ray.d.z;
        if (tHit < ray.tmin || tHit > ray.tmax) /* too close / far */
            return false;

        Point pHit = ray.at(tHit);
        /* dist. from center, squared */
        float dist2 = pHit.x * pHit.x + pHit.y * pHit.y; 
        if (dist2 > radius * radius || dist2 < innerRadius * innerRadius)
            return false;

        return true;
    }

    @Override
    public DifferentialGeometry nearestIntersection(Ray ray) {
        ray = w2g.apply(ray);
        if (Math.abs(ray.d.z) < 1e-7) return null; /* parallel - can't hit */
                float tHit = (height - ray.o.z) / ray.d.z;

        if (tHit < ray.tmin || tHit > ray.tmax) /* too close / far */
            return null;

        Point pi = ray.at(tHit);
        /* dist. from center, squared */
        float dist2 = pi.x * pi.x + pi.y * pi.y;
        if (dist2 > radius * radius || dist2 < innerRadius * innerRadius)
            return null;

        float phi = (float) Math.atan2(pi.y, pi.x);
        if (phi < 0) phi += 2.0f * Math.PI;

        final float u = phi / (2.0f * (float)Math.PI);
        final float v = 1.0f - (((float)Math.sqrt(dist2) - innerRadius) /
	                 (radius - innerRadius));

        final Vector dpdu = new Vector(pi.y, pi.x, 0);
        final Vector dpdv = new Vector(-pi.x / (1-v), -pi.y / (1-v), 0).
                mul((radius - innerRadius) / radius);

        return new DifferentialGeometry(
              g2w.apply(pi),
              (float)Math.sqrt(dist2),
              g2w.apply(dpdu),
              g2w.apply(dpdv),
              u, v, this);
    }

    @Override
    public GeometrySample sample(float u, float v) {
        float[] xy = Utils.concentricSampleDisk(u, v);
        final Point pt = new Point(xy[0] * radius, xy[1] * radius, height);
        
        return new GeometrySample(
                g2w.apply(pt),
                g2w.apply(new Normal(0, 0, invertNormals ? -1 : 1)));
    }

    @Override
    public boolean canEmit() {
        return true;
    }
}
