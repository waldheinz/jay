/*
 * Triangle.java
 *
 * Created on 21. Februar 2006, 17:28
 */

package jay.scene.primitives.geometry;

import jay.maths.*;
import jay.maths.AABB;
import jay.scene.primitives.DifferentialGeometry;

/**
 *
 * @author Matthias Treydte <waldheinz@gmail.com>
 */
public final class Triangle extends Geometry {
    
    /** das Mesh, zu welchem dieses Dreieck gehört */
    final TriangleMesh mesh;
    
    /** Offset in das Vertex - Index - Array des Meshes */
    final int vOff;
    
    /** Creates a new instance of Triangle */
    public Triangle(final TriangleMesh m, int n) {
        mesh = m;
        vOff = n * 3;
        this.g2w = m.g2w;
        this.w2g = m.w2g;
    }
    
    public AABB localBounds() {
        AABB bounds = AABB.EMPTY;
        bounds = bounds.extend(w2g.apply(mesh.getPoint(vOff)));
        bounds = bounds.extend(w2g.apply(mesh.getPoint(vOff+1)));
        bounds = bounds.extend(w2g.apply(mesh.getPoint(vOff+2)));
        return bounds;
    }
    
    @Override
    public GeometrySample sample(float u1, float u2) {
        float[] b12 = Utils.uniformSampleTriangle(u1, u2);
        
        final Point p1 = mesh.getPoint(vOff);
        final Point p2 = mesh.getPoint(vOff+1);
        final Point p3 = mesh.getPoint(vOff+2);
        
        Point p = p1.mul(b12[0]).add(
                p2.mul(b12[1])).add(
                p3.mul(1.0f - b12[0] - b12[1]));
        
        Normal n = p2.sub(p1).cross(p3.sub(p1)).normalized();
        
        if (this.invertNormals) 
            n = n.neg().asNormal();
        
        return new GeometrySample(p, n);
    }
    
    public float getArea() {
        final Point p1 = mesh.getPoint(vOff);
        final Point p2 = mesh.getPoint(vOff+1);
        final Point p3 = mesh.getPoint(vOff+2);
        return 0.5f * p2.sub(p1).cross(p3.sub(p1)).length();
    }
    
    @Override
    public AABB worldBounds() {
        AABB bounds = AABB.EMPTY;
        bounds = bounds.extend(mesh.getPoint(vOff));
        bounds = bounds.extend(mesh.getPoint(vOff+1));
        bounds = bounds.extend(mesh.getPoint(vOff+2));
        return bounds;
    }
    
    @Override
    public boolean intersects(final Ray ray) {
        final Point p1 = mesh.getPoint(vOff);
        final Point p2 = mesh.getPoint(vOff+1);
        final Point p3 = mesh.getPoint(vOff+2);
        
        final Vector e1 = p2.sub(p1);
        final Vector e2 = p3.sub(p1);
        final Vector s1 = ray.d.cross(e2);
        
        final float divisor = s1.dot(e1);
        
        /* degenerate triangle? */
        if (divisor == 0.0f) return false;
        final float invDivisor = 1.0f / divisor;
        
        /* check first barycentric coordinate */
        final Vector d = ray.o.sub(p1);
        final float b1 = d.dot(s1) * invDivisor;
        if (b1 < 0.0f || b1 > 1.0f) return false;
        
        /* check second barycentric coordinate */
        final Vector s2 = d.cross(e1);
        final float b2 = ray.d.dot(s2) * invDivisor;
        if (b2 < 0.0f || b1 + b2 > 1.0f) return false;
        
        /* check distance to intersection point */
        final float t = e2.dot(s2) * invDivisor;
        if (t < ray.tmin || t > ray.tmax) return false;
        
        return true;
    }
    
    @Override
    public DifferentialGeometry nearestIntersection(final Ray ray) {
        final Point p1 = mesh.getPoint(vOff);
        final Point p2 = mesh.getPoint(vOff+1);
        final Point p3 = mesh.getPoint(vOff+2);
        
        final Vector e1 = p2.sub(p1);
        final Vector e2 = p3.sub(p1);
        final Vector s1 = ray.d.cross(e2);
        
        final float divisor = s1.dot(e1);
        if (divisor == 0.0f) return null;
        final float invDivisor = 1.0f / divisor;
        
        /* erste Koordinate */
        final Vector d = ray.o.sub(p1);
        final float b1 = d.dot(s1) * invDivisor;
        if (b1 < 0.0f || b1 > 1.0f) return null;
        
        /* zweite Koordinate */
        final Vector s2 = d.cross(e1);
        final float b2 = ray.d.dot(s2) * invDivisor;
        if (b2 < 0.0f || b1 + b2 > 1.0f) return null;
        
        /* Entfernung zum Schnittpunkt */
        final float t = e2.dot(s2) * invDivisor;
        if (t < ray.tmin || t > ray.tmax) return null;
        
        /* Parameter am Schnittpunkt bestimmen */
        
        /* Ableitungen */
        Vector dpdu, dpdv;
        float[][] uvs = new float[3][2];
        getUVs(uvs);
        
        /* Deltas bestimmen */
        final float du1 = uvs[0][0] - uvs[2][0];
        final float du2 = uvs[1][0] - uvs[2][0];
        final float dv1 = uvs[0][1] - uvs[2][1];
        final float dv2 = uvs[1][1] - uvs[2][1];
        final Vector dp1 = p1.sub(p3), dp2 = p2.sub(p3);
        final float determinant = du1 * dv2 - dv1 * du2;
        
        if (determinant == 0.0f || true) {
            Vector[] dpduv = Utils.coordinateSystem(e1.cross(e2).normalized());
            dpdu = dpduv[0];
            dpdv = dpduv[1];
        } else {
            final float invdet = 1.0f / determinant;
            dpdu = ( dp1.mul( dv2).sub(dp2.mul(dv1)) ).mul(invdet);
            dpdv = ( dp1.mul(-du2).add(dp2.mul(du1)) ).mul(invdet);
        }
        
        /* interpolieren */
        float b0 = 1 - b1 - b2;
        float tu = b0*uvs[0][0] + b1*uvs[1][0] + b2*uvs[2][0];
        float tv = b0*uvs[0][1] + b1*uvs[1][1] + b2*uvs[2][1];
        
        return new DifferentialGeometry(
              ray.at(t), t,
              dpdu, dpdv,
              tu, tv, this);
    }
    
