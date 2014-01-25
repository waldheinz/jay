/*
 * Fresnel.java
 *
 * Created on 28. Dezember 2005, 21:34
 */

package jay.materials;

import jay.utils.*;

/**
 *
 * @author Matthias Treydte
 */
public abstract class Fresnel {
    
    /**
     * Gibt die Menge Licht zurück, welche von der Oberfläche
     * reflektiert wird.
     *
     * @param cosi Cosinus des Winkels zwischen einfallendem
     *    Lichtstrahl und der Normale der Oberfläche
     */
    public abstract Spectrum eval(float cosi);
    
    
    /**
     * Fresnel für einen Isolator
     */
    static Spectrum fresnelDielectric(float coso, float cost,
            final Spectrum etao, final Spectrum etat) {
        
        Spectrum cPara = etat.scale(coso).sub(etao.scale(cost)).
                div(etat.scale(coso).add(etao.scale(cost)));
        
        Spectrum cPerp = etao.scale(coso).sub(etat.scale(cost)).
                div(etao.scale(coso).add(etat.scale(cost)));
        
        return cPara.scale(cPara).add(cPerp.scale(cPerp)).scale(0.5f);
    }
    
    /**
     * Fresnel für einen Leiter
     */
    static Spectrum fresnelConductor(float coso, Spectrum eta, Spectrum k) {
        Spectrum tmp = eta.scale(eta).add(k.scale(k)).scale(coso * coso);
        
        Spectrum rPara2 = tmp.sub(eta.scale(2.0f * coso)).add(new Spectrum(1, 1, 1)).
                div(tmp.add(eta.scale(2.0f * coso)).add(new Spectrum(1, 1, 1)));
        
        Spectrum tmp_f = eta.scale(eta).add(k.scale(k));
        
        final float c2 = coso * coso;
        
        Spectrum rPerp2 = tmp_f.sub(eta.scale(2.0f * coso)).add(
                new Spectrum(c2, c2, c2)).
                div(tmp_f.add(eta.scale(2.0f * coso)).add(
                new Spectrum(c2, c2, c2)));
        
        return rPara2.add(rPerp2).scale(0.5f);
    }
    
    static Spectrum fresnelApproxEta(Spectrum c) {
        Spectrum i = c.clamp(0.0f, 0.999f);
        return new Spectrum(1, 1, 1).add(i.sqrt()).div(
                new Spectrum(1, 1, 1).sub(i.sqrt()));
    }
    
    static Spectrum fresnelApproxK(Spectrum c) {
        Spectrum i = c.clamp(0.0f, 0.999f);
        return i.div(new Spectrum(1, 1, 1).sub(i)).sqrt().scale(2.0f);
    }
    
}
