/*
 * Texture.java
 *
 * Created on 28. Dezember 2005, 23:34
 */

package jay.materials.textures;

import jay.scene.primitives.DifferentialGeometry;

/**
 * Abstract base class for all textures.
 *
 * @author Matthias Treydte <waldheinz@gmail.com>
 */
public abstract class Texture<T> {
    
    /**
     * Evaluates the texture at the given {@link DifferentialGeometry}
     * and returns the value at that point.
     */
    public abstract T eval(final DifferentialGeometry dg);
    
}
