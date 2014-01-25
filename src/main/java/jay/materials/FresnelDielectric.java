/*
 * FresnelDielectric.java
 *
 * Created on 28. Dezember 2005, 21:40
 */

package jay.materials;

import jay.utils.Spectrum;

/**
 *
 * @author Matthias Treydte
 */
public class FresnelDielectric extends Fresnel {
    
    float eta_o;
    float eta_t;
    
    /** Creates a new instance of FresnelDielectric */
    public FresnelDielectric(float eo, float et) {
        eta_o = eo;
        eta_t = et;
    }

    public Spectrum eval(float coso) {
        coso = Math.max(-1.0f, Math.min(1.0f, coso));
        
        boolean entering = coso > 0.0f;
        
        float eo = (entering)?eta_o:eta_t;
        float et = (entering)?eta_t:eta_o;
        
        float sint = eo / et * 
                (float)Math.sqrt(Math.max(0.0f, 1.0f - coso * coso));
        
        if (sint > 1.0f) {
            /* totale interne Reflektion */
            return new Spectrum(1);
        } else {
            float cost = (float)Math.sqrt(Math.max(0.0f, 1.0f - sint * sint));
            Spectrum ceo = new Spectrum(eo);
            Spectrum cet = new Spectrum(et);
            return fresnelDielectric(Math.abs(coso), cost, ceo, cet);
        }
    }
    
}
