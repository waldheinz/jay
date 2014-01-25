/*
 * BrushedMetal.java
 *
 * Created on 2. Januar 2006, 19:10
 */

package jay.materials;

import java.util.ArrayList;
import jay.materials.bxdfs.BxDF;
import jay.materials.bxdfs.Lafortune;
import jay.materials.bxdfs.Lafortune.LobeParams;
import jay.scene.primitives.DifferentialGeometry;
import jay.utils.Spectrum;

/**
 *
 * @author Matthias Treydze
 */
public class BrushedMetal extends Material {
    
    private final static ArrayList<LobeParams> lobes =
            new ArrayList<LobeParams>();
    
    private final static Spectrum diffuse = new Spectrum( new float[] {
        0.0f,    0.0f,   0.0f
    });

    static {
        float xy0[] = {  -1.11854f, -1.11845f, -1.11999f  };
        float z0[]  = {   1.01272f,  1.01469f,  1.01942f  };
        float e0[]  = {  15.8708f,  15.6489f,  15.4571f   };
        float xy1[] = {  -1.05334f, -1.06409f, -1.08378f  };
        float z1[]  = {   0.69541f,  0.662178f, 0.626672f };
        float e1[]  = { 111.267f,   88.9222f,  65.2179f   };
        float xy2[] = {  -1.01684f, -1.01635f, -1.01529f  };
        float z2[]  = {   1.00132f,  1.00112f,  1.00108f  };
        float e2[]  = { 180.181f,  184.152f,  195.773f    };
        
        LobeParams p0 = new LobeParams();
        p0.x = new Spectrum(xy0);
        p0.y = p0.x;
        p0.z = new Spectrum(z0);
        p0.e = new Spectrum(e0);
        
        LobeParams p1 = new LobeParams();
        p1.x = new Spectrum(xy1);
        p1.y = p1.x;
        p1.z = new Spectrum(z1);
        p1.e = new Spectrum(e1);
        
        LobeParams p2 = new LobeParams();
        p2.x = new Spectrum(xy2);
        p2.y = p2.x;
        p2.z = new Spectrum(z2);
        p2.e = new Spectrum(e2);
        
        lobes.add(p0); lobes.add(p1); lobes.add(p2);
    }
    
    public BSDF getBSDF(final DifferentialGeometry dg,
          final DifferentialGeometry dgS) {
        
        BSDF bsdf = new BSDF(dgS, dg.nn);
        bsdf.addBxDF(new Lafortune(BxDF.GLOSSY, diffuse, lobes));
        return bsdf;
    }
    
}
