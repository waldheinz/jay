/*
 * PhotonProcess.java
 *
 * Created on 2. März 2006, 15:43
 */

package jay.integrators.photonmap;

import jay.maths.Point;
import jay.utils.Heap;
import jay.utils.kdtree.AbstractNodeData;
import jay.utils.kdtree.KdTree;

/**
 *
 * @author Matthias Treydte
 */
public class PhotonProcess extends jay.utils.kdtree.Visitor<Photon> {
    
    final Point p;
    final int nLookup;
    int foundPhotons;
    ClosePhoton[] photons;
    
    /** Creates a new instance of PhotonProcess */
    public PhotonProcess(int mp, Point p) {
        this.p = p;
        this.nLookup = mp;
        this.foundPhotons = 0;
    }
    
    public float visit(Photon photon, float distSquared) {
        if (foundPhotons < nLookup) {
            // Add photon to unordered array of photons
            photons[foundPhotons++] = new ClosePhoton(photon, distSquared);
            if (foundPhotons == nLookup) {
                Heap.make(photons, 0, nLookup-1);
                distSquared = photons[0].distanceSquared;
            }
        } else {
            // Remove most distant photon from heap and add new photon
            Heap.pop(photons, 0, nLookup-1);
            photons[nLookup-1] = new ClosePhoton(photon, distSquared);
            Heap.push(photons, 0, nLookup-1);
            distSquared = photons[0].distanceSquared;
        }
        
        return distSquared;
    }

}
