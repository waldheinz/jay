/*
 * CheckerBoard.java
 *
 * Created on 29. Juni 2007, 11:19
 */

package jay.materials.textures;

import jay.scene.primitives.DifferentialGeometry;

/**
 *
 * @author Matthias Treydte <waldheinz@gmail.com>
 */
public class CheckerBoard<T> extends Texture<T> {
    
    final TextureMapping2D m;
    final Texture<T> t1, t2;
    
    /** Creates a new instance of CheckerBoard */
    public CheckerBoard(final TextureMapping2D m,
            final Texture<T> t1, final Texture<T> t2) {
        
        this.m = m;
        this.t1 = t1;
        this.t2 = t2;
    }

    public T eval(final DifferentialGeometry dg) {
        final TexCoord2D c = m.map(dg);
        
        final float fs = c.s - (float)Math.floor(c.s);
        final float ft = c.t - (float)Math.floor(c.t);
        final boolean upper = fs > 0.5f;
        final boolean left = ft > 0.5f;
        
        if (upper == left) return t1.eval(dg);
        else return t2.eval(dg);
    }
    
}
