/*
 * BluePaint.java
 *
 * Created on 2. Januar 2006, 18:24
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
public class BluePaint extends Material {
    
    private final static ArrayList<LobeParams> lobes =
            new ArrayList<LobeParams>();
    
    static private Lafortune bsdf;
    
    private final static Spectrum diffuse = new Spectrum( new float[] {
        0.3094f,    0.39667f,   0.70837f
    });
    
    static {
        float xy0[] = {  0.870567f,  0.857255f,  0.670982f };
        float z0[]  = {  0.803624f,  0.774290f,  0.586674f };
        float e0[]  = { 21.820103f, 18.597755f,  7.472717f };
        float xy1[] = { -0.451218f, -0.406681f, -0.477976f };
        float z1[]  = {  0.023123f,  0.017625f,  0.227295f };
        float e1[]  = {  2.774499f,  2.581499f,  3.677653f };
        float xy2[] = { -1.031545f, -1.029426f, -1.026588f };
        float z2[]  = {  0.706734f,  0.696530f,  0.687715f };
        float e2[]  = { 66.899060f, 63.767912f, 57.489181f };
        
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
        bsdf = new Lafortune(BxDF.DIFFUSE, diffuse, lobes);
    }
    
    public BSDF getBSDF(final DifferentialGeometry dg,
          final DifferentialGeometry dgS) {
        
        BSDF b = new BSDF(dgS, dg.nn);
        b.addBxDF(bsdf);
        return b;
    }
}
