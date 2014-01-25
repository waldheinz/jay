/*
 * ImageAdapter.java
 *
 * Created on 1. Februar 2006, 02:23
 */

package jay.sampling;

import java.awt.*;
import java.awt.image.*;
import jay.sampling.reconstruction.*;
import jay.utils.Spectrum;

/**
 * @author Matthias Treydte
 */
public class ImageAdapter extends BufferedImage {
    
    final ImageFilm film;
    ToneMap toneMap = new ContrastMap();
    
    /* max. Helligkeit der Anzeige */
    protected float maxDisplayY = 100;
    
    protected float gamma = 2.2f;
    
    public ImageAdapter() {
        this(null);
    }
    
    public ImageAdapter(ImageFilm film) {
        super(film.getWidth(), film.getHeight(), BufferedImage.TYPE_INT_RGB);
        this.film = film;
        update();
    }
    
    public void setToneMap(ToneMap tm) {
        this.toneMap = tm;
    }
    
    public void setGamma(float g) {
        this.gamma = g;
    }
    
    public void update() {       
        final int width = film.getWidth();
        final int height = film.getHeight();
        
        Spectrum[] lOut = new Spectrum[width * height];
        
        int off = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                lOut[off++] = film.getSpectrum(x, y);
            }
        }
        
        /* Helligkeit bestimmen */
        float[] ly = new float[width * height];
        for (int i=0; i<width*height; i++)
            ly[i] = lOut[i].y() * 683.0f;
        
        /* Tonemapping durchführen */
        float[] s = new float[width * height];
        toneMap.map(ly, s, width, height, maxDisplayY);
        float displayTo01 = 683.0f / maxDisplayY;
        
        off = 0;
        float[] rgb = new float[3];
        final float invGamma = 1.0f / gamma;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                lOut[off].getRGB(rgb);
                
                /* Skalierung aus Tonemapping anwenden */
                rgb[0] *= s[off] * displayTo01;
                rgb[1] *= s[off] * displayTo01;
                rgb[2] *= s[off] * displayTo01;
                
                /* Gamut */
                float m = Math.max(rgb[0], Math.max(rgb[1], rgb[2]));
                if (m > 1.0f) {
                    m = 1.0f / m;
                    rgb[0] *= m;
                    rgb[1] *= m;
                    rgb[2] *= m;
                }
                
                /* Gamma */
                if (gamma != 1.0f) {
                    rgb[0] = (float)Math.pow(rgb[0], invGamma);
                    rgb[1] = (float)Math.pow(rgb[1], invGamma);
                    rgb[2] = (float)Math.pow(rgb[2], invGamma);
                }
                
                /* auf 0..255 skalieren */
                rgb[0] *= 255;
                rgb[1] *= 255;
                rgb[2] *= 255;
                
                getRaster().setPixel(x, y, rgb);
                off++;
            }
        }
        
    }
    
}
