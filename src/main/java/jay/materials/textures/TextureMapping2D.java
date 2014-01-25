/*
 * TextureMapping2D.java
 *
 * Created on 29. Juni 2007, 11:10
 */

package jay.materials.textures;

import jay.scene.primitives.DifferentialGeometry;

/**
 *
 * @author Matthias Treydte <waldheinz@gmail.com>
 */
public abstract class TextureMapping2D {
    
    public abstract TexCoord2D map(final DifferentialGeometry dg);
    
}
