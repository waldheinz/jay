/*
 * Glass.java
 *
 * Created on 4. Januar 2006, 02:25
 */

package jay.materials;

import jay.materials.bxdfs.SpecularReflection;
import jay.materials.bxdfs.SpecularTransmission;
import jay.materials.textures.*;
import jay.scene.primitives.DifferentialGeometry;
import jay.utils.Spectrum;

/**
 *
 * @author Matthias Treydze
 */
public class Glass extends Material {
    
    Texture<Spectrum> kr;
    Texture<Spectrum> kt;
    Texture<Float> index;
    
    /** Erstellt eine neue Instanz von Glass */
    public Glass() {
        kr = new ConstantTexture<Spectrum>(new Spectrum(1.0f));
        kt = new ConstantTexture<Spectrum>(new Spectrum(1.0f));
        index = new ConstantTexture<Float>(1.5f);
    }
    
    public BSDF getBSDF(final DifferentialGeometry dg,
          final DifferentialGeometry dgS) {
        
        BSDF bsdf = new BSDF(dgS, dg.nn);
        Spectrum r = kr.eval(dg);
        Spectrum t = kt.eval(dg);
        float idx = index.eval(dg);
        
        if (!r.isBlack())
            bsdf.addBxDF(new SpecularReflection(r,
                    new FresnelDielectric(1.0f, idx)));
        
        if (!t.isBlack())
            bsdf.addBxDF(new SpecularTransmission(
                    t, 1.0f, idx));
        
        return bsdf;
    }
    
}
