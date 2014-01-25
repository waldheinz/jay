/*
 * Normal.java
 *
 * Created on 15. Dezember 2005, 13:25
 */

package jay.maths;

/**
 * Eine Normale ist ein Vektor mit einer Länge von 1.  
 *
 * @author Matthias Treydte
 */
public final class Normal extends Vector {

    public Normal(float x, float y, float z) {
        super(x, y, z);
    }
    
    @Override
    public Normal neg() {
        return super.neg().asNormal();
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Normal)) return false;
        final Normal o = (Normal)obj;
        return ((x == o.x) && (y == o.y) && (z == o.z));
    }
}