    protected void getUVs(float[][] uv) {
        if (mesh.uvs != null) {
            uv[0][0] = mesh.uvs[2*mesh.getIndex(vOff+0)];
            uv[0][1] = mesh.uvs[2*mesh.getIndex(vOff+0)+1];
            uv[1][0] = mesh.uvs[2*mesh.getIndex(vOff+1)];
            uv[1][1] = mesh.uvs[2*mesh.getIndex(vOff+1)+1];
            uv[2][0] = mesh.uvs[2*mesh.getIndex(vOff+2)];
            uv[2][1] = mesh.uvs[2*mesh.getIndex(vOff+2)+1];
        } else {
            uv[0][0] = 0.0f; uv[0][1] = 0.0f;
            uv[1][0] = 1.0f; uv[1][1] = 0.0f;
            uv[2][0] = 1.0f; uv[2][1] = 1.0f;
        }
    }
    
    @Override
    public DifferentialGeometry getShadingGeometry(
            final DifferentialGeometry dg, final Transform o2w) {
        
        if ((mesh.n == null) && (mesh.s == null)) return dg;
        
        // Compute barycentric coordinates for point
        float b[] = new float[3];
        
        // Initialize _A_ and _C_ matrices for barycentrics
        float uv[][] = new float[3][2];
        getUVs(uv);
        
        float A[][] =
            { { uv[1][0] - uv[0][0], uv[2][0] - uv[0][0] },
              { uv[1][1] - uv[0][1], uv[2][1] - uv[0][1] } };
        float C[] = { dg.u - uv[0][0], dg.v - uv[0][1] };
        float sol[] = new float[2];
        
        if (!Utils.solveLinearSystem2x2(A, C, sol)) {
            /* handle degenerate parametric mapping */
            b[0] = b[1] = b[2] = 1.0f/3.0f;
        } else {
            b[1] = sol[0];
            b[2] = sol[1];
            b[0] = 1.0f - b[1] - b[2];
        }
        
        /* use n and s to compute shading tangents
         * for triangle, ss and ts
         */
        Normal ns;
        Vector ss, ts;
        
        if (mesh.n != null) {
            ns = o2w.apply(
              mesh.n[mesh.getIndex(vOff  )].mul(b[0]).add(
              mesh.n[mesh.getIndex(vOff+1)].mul(b[1])).add(
              mesh.n[mesh.getIndex(vOff+2)].mul(b[2]))).normalized();
        } else {
            ns = dg.nn;
        }
        
        if (mesh.s != null) {
            ss = dg.dpdu.normalized();
//                    ss = Normalize(obj2world(b[0] * mesh->s[v[0]] +
//			b[1] * mesh->s[v[1]] + b[2] * mesh->s[v[2]]));
        } else {
            ss = dg.dpdu.normalized();
        }
        
        ts = ss.cross(ns).normalized(); //Normalize(Cross(ss, ns));
        ss = ts.cross(ns); //Cross(ts, ns);

        Vector dndu, dndv;
        
        if (mesh.n != null) {
            // Compute \dndu and \dndv for triangle shading geometry
            // Compute deltas for triangle partial derivatives of normal
            float du1 = uv[0][0] - uv[2][0];
            float du2 = uv[1][0] - uv[2][0];
            float dv1 = uv[0][1] - uv[2][1];
            float dv2 = uv[1][1] - uv[2][1];

            Vector dn1 = mesh.n[mesh.getIndex(vOff  )].sub(
                  mesh.n[mesh.getIndex(vOff+2)]);

            Vector dn2 = mesh.n[mesh.getIndex(vOff+1)].sub(
                  mesh.n[mesh.getIndex(vOff+2)]);

            float determinant = du1 * dv2 - dv1 * du2;
            
            if (determinant == 0) {
                dndu = dndv = new Vector(0,0,0);
            } else {
                float invdet = 1.f / determinant;
                dndu = ( dn1.mul( dv2).sub(dn2.mul(dv1)) ).mul(invdet);
                dndv = ( dn1.mul(-du2).add(dn2.mul(du1)) ).mul(invdet);    
            }
        } else {
            dndu = dndv = new Vector(0,0,0);
        }
        
        final DifferentialGeometry dgShading = new DifferentialGeometry(
              dg.p, dg.t,
              ss, ts,
              dg.u, dg.v, dg.g);
        
        dgShading.dndu = dndu;     dgShading.dndv = dndv;
        dgShading.dudx = dg.dudx;  dgShading.dvdx = dg.dvdx; // NOBOOK
        dgShading.dudy = dg.dudy;  dgShading.dvdy = dg.dvdy; // NOBOOK
        dgShading.dpdx = dg.dpdx;  dgShading.dpdy = dg.dpdy; // NOBOOK
        
        return dgShading;
    }

    @Override
    public int getMaterialIndex() {
        if (mesh.materialIndex != null)
            return mesh.materialIndex[vOff / 3];
                  
        return super.getMaterialIndex();
    }
}
