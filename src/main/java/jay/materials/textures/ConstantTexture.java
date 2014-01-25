/*
 * ConstrantTexture.java
 *
 * Created on 28. Dezember 2005, 23:36
 */

package jay.materials.textures;

import jay.scene.primitives.DifferentialGeometry;

/**
 * Eine Textur, die immer den gleichen Wert zurückgibt.
 *
 * @author Matthias Treydte <waldheinz@gmail.com>
 */
public class ConstantTexture<T> extends Texture<T> {
    
    final T value;
    
    /** Creates a new instance of ConstrantTexture */
    public ConstantTexture(final T value) {
        this.value = value;
    }
    
    public T eval(final DifferentialGeometry dg) {
        return value;
    }
    
}
