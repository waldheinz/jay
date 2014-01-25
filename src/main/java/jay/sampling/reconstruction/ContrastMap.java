/*
 * ContrastMap.java
 *
 * Created on 24. Februar 2006, 18:32
 */

package jay.sampling.reconstruction;

/**
 *
 * @author Matthias Treydte
 */
public class ContrastMap extends ToneMap {
    
    final float displayAdaptationY;
    
    public ContrastMap() {
        this(50.0f);
    }
    
    /** Creates a new instance of ContrastMap */
    public ContrastMap(float day) {
        System.out.println("contrast map, display adaption Y=" + day);
        this.displayAdaptationY = day;
    }

    public void map(float[] y, float[] scale, int xRes, int yRes, float maxY) {
        // Compute world adaptation luminance, _Ywa_
         float Ywa = 0.0f;
         for (int i=0; i < xRes * yRes; ++i)
                 if (y[i] > 0) Ywa += (float)Math.log(y[i]);
         
         Ywa = (float)Math.exp(Ywa / (xRes * yRes));
         
         // Compute contrast-preserving scalefactor, _s_
         float s = (float)Math.pow(
                 (1.219 + Math.pow(displayAdaptationY, 0.4f)) /
                 (1.219 + Math.pow(Ywa, 0.4f)), 2.5f);
         
         for (int i = 0; i < xRes*yRes; ++i)
                 scale[i] = s;
    }
    
}
