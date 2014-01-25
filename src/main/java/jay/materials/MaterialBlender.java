/*
 * MaterialBlender.java
 *
 * Created on 15. Juli 2007, 18:56
 */

package jay.materials;

import jay.scene.primitives.DifferentialGeometry;
import jay.utils.Spectrum;

/**
 * Allows to use Blender material descriptions to some degree.
 * 
 * @author Matthias Treydte <waldheinz@gmail.com>
 */
public class MaterialBlender extends Material {
    
    private Spectrum diffuse;
    
    /** Creates a new instance of MaterialBlender */
    public MaterialBlender() {
    }

    public void setDiffuse(final Spectrum diffuse) {
        this.diffuse = diffuse;
    }
    
    public BSDF getBSDF(final DifferentialGeometry dgG,
          final DifferentialGeometry dgS) {
          
        return null;
    }
    
}
