/*
 * BSDF.java
 *
 * Created on 16. Dezember 2005, 17:51
 */

package jay.materials;

import java.util.ArrayList;
import jay.materials.bxdfs.BxDF;
import jay.maths.*;
import jay.scene.primitives.DifferentialGeometry;
import jay.utils.*;

/**
 * Bidirectional Scattering Distribution Function
 *
 * @author Matthias Treydte
 */
public class BSDF {
    
    /**
     * The {@link DifferentialGeoemtry} used for
     * shading calculations
     */
    final public DifferentialGeometry dgS;
    
    final private Normal nn, sn, tn;
    
    /**
     * The true normal at the intersection point.
     */
    final private Normal nGeom;
    final private ArrayList<BxDF> bxdfs = new ArrayList<BxDF>();
    
    public BSDF(final DifferentialGeometry dgS, final Normal nG) {
        this.dgS = dgS;
        this.nGeom = nG;
        nn = dgS.nn;
        sn = dgS.dpdu.normalized();
        tn = sn.cross(nn).asNormal();
    }
    
    public void addBxDF(final BxDF bxdf) { bxdfs.add(bxdf); }
    
    public Spectrum eval(final Vector wi, final Vector wo) {
        return eval(wi, wo, BxDF.ALL);
    }
    
    public Spectrum eval(final Vector wiW, final Vector woW, int type) {
        final Vector wi = worldToLocal(wiW);
        final Vector wo = worldToLocal(woW);
        
        if ((wiW.dot(nGeom) * woW.dot(nGeom)) > 0) {
            /* Transmission ignorieren */
            type &= ~BxDF.TRANSMISSION;
        } else {
            /* Reflektion ignorieren */
            type &= ~BxDF.REFLECTION;
        }
        
        Spectrum res = Spectrum.BLACK;
        
        for (final BxDF bxdf : bxdfs) {
            if (bxdf.matches(type))
                res = res.add(bxdf.eval(wi, wo));
        }
        
        return res;
    }
    
    public BxDFSample sample(final Vector woW, float u1, float u2, int type) {
        
        int nComps = matchingComponents(type);
        int which = Math.min(nComps-1, (int)(Utils.rand() * nComps));
        int comp = which;
        BxDF bxdf = null;
        
        for (final BxDF b : bxdfs) {
            if (b.matches(type)) {
                if (comp-- == 0) { bxdf = b; break; }
            }
        }
        
        if (bxdf == null) {
            BxDFSample s = new BxDFSample();
            s.f = Spectrum.BLACK;
            return s;
        }
        
        final Vector wo = worldToLocal(woW);
        
        BxDFSample s = bxdf.sample(wo, u1, u2);
        s.wi = this.localToWorld(s.wi);
        
        if ((s.type & BxDF.SPECULAR) == 0) {
            for (final BxDF b : bxdfs) {
                if ((b != bxdf) && b.matches(type)) {
                    s.pdf += b.pdf(wo, s.wi);
                    s.f = s.f.add(b.eval(wo, s.wi));
                }
            }
        }
        
        return s;
    }
    
    public boolean hasShadingGeometry() {
        return (!nn.equals(nGeom));
    }
    
    public float pdf(final Vector wiW, final Vector woW) {
        if (bxdfs.size() == 0) return 0.0f;
        
        final Vector wi = worldToLocal(wiW);
        final Vector wo = worldToLocal(woW);
        
        float pdf = 0.0f;
        
        for (final BxDF bxdf : bxdfs)
            pdf += bxdf.pdf(wi, wo);
        
        return pdf / bxdfs.size();
    }
    
    public int matchingComponents(final int type) {
        int i = 0;
        for (final BxDF bxdf : bxdfs)
            if (bxdf.matches(type)) i++;
        
        return i;
    }
    
    public Spectrum rho(final int type) {
        Spectrum ret = Spectrum.BLACK;
        
        for (final BxDF bxdf : bxdfs) {
            if (bxdf.matches(type)) {
                ret = ret.add(bxdf.rho());
            }
        }
        
        return ret;
    }
    
    public int numComponents() {
        return bxdfs.size();
    }
    
    /**
     * Entscheidet, ob sich zwei Vektoren in der gleichen Hemisphäre
     * befinden. Die übergebenen Vektoren müssen sich auf das lokale
     * Koordinatensystem zur Beleuchtungsberechnung befinden.
     *
     * @param v1 der erste Vektor
     * @param v2 der zweite Vektor
     * @return true, wenn sich beide Vektoren in der gleichen
     *     Hemisphäre befinden
     */
    public static boolean sameHemisphere(final Vector v1, final Vector v2) {
        return ((v1.z * v2.z) > 0.0f);
    }
    
    /**
     * Transformiert einen Vektor in das Koordinatensystem
     * zur Beleuchtungsberechnung.
     *
     * @param v der zu transformierende Vektor
     * @return der Vektor im Beleuchtungs - Koordinatensystem
     */
    protected Vector worldToLocal(final Vector v) {
        return new Vector(v.dot(sn), v.dot(tn), v.dot(nn));
    }
    
    /**
     * Transformiert einen Vektor vom Koordinatensystem zur
     * Beleuchtungsberechnung in Weltkoordinaten.
     *
     * @param v der zu transformierende Vektor
     * @return der Vektor in Weltkoordinaten
     */
    protected Vector localToWorld(final Vector v) {
        return new Vector(
                sn.x * v.x + tn.x * v.y + nn.x * v.z,
                sn.y * v.x + tn.y * v.y + nn.y * v.z,
                sn.z * v.x + tn.z * v.y + nn.z * v.z);
    }

    public Spectrum rho(final Vector wo, int type) {
        Spectrum ret = Spectrum.BLACK;
        
        for (BxDF bxdf : bxdfs)
            ret = ret.add(bxdf.rho(worldToLocal(wo)));
        
        return ret;
    }
}
