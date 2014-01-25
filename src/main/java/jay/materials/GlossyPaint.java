/*
 * GlossyPaint.java
 *
 * Created on 31. Dezember 2005, 14:48
 */

package jay.materials;

import jay.materials.bxdfs.*;
import jay.materials.textures.*;
import jay.scene.primitives.DifferentialGeometry;
import jay.utils.Spectrum;

/**
 *
 * @author Matthias Treydte <waldheinz@gmail.com>
 */
public class GlossyPaint extends Material {
    
    /** Diffuse reflectivity */
    private Texture<Spectrum> kd;
    
    /** Specular reflectivity */
    private Texture<Spectrum> ks;
    
    /** Roughness in u direction */
    private Texture<Float> nu;
    
    /** Roughness in v direction */
    private Texture<Float> nv;
    
    /** Erstellt eine neue Instanz von GlossyPaint */
    public GlossyPaint() {
        kd = new ConstantTexture<Spectrum>(new Spectrum(0.3f));
        ks = new ConstantTexture<Spectrum>(new Spectrum(0.4f));
        nu = new ConstantTexture<Float>(0.1f);
        nv = new ConstantTexture<Float>(0.1f);
    }

    public GlossyPaint(final Texture<Spectrum> kd, final Texture<Spectrum> ks,
            final Texture<Float> nu, final Texture<Float> nv) {
        
        this.kd = kd;
        this.ks = ks;
        this.nu = nu;
        this.nv = nv;
    }
    
    public void setDiffuse(Texture<Spectrum> kd) {
        this.kd = kd;
    }
    
    public void setSpecular(Texture<Spectrum> ks) {
        this.ks = ks;
    }
    
    public void setRoughU(Texture<Float> nu) {
        this.nu = nu;
    }
    
    public void setRoughV(Texture<Float> nv) {
        this.nv = nv;
    }
    
    @Override
    public BSDF getBSDF(final DifferentialGeometry dgG,
          final DifferentialGeometry dgS) {
        
        BSDF bsdf = new BSDF(dgS, dgG.nn);
        
        final Spectrum d = kd.eval(dgS);
        final Spectrum s = ks.eval(dgS);
        float u = nu.eval(dgS);
        float v = nv.eval(dgS);
        
        bsdf.addBxDF(new FresnelBlend(d, s, new Anisotropic(1.0f / u, 1.0f / v)));

        return bsdf;
    }
    
}
