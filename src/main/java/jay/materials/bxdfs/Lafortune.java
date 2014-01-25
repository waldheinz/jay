/*
 * Lafortune.java
 *
 * Created on 2. Januar 2006, 15:51
 */

package jay.materials.bxdfs;

import java.util.ArrayList;
import jay.materials.*;
import jay.maths.Utils;
import jay.maths.Vector;
import jay.utils.Spectrum;

/**
 *
 * @author Matthias Treydte <waldheinz@gmail.com>
 */
public final class Lafortune extends BxDF {
    
    public final static class LobeParams {
        public Spectrum x;
        public Spectrum y;
        public Spectrum z;
        public Spectrum e;
    }
    
    private final ArrayList<LobeParams> lobes;
    private final Spectrum invDiffuse;
    
    /** Erstellt eine neue Instanz von Lafortune */
    public Lafortune(final int type, final Spectrum diffuse,
            final ArrayList<LobeParams> lobes) {
        
        super(type);
        this.lobes = lobes;
        this.invDiffuse = diffuse.scale(Utils.INV_PI);
    }
    
    public Spectrum eval(final Vector wo, final Vector wi) {
        Spectrum ret = invDiffuse;

        for (final LobeParams p : lobes) {
            Spectrum v =
                    p.x.scale(wo.x * wi.x).add(
                    p.y.scale(wo.y * wi.y).add(
                    p.z.scale(wo.z * wi.z)));
            
            ret = ret.add(v.pow(p.e));
        }
        
        return ret;
    }

    public float pdf(final Vector wo, final Vector wi) {
        if (!BSDF.sameHemisphere(wo, wi)) return 0.0f;
        
        float pdfSum = cosTheta(wi) * Utils.INV_PI;
        
        for (final LobeParams p : lobes) {
            float xlum = p.x.y();
            float ylum = p.y.y();
            float zlum = p.z.y();
            
            Vector center = new Vector(
                    wo.x * xlum,
                    wo.y * ylum,
                    wo.z * zlum).normalized();
            
            float e = 0.8f * p.e.y();
            pdfSum += (e + 1.0f) * Math.pow(
                    Math.max(0.0f, wi.dot(center)), e);
        }

        return pdfSum / (1.0f + lobes.size());
    }
    
    public BxDFSample sample(final Vector wo, float u1, float u2) {
        BxDFSample smp = new BxDFSample();
        smp.type = this.type;
        int rnd = Utils.randInt();
        if (rnd == Integer.MIN_VALUE) rnd = Integer.MAX_VALUE;
        int comp = Math.abs(rnd) % (lobes.size() + 1);
        
        if (comp == lobes.size()) {
            /* cosine-sample the hemisphere */
            smp.wi = Utils.cosineSampleHemisphere(u1, u2);
            if (wo.z < 0.0f)
                smp.wi = new Vector(smp.wi.x, smp.wi.y, -smp.wi.z);
        } else {
            final LobeParams lobe = lobes.get(comp);
            
            /* sample lobe */
            float xlum = lobe.x.y();
            float ylum = lobe.y.y();
            float zlum = lobe.z.y();
            float costheta = (float)Math.pow(u1,
                    1.0f / (0.8f * lobe.e.y() + 1));
            float sintheta = (float)Math.sqrt(
                    Math.max(0.0f, 1.0f - costheta*costheta));
            float phi = u2 * 2.0f * (float)Math.PI;
            Vector lobeCenter = new Vector(
                    xlum * wo.x, ylum * wo.y, zlum * wo.z).normalized();
            
            Vector lobeXY[] = Utils.coordinateSystem(lobeCenter);
            smp.wi = Utils.sphericalDirection(
                    sintheta, costheta, phi, lobeXY[0], lobeXY[1],
                    lobeCenter);
        }
        
        if (!BSDF.sameHemisphere(wo, smp.wi)) {
            smp.f = Spectrum.BLACK;
            smp.pdf = 0.0f;
            return smp;
        }
        
        smp.pdf = pdf(wo, smp.wi);
        smp.f = eval(wo, smp.wi);
        
        return smp;
    }
    
}
