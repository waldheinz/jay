/*
 * ConstantUVMap.java
 *
 * Created on 4. Januar 2006, 01:54
 */

package jay.materials.textures;


/**
 *
 * @author Matthias Treydze
 */
public class ConstantUVMap<T> extends UVMap<T> {
    
    final T value;
    
    /** Erstellt eine neue Instanz von ConstantUVMap */
    public ConstantUVMap(T value) {
        this.value = value;
    }

    public T eval(float u, float v) {
        return value;
    }
    
}
