/*
 * CheckerBoard3D.java
 *
 * Created on 4. Januar 2006, 03:01
 */

package jay.materials.textures;

import jay.scene.primitives.DifferentialGeometry;

/**
 *
 * @author Matthias Treydte <waldheinz@gmail.com>
 */
public class CheckerBoard3D<T> extends Texture<T> {
    
    Texture<T> t1;
    Texture<T> t2;
    
    /** Erstellt eine neue Instanz von CheckerBoard3D */
    public CheckerBoard3D(Texture<T> t1, Texture<T> t2) {
        this.t1 = t1;
        this.t2 = t2;
    }

    public T eval(DifferentialGeometry dg) {
        int xi = (int)dg.p.x;
        int yi = (int)dg.p.y;
        int zi = (int)dg.p.z;
        
        if ((xi + yi + zi) % 2 == 0) return t1.eval(dg);
        else return t1.eval(dg);
    }
    
}
