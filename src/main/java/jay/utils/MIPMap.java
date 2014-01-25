/*
 * MIPMap.java
 *
 * Created on 25. Februar 2006, 14:35
 */

package jay.utils;

/**
 *
 * @author Matthias Treydte
 */
public class MIPMap<T> {
    
    public static enum ImageWrap {
        TEXTURE_REPEAT,
        TEXTURE_BLACK,
        TEXTURE_CLAMP
    }
    
    class ResampleWeight {
        int firstTexel;
        float[] weight = new float[4];
    }
    
    boolean doTrilinear;
    float maxAnisotropy;
    ImageWrap wrapMode;
    
    T[][] pyramid;
    int nLevels;
    public final static int WEIGHT_LUT_SIZE = 128;
    static float[] weightLut;
    
    public MIPMap(int xres, int yres, T[] data) {
        this(xres, yres, data, false, 8.0f, ImageWrap.TEXTURE_REPEAT);
    }
    
    /** Creates a new instance of MIPMap */
    public MIPMap(int xres, int yres, T[] data,
            boolean doTri, float maxAnsi,
            ImageWrap wrapMode) {
        
        
    }
    
}
