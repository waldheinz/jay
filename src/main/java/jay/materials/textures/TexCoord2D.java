/*
 * TexCoord2D.java
 *
 * Created on 29. Juni 2007, 11:07
 */

package jay.materials.textures;

/**
 * A 2D Texture Coordinate including its partial derivates.
 *
 * @author Matthias Treydte <waldheinz@gmail.com>
 */
public class TexCoord2D {
    
    final float s, t, dsdx, dtdx, dsdy, dtdy;
    
    /** Creates a new instance of TexCoord2D */
    public TexCoord2D(float s, float t, float dsdx, float dtdx,
            float dsdy, float dtdy) {
        
        this.s = s;
        this.t = t;
        this.dsdx = dsdx;
        this.dtdx = dtdx;
        this.dsdy = dsdy;
        this.dtdy = dtdy;
    }
    
}
