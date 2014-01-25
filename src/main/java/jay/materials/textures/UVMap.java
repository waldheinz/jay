/*
 * UVMap.java
 *
 * Created on 4. Januar 2006, 01:53
 */

package jay.materials.textures;

/**
 *
 * @author Matthias Treydze
 */
public abstract class UVMap<T> {
        public abstract T eval(float u, float v);
}
