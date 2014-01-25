/*
 * ToneMap.java
 *
 * Created on 24. Februar 2006, 16:49
 */

package jay.sampling.reconstruction;

/**
 *
 * @author trem
 */
public abstract class ToneMap {
    
    /**
     * Führt die Tonemapping - Operation durch.
     *
     * @param y Helligkeitswerte im Bild
     * @param scale Ausgabe: hier wird die vorgeschlagene
     *  Skalierung eingetragen
     * @param xRes X - Auflösung des Bildes
     * @param yRes Y - Auflösung des Bildes
     * @param maxY Helligkeit des Ausgabegerätes bei voller Ansteuerung
     *      (ca. 150 für Computermonitore)
     */
    public abstract void map(
            float[] y, float[] scale, 
            int xRes, int yRes, float maxY);
    
}
