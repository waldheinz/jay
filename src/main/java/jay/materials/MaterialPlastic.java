/*
 * MaterialPlastic.java
 *
 * Created on 16. Juli 2007, 15:21
 */

package jay.materials;

import jay.materials.bxdfs.Blinn;
import jay.materials.bxdfs.BxDF;
import jay.materials.bxdfs.Lambertian;
import jay.materials.bxdfs.Microfacet;
import jay.materials.textures.ConstantTexture;
import jay.materials.textures.Texture;
import jay.maths.Utils;
import jay.scene.primitives.DifferentialGeometry;
import jay.utils.Spectrum;

/**
 *
 * @author Matthias Treydte <waldheinz@gmail.com>
 */
public final class MaterialPlastic extends Material {
    
    private Texture<Spectrum> kd;
    private Texture<Spectrum> ks;
    private Texture<Float> roughness;
    private static Fresnel fresnel = new FresnelDielectric(1.5f, 1.0f);
    
    /** Creates a new instance of MaterialPlastic */
    public MaterialPlastic() {
        kd = new ConstantTexture<Spectrum>(new Spectrum(0.8f));
        ks = new ConstantTexture<Spectrum>(new Spectrum(0.3f));
        
        /* very diffuse */
        roughness = new ConstantTexture<Float>(new Float(0.999f));
    }

    public void setDiffuse(final Texture<Spectrum> kd) {
        this.kd = kd;
    }
    
    public void setSpecular(final Texture<Spectrum> ks) {
        this.ks = ks;
    }
    
    public void setRoughness(final Texture<Float> roughness) {
        this.roughness = roughness;
    }
    
    public BSDF getBSDF(final DifferentialGeometry dgG,
          final DifferentialGeometry dgS) {
        
        BSDF bsdf = new BSDF(dgS, dgG.nn);
        
        /* diffuse part */
        bsdf.addBxDF(new Lambertian(kd.eval(dgS).clamp(0, 1)));
        
        /* specular part */
        Spectrum spec = ks.eval(dgS).clamp(0, 1);
        float rough = roughness.eval(dgS).floatValue();
        if (rough <= 0.0f) rough = Utils.EPSILON;
        bsdf.addBxDF(new Microfacet(spec, fresnel, new Blinn(1.0f / rough)));
        
        return bsdf;
    }
    
}
