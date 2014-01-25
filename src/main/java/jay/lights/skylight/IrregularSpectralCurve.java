/*
 * IrregularSpectralCurve.java
 *
 * Created on 30. Januar 2006, 22:55
 */

package jay.lights.skylight;

import java.util.Arrays;
import jay.maths.Utils;

/**
 * @author Matthias Treydte
 */
public class IrregularSpectralCurve {
    
    final float[] lambdas;
    final float[] amplitudes;
    
    public IrregularSpectralCurve(float[] amplitudes, float[] lambdas) {
        this.lambdas = lambdas;
        this.amplitudes = amplitudes;
    }
    
    public float getValue(float lambda) {
        int idx = Arrays.binarySearch(lambdas, lambda);
        
        /* exact match? */
        if (idx >= 0) return amplitudes[idx];
        
        int right = -(idx + 1);
        if (right == lambdas.length) return amplitudes[right-1];
        int left = Math.max(0, right - 1);
        return Utils.lerp((lambda - lambdas[left]) / lambdas[right],
              amplitudes[left], amplitudes[right]);
    }
}
