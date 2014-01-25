/*
 * Utils.java
 *
 * Created on 2. März 2006, 21:04
 */

package jay.sampling;

import jay.maths.Vector;

/**
 *
 * @author trem
 */
public class Utils {
    
    public static void latinHyperCube(float[] samples, int nSamples, int nDim) {
        // Generate LHS samples along diagonal
        float delta = 1.0f / nSamples;
        
        for (int i = 0; i < nSamples; ++i)
            for (int j = 0; j < nDim; ++j)
                samples[nDim * i + j] = (i + jay.maths.Utils.rand()) * delta;
        
        /* in jeder Dimension permutieren */
        for (int i = 0; i < nDim; ++i) {
            for (int j = 0; j < nSamples; ++j) {
                int other = Math.abs(jay.maths.Utils.randInt()) % nSamples;
                float tmp = samples[nDim * j + i];
                samples[nDim * j + i] = samples[nDim * other + i];
                samples[nDim * other + i] = tmp;
            }
        }
    }
    
    public static Vector uniformSampleHemisphere(float u1, float u2) {
        final float z = u1;
        final float r = (float)Math.sqrt(Math.max(0.0f, 1.0f - z*z));
        final float phi = 2.0f * (float)Math.PI * u2;
        final float x = r * (float)Math.cos(phi);
        final float y = r * (float)Math.sin(phi);
        return new Vector(x, y, z);
    }
    
}
