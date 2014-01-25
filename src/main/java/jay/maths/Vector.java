/*
 * Vector.java
 *
 * Created on 15. Dezember 2005, 13:24
 */

package jay.maths;

import static java.lang.Math.*;

/**
 * @author Matthias Treydte
 */
public class Vector implements Comparable<Vector> {
    
    public final float x;
    public final float y;
    public final float z;
    
    public Vector() {
        x = 0; y = 0; z = 0;
    }
    
    public Vector(float x, float y, float z) {
        this.x = x; this.y = y; this.z = z;
    }
    
    public Vector(final Vector o) {
        x = o.x; y = o.y; z = o.z;
    }
    
    /**
     * Converts a normal into a vector.
     * 
     * @param n the normal to use as a vector.
     */
    public Vector (final Normal n) {
        this.x = n.x;
        this.y = n.y;
        this.z = n.z;
    }

    /**
     * Gibt diesen Vektor als Normale zurück. Dabei wird jedoch nicht
     * sichergestellt, dass seine Länge tatsächlich 1 beträgt. Dies sollte
     * der Aufrufer sicherstellen.
     * <p>
     * Nützlich, wenn der Vektor z.B. als Kreuzprodukt zweier Normalen
     * entstanden ist und somit implizit auch wieder normalisiert ist.
     *
     * @return eine Normale, initialisiert mit den Werten dieses Vektors
     */
    public Normal asNormal() {
        return new Normal(x, y, z);
    }

    /**
     * Returns the length of this vector.
     *
     * @return the length of this vector.
     */
    public float length() {
        return (float)sqrt(lengthSquared());
    }

    /**
     * Returns l^2, where l is the {@link #length()} of this vector.
     *
     * @return the squared length of this vector.
     */
    public float lengthSquared() {
        return x*x + y*y + z*z;
    }
    
    public Normal normalized() {
        final float l1 = 1.0f / length();
        return new Normal(x * l1, y * l1, z * l1);
    }
    
    public Vector cross(final Vector b) {
        return new Vector(
                y * b.z - z * b.y,
                z * b.x - x * b.z,
                x * b.y - y * b.x);
    }
    
    public int dominantAxis() {
        if (abs(x) > abs(y))	{
            if (abs(x) > abs(z)) return 0;
            else return 2;
        } else {
            if (abs(y) > abs(z)) return 1;
            else return 2;
        }
    }
    
    public Vector add(final Vector  a_V) {
        return new Vector(x + a_V.x, y + a_V.y, z + a_V.z);
    }
    
    public Vector sub(final Vector  a_V) {
        return new Vector(x - a_V.x, y - a_V.y, z - a_V.z);
    }
    
    public Vector mul(final float f) {
        return new Vector(x * f, y * f, z * f);
    }
    
    public Vector mul(final Vector  a_V) {
        return new Vector(x * a_V.x, y * a_V.y, z * a_V.z);
    }
    
    public Vector neg() {
        return new Vector(-x, -y, -z);
    }

    /**
     * Returns the component wise inverse of this vector. If this vector has
     * zero components, the resulting vector will have the corresponding
     * components set to {@link Float.NaN}.
     *
     * @return the inverse of this vector.
     */
    public Vector inv() {
        return new Vector(1.0f / x, 1.0f / y, 1.0f / z);
    }

    /**
     * Gibt ein Element dieses Vektors zurück.
     *
     * @param i Index des Elements (0 = x, 1 = y, sonst z)
     * @return das Element
     */
    public float get(int i) {
        switch(i) {
            case 0  : return x;
            case 1  : return y;
            default : return z;
        }
    }
    
    public float dot(final Normal n) {
        return x * n.x + y * n.y + z * n.z;
    }
    
    public float dot(final Point p) {
        return x * p.x + y * p.y + z * p.z;
    }
    
    public float dot(final Vector v) {
        return x * v.x + y * v.y + z * v.z;
    }
    
    @Override
    public String toString() {
        return "[" + x + ", " + y + ", " + z + "]";
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Vector)) return false;
        Vector v = (Vector)obj;
        return ((v.x == x) && (v.y == y) && (v.z == z));
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + Float.floatToIntBits(this.x);
        hash = 17 * hash + Float.floatToIntBits(this.y);
        hash = 17 * hash + Float.floatToIntBits(this.z);
        return hash;
    }

    /**
     * Compares this vector to another vector. The components are checked
     * in x first, then y, then z order.
     *
     * @param other the vector to compare this vector with.
     * @return {@inheritDoc}
     */
    public int compareTo(Vector other) {
                double diff = x - other.x;

		if (diff == 0) {
			diff = y - other.y;
			if (diff == 0)
				diff = z - other.z;
		}

		return (diff < 0) ? -1 : ( (diff > 0) ? 1 : 0);
    }
}
