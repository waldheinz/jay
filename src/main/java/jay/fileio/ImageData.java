/*
 * ImageData.java
 *
 * Created on 10. Januar 2006, 17:19
 */

package jay.fileio;

import jay.utils.Spectrum;
import jay.utils.SpectrumCalc;

/**
 * @author Matthias Treydte
 */
public class ImageData {
    
    final int width;
    final int height;
    final Spectrum[] pixels;
    
    public ImageData(int width, int height, Spectrum[] pixels) {
        this.width = width;
        this.height = height;
        this.pixels = pixels;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
    
    public Spectrum getPixel(int x, int y) {
        return pixels[y * width + x];
    }
    
    public Spectrum getPixelBilin(float s, float t) {
//        System.out.println(s + " " + t);
        
//        s += 0.5;// if (s > 1.0f) s -= 1.0f;
//        t += 0.5;// if (t < 0.0f) t += 1.0f;
        
        float col = s * width - 0.5f;
        float row = t * height - 0.5f;
        
        if (true) return pixels[(int)row * width + (int)col];
        
        //col = Utils.clamp(col, 0, width);
        //row = Utils.clamp(row, 0, height);
        
        int left = (int)Math.floor(col);
        int right = left < width - 1 ? left + 1 : 0;
        float rightPart = col - left;
        
        int upper = (int)Math.floor(row);
        int lower = upper < height - 1 ? upper + 1 : 0;
        float lowerPart = row - upper;
        
//        left = left % width;
//        right = right % width;
//        upper = upper % height;
//        lower = lower % height;
//        
        //System.out.println(upper);
        
        int upperLeft = upper * width + left;
        int upperRight = upper * width + right;
        int lowerLeft = lower * width + left;
        int lowerRight = lower * width + right;
        

        float upperLeftPart = (1 - lowerPart) * (1 - rightPart);
        float upperRightPart = (1 - lowerPart) * rightPart;
        float lowerLeftPart = lowerPart * (1 - rightPart);
        float lowerRightPart = lowerPart * rightPart;
        
        SpectrumCalc sc = new SpectrumCalc();
        
        sc.add(pixels[upperLeft].scale(upperLeftPart));
        sc.add(pixels[upperRight].scale(upperRightPart));
        sc.add(pixels[lowerLeft].scale(lowerLeftPart));
        sc.add(pixels[lowerRight].scale(lowerRightPart));
        
        return sc.s;
    }
    
}
