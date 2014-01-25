/*
 * Photon.java
 *
 * Created on 1. März 2006, 18:34
 */

package jay.integrators.photonmap;

import jay.maths.*;
import jay.utils.Spectrum;
import jay.utils.kdtree.AbstractNodeData;

/**
 *
 * @author Matthias Treydte
 */
public class Photon extends jay.utils.kdtree.AbstractNodeData {
    
    public Vector wi;
    public Spectrum alpha;
    
    /** Creates a new instance of Photon */
    public Photon(Point p, Spectrum a, Vector wi) {
        this.p = p;
        this.wi = wi;
        this.alpha = a;
    }
    
    public Photon() { }

    public int compareTo(AbstractNodeData o) {
        final Photon p2 = (Photon)o;
        return (int)Math.signum(alpha.y() - p2.alpha.y());
    }
    
}
