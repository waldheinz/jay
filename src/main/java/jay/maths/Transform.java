/*
 * Transform.java
 *
 * Created on 15. Dezember 2005, 13:24
 */

package jay.maths;

import static java.lang.Math.*;

/**
 * @author Matthias Treydte <waldheinz@gmail.com>
 */
public final class Transform {
    
    protected final Matrix trans, inv_trans;
    
    public final static Transform IDENTITY = 
            new Transform(Matrix.IDENTITY, Matrix.IDENTITY);
    
    public Transform() {
        trans = inv_trans = Matrix.IDENTITY;
    }
    
    public Transform(final Matrix m) {
        trans = m; inv_trans = m.inverted();
    }
    
    public Transform(final Matrix m, final Matrix m_inv) {
        trans = m;
        inv_trans = m_inv;
    }
    
    /**
     * Transformation umkehren
     */
    public Transform getInverse() { return new Transform(inv_trans, trans); }
    
    /**
     * Verschiebung
     */
    public static Transform translate(final Vector delta) {
        Matrix m = new Matrix(
              1, 0, 0, delta.x,
              0, 1, 0, delta.y,
              0, 0, 1, delta.z,
              0, 0, 0, 1);
        
        Matrix m_inv = new Matrix(
              1, 0, 0, -delta.x,
              0, 1, 0, -delta.y,
              0, 0, 1, -delta.z,
              0, 0, 0, 1);
        
        return new Transform(m, m_inv);
    }
    
    /**
     * Skalierung
     */
    public static Transform scale(float sx, float sy, float sz) {
        Matrix m = new Matrix(
              sx,  0,  0, 0,
              0 , sy,  0, 0,
              0 ,  0, sz, 0,
              0 ,  0,  0, 1);
        
        Matrix m_inv = new Matrix(
              1.0f/sx, 0.0f, 0.0f, 0.0f,
              0.0f, 1.0f/sy, 0.0f, 0.0f,
              0.0f, 0.0f, 1.0f/sz, 0.0f,
              0.0f, 0.0f, 0.0f, 1.0f);
        
        return new Transform(m, m_inv);
    }
    
    /**
     * Rotation um X
     */
    public static Transform rotateX(float angle) {
        final float sin_t = (float)sin(angle * PI / 180.0f);
        final float cos_t = (float)cos(angle * PI / 180.0f);
        
        Matrix m = new Matrix(
              1,     0,      0, 0,
              0, cos_t, -sin_t, 0,
              0, sin_t,  cos_t, 0,
              0,     0,      0, 1);
        
        return new Transform(m, m.transposed());
    }
    
    public static Transform rotateY(float angle) {
        final float sin_t = (float)sin(angle * PI / 180.0f);
        final float cos_t = (float)cos(angle * PI / 180.0f);
        
        final Matrix m = new Matrix(
              cos_t ,     0,  sin_t, 0,
              0     ,     1,      0, 0,
              -sin_t,     0,  cos_t, 0,
              0     ,     0,      0, 1);
        
        return new Transform(m, m.transposed());
    }
    
    public static Transform rotateZ(float angle) {
        final float sin_t = (float)sin(angle * PI / 180.0f);
        final float cos_t = (float)cos(angle * PI / 180.0f);
        
        Matrix m = new Matrix(
              cos_t, -sin_t, 0, 0,
              sin_t,  cos_t, 0, 0,
              0    ,      0, 1, 0,
              0    ,      0, 0, 1);
        
        return new Transform(m, m.transposed());
    }
    
    public Matrix getMatrix() {
        return trans;
    }
    
    public Matrix getInverseMatrix() {
        return inv_trans;
    }
    
    /**
     * Rotation um beliebige Achse
     */
    public static Transform rotate(float angle, final Vector axis) {
        final Normal a = axis.normalized();
        
        final float s = (float)sin(angle * PI / 180.0f);
        final float c = (float)cos(angle * PI / 180.0f);
        float m[][] = new float[4][4];
        
        m[0][0] = a.x * a.x + (1.0f - a.x * a.x) * c;
        m[0][1] = a.x * a.y * (1.0f - c) - a.z * s;
        m[0][2] = a.x * a.z * (1.0f - c) + a.y * s;
        m[0][3] = 0.0f;
        m[1][0] = a.x * a.y * (1.0f - c) + a.z * s;
        m[1][1] = a.y * a.y + (1.0f - a.y * a.y) * c;
        m[1][2] = a.y * a.z * (1.0f - c) - a.x * s;
        m[1][3] = 0.0f;
        m[2][0] = a.x * a.z * (1.0f - c) - a.y * s;
        m[2][1] = a.y * a.z * (1.0f - c) + a.x * s;
        m[2][2] = a.z * a.z + (1.0f - a.z * a.z) * c;
        m[2][3] = 0.0f;
        m[3][0] = 0.0f;
        m[3][1] = 0.0f;
        m[3][2] = 0.0f;
        m[3][3] = 1.0f;
        
        final Matrix mat = new Matrix(m);
        
        return new Transform(mat, mat.transposed());
    }
    
    /**
     * perspektivische Transformation
     *
     * @param fov Öffnungwinkel in Grad
     * @param n nähester Punkt
     * @param f fernster Punkt
     * @return Perspektivische Transformation
     */
    public static Transform perspective(float fov, float near, float far) {
        float inv_denom = 1.0f / (far - near);
        
        Matrix persp = new Matrix(
                1, 0,       0,          0,
                0, 1,       0,          0,
                0, 0, far*inv_denom, -far*near*inv_denom,
                0, 0,       1,          0);
        
        /* Scale to canonical viewing volume */
        float invTanAng = 1.0f / (float)tan(toRadians(fov) / 2.0f);
        return scale(invTanAng, invTanAng, 1).compose(
                new Transform(persp));
    }
    
