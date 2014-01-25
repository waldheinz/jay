/*
 * Mirror.java
 *
 * Created on 3. Januar 2006, 17:57
 */

package jay.materials;

import jay.materials.bxdfs.SpecularReflection;
import jay.materials.textures.*;
import jay.scene.primitives.DifferentialGeometry;
import jay.utils.Spectrum;

/**
 *
 * @author Matthias Treydze
 */
public class Mirror extends Material {

    Texture<Spectrum> kr = new ConstantTexture<Spectrum>(Spectrum.WHITE);

    public BSDF getBSDF(final DifferentialGeometry dg,
          final DifferentialGeometry dgS) {
        
        Spectrum r = kr.eval(dgS);
        BSDF bsdf = new BSDF(dgS, dg.nn);
        
        if (!r.isBlack()) {
            bsdf.addBxDF(new SpecularReflection(r, new FresnelNoOp()));
        }
        
        return bsdf;
    }

    public String getName() {
        return "Mirror";
    }
    
}
