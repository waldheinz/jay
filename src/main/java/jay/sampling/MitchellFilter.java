/*
 * MitchellFilter.java
 *
 * Created on 31. Dezember 2005, 01:06
 */

package jay.sampling;

/**
 *
 * @author Matthias Treydze
 */
public class MitchellFilter extends Filter {
    
    private final float B;
    private final float C;
    
    /** Erstellt eine neue Instanz von MitchellFilter */
    public MitchellFilter(float xw, float yw, float b, float c) {
        super(xw, yw);
        this.B = b;
        this.C = c;
    }
    
    public float eval(float x, float y) {
        return mitchell1D(x * invXwidth) * mitchell1D(y * invYwidth);
    }
    
    public float mitchell1D(float x) {
        x = Math.abs(2.0f * x);
        if (x > 1.0f)
            return ((-B - 6*C) * x*x*x + (6*B + 30*C) * x*x +
                    (-12*B - 48*C) * x + (8*B + 24*C)) * (1.f/6.f);
        else
            return ((12 - 9*B - 6*C) * x*x*x +
                    (-18 + 12*B + 6*C) * x*x +
                    (6 - 2*B)) * (1.f/6.f);
    }
    
}