    public static Transform orthographic(float near, float far) {
         return scale(1.0f, 1.0f, 1.0f / (far-near)).compose(
                translate(new Vector(0.0f, 0.0f, -near)));
    }
    
    /**
     * eine lookAt - Transformation
     */
    public static Transform lookAt(Point pos, Point look, final Vector up) {

        float m[][] = new float[4][4];
        
        m[0][3] = pos.x;
        m[1][3] = pos.y;
        m[2][3] = pos.z;
        m[3][3] = 1.0f;
        
        final Vector dir = look.sub(pos).normalized();
        final Vector right = dir.cross(up).normalized();
        final Vector newUp = right.cross(dir);
        
        m[0][0] = right.x;
        m[1][0] = right.y;
        m[2][0] = right.z;
        m[3][0] = 0.0f;
        m[0][1] = newUp.x;
        m[1][1] = newUp.y;
        m[2][1] = newUp.z;
        m[3][1] = 0.0f;
        m[0][2] = dir.x;
        m[1][2] = dir.y;
        m[2][2] = dir.z;
        m[3][2] = 0.0f;
        
        Matrix camToWorld = new Matrix(m);
        return new Transform(camToWorld, camToWorld.inverted());
    }
    
    /**
     * Punkte transformieren
     */
    public Point apply(final Point p) {
        final float x = p.x, y = p.y, z = p.z;
        
        float xp = trans.n[0][0]*x + trans.n[0][1]*y +
              trans.n[0][2]*z + trans.n[0][3];
        
        float yp = trans.n[1][0]*x + trans.n[1][1]*y +
              trans.n[1][2]*z + trans.n[1][3];
        
        float zp = trans.n[2][0]*x + trans.n[2][1]*y +
              trans.n[2][2]*z + trans.n[2][3];
        
        float wp = trans.n[3][0]*x + trans.n[3][1]*y +
              trans.n[3][2]*z + trans.n[3][3];
        
        if (wp == 1.0f) return new Point(xp, yp, zp);
        else return new Point(xp / wp, yp / wp, zp / wp);
    }
    
    /**
     * Vektoren transformieren
     */
    public Vector apply(final Vector v) {
        final float x = v.x, y = v.y, z = v.z;
        final float xp = trans.n[0][0]*x + trans.n[0][1]*y + trans.n[0][2]*z;
        final float yp = trans.n[1][0]*x + trans.n[1][1]*y + trans.n[1][2]*z;
        final float zp = trans.n[2][0]*x + trans.n[2][1]*y + trans.n[2][2]*z;
        return new Vector(xp, yp, zp);
    }
    
    /**
     * Normalen transformieren
     */
    public Normal apply(final Normal n) {
        float x = n.x, y = n.y, z = n.z;
        return new Normal(
              inv_trans.n[0][0]*x + inv_trans.n[1][0]*y + inv_trans.n[2][0]*z,
              inv_trans.n[0][1]*x + inv_trans.n[1][1]*y + inv_trans.n[2][1]*z,
              inv_trans.n[0][2]*x + inv_trans.n[1][2]*y + inv_trans.n[2][2]*z
              );
    }
    
    /**
     * Strahlen transformieren
     */
    public Ray apply(final Ray r) {
        return new Ray(apply(r.o), apply(r.d), r.tmin, r.tmax);
    }
    
    /**
     * Eine {@link AABB} transformieren
     */
    public AABB apply(final AABB box) {
        final Point min = box.min;
        final Point max = box.max;
        
        AABB ret = new AABB(apply(min), apply(max));
        ret = ret.extend(apply(new Point(min.x, min.y, max.z)));
        ret = ret.extend(apply(new Point(min.x, max.y, min.z)));
        ret = ret.extend(apply(new Point(min.x, max.y, max.z)));
        ret = ret.extend(apply(new Point(max.x, min.y, min.z)));
        ret = ret.extend(apply(new Point(max.x, min.y, max.z)));
        ret = ret.extend(apply(new Point(max.x, max.y, min.z)));
        return ret;
    }
    
    public Transform compose(final Transform t2) {
        final Matrix m1 = Matrix.mul(this.trans, t2.trans);
        final Matrix m2 = Matrix.mul(t2.inv_trans, this.inv_trans);
        return new Transform(m1, m2);
    }
    
    /**
     * Tells if this transfomation swaps the handedness of
     * the coordinate system. A transformation does so,
     * if the determinant of the upper left 3x3 sub-matrix
     * of its matrix representation has a determinant less
     * than zero.
     *
     * @return If this transformation swaps the handedness.
     */
    public boolean swapsHandedness() {
        final float[][] m = trans.n;
        
        float det = (
            (m[0][0] * (m[1][1] * m[2][2] - m[1][2] * m[2][1])) -
            (m[0][1] * (m[1][0] * m[2][2] - m[1][2] * m[2][0])) +
            (m[0][2] * (m[1][0] * m[2][1] - m[1][1] * m[2][0])));
        
        return (det < 0.0f);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Transform [trans=");
        sb.append(trans);
        sb.append(",\n   inv=");
        sb.append(inv_trans);
        sb.append("]");
        return sb.toString();
    }
    
}
