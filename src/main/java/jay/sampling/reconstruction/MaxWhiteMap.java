/*
 * MaxWhiteMap.java
 *
 * Created on 24. Februar 2006, 16:54
 */

package jay.sampling.reconstruction;

/**
 * Einfache Variante des Tonemappings: Die hellste vorkommende
 * Farbe im Ausgangsbild wird auf Weiss abgebildet.
 *
 * @author Matthias Treydte
 */
public class MaxWhiteMap extends ToneMap {
    
    public void map(float[] y, float[] scale, int xRes, int yRes, float maxY) {
        /* maximale Helligkeit bestimmen */
        float mY = 0.0f;
        for (int i=0; i < xRes * yRes; ++i)
            mY = Math.max(mY, y[i]);
        
        float s;
        if (mY > 0.0f) {
            s = maxY / mY;
            System.out.println("MaxWhiteMap: max: " + mY);
        } else {
            /* eh alles Schwarz */
            s = 1.0f;
        }
        
        for (int i = 0; i < xRes * yRes; ++i)
            scale[i] = s;
    }
    
}
