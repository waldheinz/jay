/*
 * RegularSpectralCurve.java
 *
 * Created on 30. Januar 2006, 23:38
 */

package jay.lights.skylight;

import jay.maths.Utils;
import jay.utils.Spectrum;

/**
 * @author Matthias Treydte
 */
public class RegularSpectralCurve {
    
    float[] amplitudes;
    float minLambda;
    float maxLambda;
    float invStepSize;
    
    public RegularSpectralCurve(float[] amplitudes, float minLambda,
          float maxLambda) {
        
        this.amplitudes = amplitudes;
        this.minLambda = minLambda;
        this.maxLambda = maxLambda;
        this.invStepSize = amplitudes.length / (maxLambda - minLambda);
    }
    
    public float getValue(float lambda) {
        final int maxidx = amplitudes.length - 1;
        if (lambda <= minLambda) return amplitudes[0];
        if (lambda >= maxLambda) return amplitudes[maxidx];
        
        int left = (int)((lambda - minLambda) * invStepSize);
        int right = Math.min(left + 1, maxidx);
        
        return Utils.lerp((lambda - (int)lambda) * invStepSize,
              amplitudes[left], amplitudes[right]);
    }
    
    public Spectrum getSpectrum() {
        float x=0, y=0, z=0, v;
        final int count = Spectrum.CIE_END - Spectrum.CIE_START + 1;
        
        for (int i=0; i < count; i += 10) {
            v = getValue(i);
            
            x += Spectrum.CIE_X[i] * v;
            y += Spectrum.CIE_Y[i] * v;
            z += Spectrum.CIE_Z[i] * v;
        }
        
        return Spectrum.fromXYZ(x, y, z);
    }
    
}
