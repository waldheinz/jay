/*
 * NonLinearMap.java
 *
 * Created on 24. Februar 2006, 19:46
 */

package jay.sampling.reconstruction;

/**
 *
 * @author Matthias Treydte
 */
public class NonLinearMap extends ToneMap {
    
    final float maximumY;
    
    public NonLinearMap() {
        this(0.0f);
    }
    
    /** Creates a new instance of NonLinearMap */
    public NonLinearMap(float maxY) {
        System.out.println("nonlinear map, maxY=" + maxY);
        this.maximumY = maxY;
    }
    
    public void map(float[] y, float[] scale, int xRes, int yRes, float maxY) {
        float invY2;
        if (maximumY <= 0.0f) {
            // Compute world adaptation luminance, _Ywa_
            float Ywa = 0.0f;
            for (int i=0; i < xRes * yRes; ++i)
                if (y[i] > 0) Ywa += Math.log(y[i]);
            Ywa = (float)Math.exp(Ywa / (xRes * yRes));
            Ywa /= 683.f;
            invY2 = 1.0f / (Ywa * Ywa);
            System.out.println("NonLinearMap Ywa=" + Ywa);
        } else {
            invY2 = 1.0f / (maximumY * maximumY);
        }
        
        
        
        for (int i = 0; i < xRes * yRes; ++i) {
            float ys = y[i] / 683.f;
            scale[i] = maxY / 683.f *
                    (1.f + ys * invY2) / (1.f + ys);
        }
    }
    
}
