/*
 * Filter.java
 *
 * Created on 29. Dezember 2005, 16:45
 */

package jay.sampling;

/**
 *
 * @author Matthias Treydze
 */
public abstract class Filter {
    
    final float xWidth;
    final float yWidth;
    final float invXwidth;
    final float invYwidth;
    
    /** Erstellt eine neue Instanz von Filter */
    public Filter(float xw, float yw) {
        xWidth = xw; invXwidth = 1.0f / xw;
        yWidth = yw; invYwidth = 1.0f / yw;
    }
    
    public abstract float eval(float x, float y);
    
}
