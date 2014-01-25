/*
 * ClosePhoton.java
 *
 * Created on 2. März 2006, 15:46
 */

package jay.integrators.photonmap;

/**
 *
 * @author Matthias Treydte
 */
public class ClosePhoton implements Comparable {
    
    final Photon photon;
    float distanceSquared;
    
    public ClosePhoton() {
        this(null, Float.POSITIVE_INFINITY);
    }
    
    /** Creates a new instance of ClosePhoton */
    public ClosePhoton(Photon photon, float md2) {
        this.photon = photon;
        this.distanceSquared = md2;
    }

    public int compareTo(Object o) {
        final ClosePhoton p2 = (ClosePhoton)o;
        if (distanceSquared == p2.distanceSquared) {
            return photon.compareTo(p2.photon);
        } else {
            return (int)Math.signum(distanceSquared - p2.distanceSquared);
        }
    }
 
}
