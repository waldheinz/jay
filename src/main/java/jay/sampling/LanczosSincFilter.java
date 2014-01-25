/*
 * LanczosSincFilter.java
 *
 * Created on 29. Dezember 2005, 16:48
 */

package jay.sampling;

/**
 *
 * @author Matthias Treydze
 */
public class LanczosSincFilter extends Filter {
    
    final float tau;
    
    /** Erstellt eine neue Instanz von LanczosSincFilter */
    public LanczosSincFilter(float xw, float yw, float tau) {
        super(xw, yw);
        this.tau = tau;
    }
    
    public float eval(float x, float y) {
        return sinc1D(x * invXwidth) * sinc1D(y * invYwidth);
    }
    
    public float sinc1D(float x) {
        x = Math.abs(x);
        
        if (x < 1e-5) return 1.0f;
        if (x > 1.0f) return 0.0f;
        x *= Math.PI;
        
        float sinc = (float)Math.sin(x * tau) / (x * tau);
        float lanczos = (float)Math.sin(x) / x;
        
        return sinc * lanczos;
    }

}
