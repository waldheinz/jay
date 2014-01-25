/*
 * PhongMaterial.java
 *
 * Created on 4. Januar 2006, 14:26
 */

package jay.materials;

import jay.materials.bxdfs.Phong;
import jay.materials.textures.ConstantTexture;
import jay.materials.textures.Texture;
import jay.scene.primitives.DifferentialGeometry;
import jay.utils.Spectrum;

/**
 *
 * @author Matthias Treydze
 */
public class PhongMaterial extends Material {
    
    Texture<Spectrum> kd;
    Texture<Spectrum> ks;
    Texture<Float> exp;
    
    /** Erstellt eine neue Instanz von PhongMaterial */
    public PhongMaterial() {
        this(new Spectrum(0.9f), new Spectrum(0.0f), 0.0f);
    }

    public PhongMaterial(Spectrum d, Spectrum s, float e) {
        kd = new ConstantTexture<Spectrum>(d);
        ks = new ConstantTexture<Spectrum>(s);
        exp = new ConstantTexture<Float>(e);
    }
    
    public PhongMaterial(Texture<Spectrum> d, Texture<Spectrum> s,
            Texture<Float> e) {
        
        kd = d;
        ks = s;
        exp = e;
    }
    
    public void setDiffuse(Texture<Spectrum> d) {
        this.kd = d;
    }
    
    public void setSpecular(Texture<Spectrum> s) {
        this.ks = s;
    }
    
    public void setExponent(Texture<Float> e) {
        this.exp = e;
    }
    
    @Override
    public BSDF getBSDF(final DifferentialGeometry dg,
          final DifferentialGeometry dgS) {
        
        BSDF bsdf = new BSDF(dgS, dg.nn);
        
        Spectrum d = kd.eval(dgS);
        Spectrum s = ks.eval(dgS);
        float e = exp.eval(dgS); 
        
        bsdf.addBxDF(new Phong(d, s, e));
        
        return bsdf;
    }
    
}
