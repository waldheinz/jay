/*
 * GeometrySample.java
 *
 * Created on 27. Dezember 2005, 23:26
 */

package jay.scene.primitives.geometry;

import jay.maths.Normal;
import jay.maths.Point;

/**
 *
 * @author Matthias Treydte <waldheinz@gmail.com>
 */
public class GeometrySample {

    /**
     * Der zufällig gewählte Punkt auf der Oberfläche der Geometrie.
     */
    public final Point p;
    
    /**
     * Die Normale an p.
     */
    public final Normal n;
    
    public GeometrySample(Point p, Normal n) {
        this.p = p;
        this.n = n;
    }
}
