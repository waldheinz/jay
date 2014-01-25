/*
 * TextureMapping2DUV.java
 *
 * Created on 29. Juni 2007, 11:11
 */

package jay.materials.textures;

import jay.scene.primitives.DifferentialGeometry;

/**
 *
 * @author Matthias Treydte <waldheinz@gmail.com>
 */
public class TextureMapping2DUV extends TextureMapping2D {
    
    final float su, sv, du, dv;
    
    /**
     * Creates a new instance of TextureMapping2DUV
     * 
     * @param su Scaling factor for u coordinate
     * @param sv Scaling factor for v coordinate
     * @param du Offset for u.
     * @param dv Offset for v.
     */
    public TextureMapping2DUV(float su, float sv, float du, float dv) {
        this.su = su;
        this.sv = sv;
        this.du = du;
        this.dv = dv;
    }
    
    public TextureMapping2DUV() {
        this(1, 1, 0, 0);
    }
    
    public TexCoord2D map(final DifferentialGeometry dg) {
        /* FIXME: Compute partial derivates */
        return new TexCoord2D(
                su * dg.u + du,
                sv * dg.v + dv,
                0, 0, 0, 0);
    }
    
}
