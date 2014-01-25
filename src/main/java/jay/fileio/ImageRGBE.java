/*
 * ImageRGBE.java
 *
 * Created on 9. Januar 2006, 20:49
 */

package jay.fileio;

import java.io.*;
import java.util.Arrays;
import jay.utils.Spectrum;

/**
 * Kann Gregory Ward's RGBE Format lesen / schreiben.
 * <p>
 * Siehe <a href="http://www.graphics.cornell.edu/~bjw/rgbe.html">
 * http://www.graphics.cornell.edu/~bjw/rgbe.html</a>.
 *
 * @author Matthias Treydte
 */
public class ImageRGBE {
    
    public static ImageData readImage(File file) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(
              new FileInputStream(file), "ISO-8859-1"));
        
        HeaderInfo hi = readHeader(br);
        
        Spectrum pixels[] = readPixels(hi, br);
        
        return new ImageData(hi.width, hi.height, pixels);
    }
    
    static Spectrum[] readPixels(HeaderInfo hi, BufferedReader br) throws
          IOException {
        final int width = hi.width;
        final int height = hi.height;
        Spectrum pixels[] = new Spectrum[width * height];
        
        switch (hi.type) {
            case RGBE_RLE_32_BIT:
                readPixelsRLE(width, height, br, pixels);
        }
        
        return pixels;
    }
    
    static void readPixelsRLE(final int width, final int height,
          BufferedReader br, Spectrum[] pixels) throws IOException {
        
        int line = 0;
        
        while (line < height) {
            
            RGBEInt rgbe = readRGBE(br);
            
            if (checkCompressedMarker(rgbe, width)) {
                /* komprimierte Zeile, jeden der vier Kanäle einlesen */
                int[] redBytes = readRLEChannel(br, width);
                int[] greenBytes = readRLEChannel(br, width);
                int[] blueBytes = readRLEChannel(br, width);
                int[] expBytes = readRLEChannel(br, width);
                
                for (int x=0; x < width; x++) {
                    rgbe = new RGBEInt();
                    rgbe.red = redBytes[x];
                    rgbe.green = greenBytes[x];
                    rgbe.blue = blueBytes[x];
                    rgbe.exponent = expBytes[x];
                    
                    pixels[line * width + x] = rgbeToSpectrum(readRGBE(br));
                    
                }
                
            } else {
                /* nicht komprimierte Zeile */
                pixels[line * width] = rgbeToSpectrum(rgbe);
                
                for (int x=1; x < width; x++) {
                    pixels[line*width + x] = rgbeToSpectrum(readRGBE(br));
                }
            }
            
            line++;
        }
    }
    
    static int[] readRLEChannel(BufferedReader br, final int width) throws
          IOException {
        
        int[] buffer = new int[width];
        int x = 0;
        
        while (x < width) {
            int marker = br.read();
            
            if (marker > 128) {
//                System.out.println(marker-128);
                /* ein Lauf */
                int v = br.read();
                Arrays.fill(buffer, x, x+marker-128-1, v);
                x += marker - 128;
            } else {
//                System.out.println("x");
                /* ein nicht - Lauf */
                if (marker == 0) throw new FileFormatException(
                      "marker starting RLE run is 0");
                
                for (int i = 0; i < marker; i++) {
                    buffer[x++] = br.read();
                }
            }
        }
        
        System.out.println(x - width);
        
        return buffer;
    }
    
    static RGBEInt readRGBE(BufferedReader br) throws IOException {
        RGBEInt rgbe = new RGBEInt();
        rgbe.red = br.read() & 0xff;
        rgbe.green = br.read() & 0xff;
        rgbe.blue = br.read() & 0xff;
        rgbe.exponent = br.read();
        return rgbe;
    }
    
    static boolean checkCompressedMarker(RGBEInt v, int width) throws
          IOException {
        
        boolean ret = (v.red == 2) && (v.green == 2) && ((v.blue & 0x80) == 0);
        if (ret && (((v.blue << 8) | v.exponent) != width)) throw
              new FileFormatException("wrong compressed scanline width");
        return ret;
    }
    
    static HeaderInfo readHeader(BufferedReader br) throws IOException {
        HeaderInfo hi = new HeaderInfo();
        
        String pType = br.readLine();
        
        if ((pType.charAt(0) != '#') || (pType.charAt(1) != '?')) {
            throw new FileFormatException("magic token missing");
        }
        
        hi.programType = pType.substring(2);
        
        while (true) {
            String line = br.readLine();
            
            if (line.startsWith("FORMAT=")) {
                String format = line.substring(7);
                if (format.equals("32-bit_rle_rgbe")) {
                    hi.type = HeaderInfo.Type.RGBE_RLE_32_BIT;
                }
            } else if (line.startsWith("-Y")) {
                String d[] = line.split(" ");
                hi.width = Integer.parseInt(d[3]);
                hi.height = Integer.parseInt(d[1]);
                /* jetzt simmer fertsch */
                break;
            }
        }
        
        return hi;
    }
    
    static class HeaderInfo {
        public static enum Type { RGBE_RLE_32_BIT };
        
        public Type type = Type.RGBE_RLE_32_BIT;
        
        /**
         * Listed at beginning of file to identify it
         * after "#?".  Defaults to "RGBE".
         */
        String programType = "RGBE";
        
        /**
         * Image has already been gamma corrected with
         * given gamma. Defaults to 1.0 (no correction).
         */
        float gamma = 1.0f;
        
        /**
         * A value of 1.0 in an image corresponds to
         * <exposure> watts/steradian/m^2.
         * Defaults to 1.0.
         */
        float exposure = 1.0f;
        
        /**
         * Breite des Bildes.
         */
        int width;
        
        /**
         * Höhe des Bildes
         */
        int height;
    }
    
    private static int exponent(float num) {
        int value = Float.floatToIntBits(num);
        int exp = ((value & (0x7f800000)) >> 23)-126;
        return exp;
    }
    
    private static float normalize(float num) {
        int value = Float.floatToIntBits(num);
        int mantissa = value & (0x007fffff);
        float res = (float)(((double)(mantissa | 0x00800000))/
              ((double)(0x01000000)));
        return res;
    }
    
    static Spectrum rgbeToSpectrum(RGBEInt rgbe) {
        float r, g, b, f;
        
        if (rgbe.exponent != 0) {
            
            f = (float)Math.pow(2.0, rgbe.exponent - (128+8));
            r = rgbe.red * f;
            g = rgbe.green * f;
            b = rgbe.blue * f;
            
            
            Spectrum s = new Spectrum(new float[] {(float)r, (float)g, (float)b});
            // System.out.println(s);
            return s;
        } else {
            return Spectrum.BLACK;
        }
    }
    
    static class RGBEInt {
        public int red;
        public int green;
        public int blue;
        public int exponent;
    }
}
