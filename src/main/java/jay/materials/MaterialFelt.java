/*
 * MaterialFelt.java
 *
 * Created on 29. Juli 2007, 15:16
 */

package jay.materials;

import java.util.ArrayList;
import jay.materials.bxdfs.BxDF;
import jay.materials.bxdfs.Lafortune;
import jay.materials.bxdfs.Lafortune.LobeParams;
import jay.scene.primitives.DifferentialGeometry;
import jay.utils.Spectrum;

/**
 * <p>
 * Uses a Lafortune BxDF to imitate the (measured) appearance
 * of felt.
 * </p>
 * 
 * @author Matthias Treydte <waldheinz@gmail.com>
 */
public class MaterialFelt extends Material {
    
    private final static ArrayList<LobeParams> lobes =
            new ArrayList<LobeParams>();
    
    static private Lafortune bsdf;
    
    private final static Spectrum diffuse = new Spectrum( new float[] {
        0.3094f,    0.39667f,   0.70837f
    });
    
    static {
        float diffuse[] = {  0.025865f,  0.025865f,  0.025865f};
	float xy0[] =     { -0.304075f, -0.304075f, -0.304075f};
	float z0[] =      { -0.065992f, -0.065992f, -0.065992f};
	float e0[] =      {  3.047892f,  3.047892f,  3.047892f};
	float xy1[] =     { -0.749561f, -0.749561f, -0.749561f};
	float z1[] =      { -1.167929f, -1.167929f, -1.167929f};
	float e1[] =      {  6.931827f,  6.931827f,  6.931827f};
	float xy2[] =     {  1.004921f,  1.004921f,  1.004921f};
	float z2[] =      { -0.205529f, -0.205529f, -0.205529f};
	float e2[] =      { 94.117332f, 94.117332f, 94.117332f};
        
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
        
        lobes.add(p0);
        lobes.add(p1);
        lobes.add(p2);
        bsdf = new Lafortune(BxDF.DIFFUSE, new Spectrum(diffuse), lobes);
    }
    
    public BSDF getBSDF(final DifferentialGeometry dg,
          final DifferentialGeometry dgS) {
        
        BSDF b = new BSDF(dgS, dg.nn);
        b.addBxDF(bsdf);
        return b;
    }
    
}
