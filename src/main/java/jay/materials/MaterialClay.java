/*
 * MaterialClay.java
 *
 * Created on 29. Juli 2007, 15:40
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
public class MaterialClay extends Material {
    
    private final static ArrayList<LobeParams> lobes =
            new ArrayList<LobeParams>();
    
    static private Lafortune bsdf;
    
    private final static Spectrum diffuse = new Spectrum( new float[] {
        0.3094f,    0.39667f,   0.70837f
    });
    
    static {
        float diffuse[] = {   0.383626f,   0.260749f,   0.274207f };
	float xy0[] =     {  -1.089701f,  -1.102701f,  -1.107603f };
	float z0[] =      {  -1.354682f,  -2.714801f,  -1.569866f };
	float e0[] =      {  17.968505f,  11.024489f,  21.270282f };
	float xy1[] =     {  -0.733381f,  -0.793320f,  -0.848206f };
	float z1[] =      {   0.676108f,   0.679314f,   0.726031f };
	float e1[] =      {   8.219745f,   9.055139f,  11.261951f };
	float xy2[] =     {  -1.010548f,  -1.012378f,  -1.011263f };
	float z2[] =      {   0.910783f,   0.885239f,   0.892451f };
	float e2[] =      { 152.912795f, 141.937171f, 201.046802f };
        
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
