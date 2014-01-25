/*
 * MaterialPrimer.java
 *
 * Created on 29. Juli 2007, 15:46
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
 * @author Matthias Treydte <waldheinz@gmail.com>
 */
public class MaterialPrimer extends Material {
    
    private final static ArrayList<LobeParams> lobes =
            new ArrayList<LobeParams>();
    
    static private Lafortune bsdf;
    
    private final static Spectrum diffuse = new Spectrum( new float[] {
        0.3094f,    0.39667f,   0.70837f
    });
    
    static {
        float diffuse[] = {  0.118230f,  0.121218f,  0.133209f};
	float xy0[] =     { -0.399286f, -1.033473f, -1.058104f};
	float z0[] =      {  0.167504f,  0.009545f, -0.068002f};
	float e0[] =      {  2.466633f,  7.637253f,  8.117645f};
	float xy1[] =     { -1.041861f, -1.100108f, -1.087779f};
	float z1[] =      {  0.014375f, -0.198147f, -0.053605f};
	float e1[] =      {  7.993722f, 29.446268f, 41.988990f};
	float xy2[] =     { -1.098605f, -0.379883f, -0.449038f};
	float z2[] =      { -0.145110f,  0.159127f,  0.173224f};
	float e2[] =      { 31.899719f,  2.372852f,  2.636161f};
        
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
