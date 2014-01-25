/*
 * Material.java
 *
 * Created on 15. Dezember 2005, 15:48
 */

package jay.materials;

import jay.scene.primitives.DifferentialGeometry;

/**
 * @author Matthias Treydte
 */
public abstract class Material {
    
    public final static Material DEFAULT = new MaterialPlastic();    

    public abstract BSDF getBSDF(final DifferentialGeometry dgG,
          final DifferentialGeometry dgS);
}
