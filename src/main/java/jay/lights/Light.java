/*
 * Light.java
 *
 * Created on 15. Dezember 2005, 17:46
 */

package jay.lights;

import jay.maths.*;
import jay.scene.*;
import jay.utils.*;

/**
 * @author Matthias Treydte <waldheinz@gmail.com>
 */
public abstract class Light {

    /**
     * Transformiert Welt- in lokale Koordinaten
     */
    protected Transform w2l = Transform.IDENTITY;
    
    /**
     * Transformiert lokale in Weltkoorinaten
     */
    protected Transform l2w = Transform.IDENTITY;
    
    /**
     * Gibt an, ob es sich bei dieser Lichtquelle um eine
     * delta - Lichquelle handelt. Es macht keinen Sinn,
     * eine solche zu sampeln, da sie Punktf�rmig ist.
     *
     * @return ob dieses Licht eine Delta - Lichquelle ist
     */
    public boolean isDeltaLight() { return false; }
    
    /**
     * Gesamtleuchtkraft dieser Lichtquelle
     *
     * @return Leuchtst�rke dieser Lichtquelle
     */
    public abstract Spectrum power(final Scene scene);
    
    /**
     * Gibt das an p einfallende Licht aufgrund dieser Lichtquelle zur�ck.
     * <p>
     * Die �bergebene Normale kann dazu genutzt werden, nur Teile der
     * Lichtquelle zu sampeln welche auch tats�chlich von p aus gesehen werden
     * k�nnen und somit die Varianz zu verringern.
     *
     * @param p Position des Betrachters
     * @param n Normale der Oberfläche an p, kann <code>null</code> sein
     *      wenn unbestimmt
     * @param u Zufallszahl
     * @param v Zufallszahl
     * @return das LightSample
     */
    public abstract LightSample sample(final Point p, final Normal n, float u, float v);
    
    public LightRaySample sampleRay(final Scene scene,
            float u1, float u2, float u3, float u4) {
        
        throw new UnsupportedOperationException(
                "unimplemented Light.sampleRay called");
    }
    
    public float pdf(final Point p, final Normal n, final Vector wi) {
        return pdf(p, wi);
    }
    
    public float getArea(final Scene scene) { return 0.0f; } 
    
    public abstract float pdf(final Point p, final Vector wi);
    
    public Spectrum direct(final Ray ray) {
        return Spectrum.BLACK;
    }
    
    public void setTransform(final Transform t) {
        l2w = t;
        w2l = t.getInverse();
    }
    
}
