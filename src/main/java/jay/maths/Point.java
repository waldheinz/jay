/*
 * Point.java
 *
 * Created on 15. Dezember 2005, 13:26
 */

package jay.maths;


/**
 * @author Matthias Treydte <waldheinz at gmail.com>
 */
public final class Point implements Comparable<Point> {
    
    public final float x, y, z;
    
    public Point() {
        x = 0.0f; y = 0.0f; z = 0.0f;
    }
    
    public Point(float x, float y, float z) {
        this.x = x; this.y = y; this.z = z;
    }
    
    public Point add(final Vector v) {
        return new Point(x + v.x, y + v.y, z + v.z);
    }
    
    public Point add(final Point p) {
        return new Point(x + p.x, y + p.y, z + p.z);
    }
    
    public Vector sub(final Point p) {
        return new Vector(x - p.x, y - p.y, z - p.z);
    }
    
    public Point sub(final Vector v) {
        return new Point(x - v.x, y - v.y, z - v.z);
    }
    
    
    public Point mul(final Vector v) {
        return new Point(x * v.x, y * v.y, z * v.z);
    }
    
    public Point mul(float f) {
        return new Point(x * f, y * f, z * f);
    }
    
    /**
     * Returns the {@link Vector} from {@literal (0, 0, 0)} to this point.
     * 
     * @return the {@link Vector} from {@literal (0, 0, 0)} to this point.
     */
    public Vector vectorTo() {
        return new Vector(x, y, z);
    }
    
    static float distanceSquared(final Point p1, final Point p2) {
        return p2.sub(p1).lengthSquared();
    }
    
    static float distance(final Point p1, final Point p2) {
        return p2.sub(p1).length();
    }
    
    /**
     * Gibt ein Element dieses Punktes zurück.
     * 
     * @param i Index des Elements (0 = x, 1 = y, sonst z)
     * @return das Element
     */
    public float get(int i) {
        switch(i) {
            case 0: return x;
            case 1: return y;
            case 2: return z;
            default:
                throw new IllegalArgumentException("axis out of range");
        }
    }
    
    public Point set(float val, int axis) {
        switch(axis) {
            case 0: return new Point(val, y, z);
            case 1: return new Point(x, val, z);
            case 2: return new Point(x, y, val);
            default:
                throw new IllegalArgumentException("axis out of range");
        }
    }
    
    @Override
    public String toString() {
        return "[" + x + ", " + y + ", " + z + "]";
    }

    /**
     * Checks component-wise equality if the given object is a
     * {@literal Point}, too. Returns {@literal false} otherwise.
     *
     * @param obj the object to check equality with.
     * @return 
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Point)) return false;
        final Point p = (Point)obj;
        return ((p.x == x) && (p.y == y) && (p.z == z));
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + Float.floatToIntBits(this.x);
        hash = 41 * hash + Float.floatToIntBits(this.y);
        hash = 41 * hash + Float.floatToIntBits(this.z);
        return hash;
    }
    
    /**
     * Compares this point to another point. The components are checked
     * in x first, then y, then z order.
     *
     * @param other the point to compare this point with.
     * @return {@inheritDoc}
     */
    public int compareTo(Point other) {
        double diff = x - other.x;
        
		if (diff == 0) {
			diff = y - other.y;
			if (diff == 0)
				diff = z - other.z;
		}
        
		return (diff < 0) ? -1 : ( (diff > 0) ? 1 : 0);
    }
}
