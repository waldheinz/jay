/*
 * Stratified2D.java
 *
 * Created on 29. Dezember 2005, 15:22
 */

package jay.sampling;

import jay.maths.MersenneTwister;
import jay.maths.Utils;

/**
 * Stratified sampling in two dimensions.
 *
 * @author Matthias Treydte <waldheinz@gmail.com>
 */
public final class Stratified2D {
    
    /** # Samples in X - Richtung */
    final int nx;
    
    /** # Samples in Y - Richtung */
    final int ny;
    
    /** akt. Sample (x) */
    private int x = 0;
    
    /** akt. Sample (y) */
    private int y = 0;
    
    private final float dx;
    private final float dy;
    private final MersenneTwister random;

    /**
     * Creates a new instance of Stratified2D.
     *
     * @param nx # of samples in x - direction
     * @param ny # of samples in Y - direction
     */
    public Stratified2D(int nx, int ny) {
        this.nx = nx; this.dx = 1.0f / nx;
        this.ny = ny; this.dy = 1.0f / ny;
        this.random = Utils.getRandom();
    }
    
    /**
     * Gibt das nächste Sample zur�ck. Die zur�ckgegebenen Werte
     * liegen im Intervall 0 <= x <= 1.
     *
     * @param s Das Array, in das die erzeugten Werte eingetragen
     *      werden. Diese muss ein Array wenigstens der Größe 2 sein.
     * @return 
     */
    public boolean nextSample(float[] s) {
        if (y >= ny) return false;
        
        s[0] = (x + random.nextFloat()) * dx;
        s[1] = (y + random.nextFloat()) * dy;
        
        if (++x == nx) {
            y++;
            x = 0;
        }
        
        return true;
    }
    
    /**
     * Setzt den Sampler zur�ck. Anschlie�end kann er erneut
     * benutzt werden, um die gew�nscht Anzahl Sampels zu liefern.
     */
    public void reset() {
        x = y = 0;
    }
}
