/*
 * Film.java
 *
 * Created on 15. Dezember 2005, 17:42
 */

package jay.sampling;

import java.util.ArrayList;
import jay.maths.Ray;
import jay.utils.*;

/**
 * @author Matthias Treydte <waldheinz@gmail.com>
 */
public final class ImageFilm extends Film {
    
    private final static int UPDATE_INTERVALL = 2500000;
    private final static int TABLE_SIZE = 16;
    
    private final Pixel[][] pixels;
    private Filter filter = new MitchellFilter(2, 2, 1.0f / 3, 1.0f / 3);
    
    final int xPixelStart = 0;
    final int yPixelStart = 0;
    final int xPixelCount;
    final int yPixelCount;
    final float[] filterTable;
    
    int samplesSinceUpdate = 0;
    
    ArrayList<ImageFilmListener> listeners =
            new ArrayList<ImageFilmListener>();
    
    public ImageFilm(int w, int h) {
        super(w, h);
        
        xPixelCount = w;
        yPixelCount = h;
        filterTable = new float[TABLE_SIZE * TABLE_SIZE];
        int offset = 0;
        
        for (int y=0; y < TABLE_SIZE; y++) {
            float fy = ((float)y + 0.5f) * filter.yWidth / TABLE_SIZE;
            for (int x=0; x < TABLE_SIZE; x++) {
                float fx = ((float)x + 0.5f) * filter.xWidth / TABLE_SIZE;
                filterTable[offset++] = filter.eval(fx, fy);
            }
        }
        
        /* Pixel - Array initialisieren */
        pixels = new Pixel[w][h];
        for (int y=0; y<h; y++)
            for (int x=0; x<w; x++)
                pixels[x][y] = new Pixel();
        
    }
    
    public void addListener(ImageFilmListener listener) {
        listeners.add(listener);
    }
    
    public void removeListener(ImageFilmListener listener) {
        listeners.remove(listener);
    }
    
    protected void fireRegionUpdated() {
        for (ImageFilmListener l : listeners)
            l.filmUpdated(this);
    }
    
    public int getRGB(int x, int y) {
        return getSpectrum(x, y).getRGBI();
    }
    
    public Spectrum getSpectrum(int x, int y) {
        Pixel p = pixels[x][y];
        if (p.weightSum != 0.0f) return p.c.scale(1.0f / p.weightSum);
        else return p.c;
    }
    
    public void addSample(float imageX, float imageY, Ray ray, Spectrum c) {
        
        if (c.isNaN()) {
            System.err.println("skipped bad sample (nan)");
            return;
        }
        
        if (c.y() < 0) {
            System.err.println("skipped bad sample (< 0)");
            return;
        }
//        
        if (Float.isInfinite(c.y())) {
            System.err.println("skipped bad sample (infinite y)");
            return;
        }
        
        float dImageX = imageX - 0.5f;
        float dImageY = imageY - 0.5f;
        
        int x0 = (int)Math.ceil(dImageX - filter.xWidth);
        int x1 = (int)Math.floor(dImageX + filter.xWidth);
        int y0 = (int)Math.ceil(dImageY - filter.yWidth);
        int y1 = (int)Math.floor(dImageY + filter.yWidth);
        
        x0 = Math.max(x0, xPixelStart);
        x1 = Math.min(x1, xPixelStart + xPixelCount - 1);
        y0 = Math.max(y0, yPixelStart);
        y1 = Math.min(y1, yPixelStart + yPixelCount - 1);

        int[] ifx = new int[x1 - x0 + 1];
        for (int x = x0; x <= x1; ++x) {
            float fx = Math.abs((x - dImageX) * filter.invXwidth * TABLE_SIZE);
            ifx[x-x0] = Math.min((int)fx, TABLE_SIZE-1);
        }
        
        int[] ify = new int[y1 - y0 + 1];
        for (int y = y0; y <= y1; ++y) {
            float fy = Math.abs((y - dImageY) * filter.invYwidth * TABLE_SIZE);
            ify[y-y0] = Math.min((int)fy, TABLE_SIZE-1);
        }
        
        for (int y = y0; y <= y1; ++y) {
            for (int x = x0; x <= x1; ++x) {
                // Evaluate filter value at $(x,y)$ pixel
                int offset = ify[y-y0] * TABLE_SIZE + ifx[x-x0];
                float filterWt = filterTable[offset];
                // Update pixel values with filtered sample contribution
                Pixel pixel = pixels[x - xPixelStart][y - yPixelStart];
                pixel.c = pixel.c.add(c.scale(filterWt));
                //pixel.alpha += alpha * filterWt;
                pixel.weightSum += filterWt;
            }
        }
        
        if (++samplesSinceUpdate >= UPDATE_INTERVALL) {
            fireRegionUpdated();
            samplesSinceUpdate = 0;
        }
    }

    public int[] getSamplingExtent() {
        int[] res = new int[4];
        
        res[0] = (int)Math.floor(xPixelStart + 0.5f - filter.xWidth);
        res[1] = (int)Math.floor(xPixelStart + 0.5f + xPixelCount  +
                filter.xWidth);
        res[2] = (int)Math.floor(yPixelStart + 0.5f - filter.yWidth);
        res[3] = (int)Math.floor(yPixelStart + 0.5f + yPixelCount +
                filter.yWidth);
        
        return res;
    }
    
    int getWidth() {
        return xPixelCount;
    }
    
    int getHeight() {
        return yPixelCount;
    }

    private static class Pixel {
        public Spectrum c = Spectrum.BLACK;
        public float weightSum;
    }
}
