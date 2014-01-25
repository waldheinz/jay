/*
 * MicrofacetDistribution.java
 *
 * Created on 31. Dezember 2005, 14:57
 */

package jay.materials.bxdfs;

import jay.maths.Vector;

/**
 *
 * @author Matthias Treydze
 */
public abstract class MicrofacetDistribution {
    
    /**
     * Gibt den "D" - Parameter für das Torrance-Sparrow
     * Modell zurück.
     */
    public abstract float d(final Vector wh);
    
    public abstract float pdf(final Vector wo, final Vector wi);
    
    public abstract VectorSample sample(final Vector wo, float u1, float u2); 
    
}
