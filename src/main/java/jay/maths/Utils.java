/*
 * Utils.java
 *
 * Created on 15. Dezember 2005, 20:02
 */

package jay.maths;

import static java.lang.Math.*;


/**
 * Some mathematical utility functions.
 *
 * @author Matthias Treydte <waldheinz@gmail.com>
 */
public final class Utils {
    
    /**
     * This utility class can not be instanced.
     */
    private Utils() { }
    
    public static final float EPSILON = 0.0001f;
    
    /**
     * 1 / PI
     */
    public static final float INV_PI = 1.0f / (float)Math.PI;
    
    /**
     * 1 / (2 * PI)
     */
    public static final float INV_2PI = 1.0f / ((float)Math.PI * 2);
    
    /**
     * For having a thread-local random number generator.
     */
    private static ThreadLocal<MersenneTwister> randomGen =
            new ThreadLocal<MersenneTwister>() {
        @Override
        protected MersenneTwister initialValue() {
            return new MersenneTwister();
        }
    };

    /**
     * Returns a random generator for public use. The random generator
     * is local to the current thread.
     *
     * @return a random generator.
     */
    public static MersenneTwister getRandom() {
        return randomGen.get();
    }

    /**
     * Solves the quadric equation: ? :-)
     *
     * @param a
     * @param b
     * @param c 
     * @return The two roots of the quadric as an array, or
     *      <code>null</code> if the quadric equation can not be solved.
     */
    public static float[] solveQuadric(float a, float b, float c) {
        
        double D = b*b - 4.0 * a * c;
        
        /* keine (reelle) Lösung */
        if (D < 0.0) return null;
        
        final double a2 = a * 2.0;
        D = Math.sqrt(D) / a2;
        
        final double x = -b / a2;
        final float t1 = (float)(x + D);
        final float t2 = (float)(x - D);
        
        /* immer die kleinere Lösung zuerst */
        if (t1 > t2) {
            return new float[] { t2, t1 };
        } else {
            return new float[] { t1, t2 };
        }
    }
    
    public static boolean solveLinearSystem2x2(final float A[][],
                                        final float B[], float x[]) {
        
        float det = A[0][0] * A[1][1] - A[0][1] * A[1][0];
        
        if (Math.abs(det) < 1e-5)
            return false;
    
         float invDet = 1.0f/det;
         x[0] = (A[1][1]*B[0] - A[0][1]*B[1]) * invDet;
         x[1] = (A[0][0]*B[1] - A[1][0]*B[0]) * invDet;
         
         return true;
    }
    
    public static boolean sameHemisphere(final Vector v1, final Vector v2,
          final Normal n) {
        
        return (Math.signum(n.dot(v1)) == Math.signum(n.dot(v2)));
    }
    
    /**
     * Returns a random number in {@literal [0, 1)}.
     * 
     * @return a random number in {@literal [0, 1)}.
     */
    public static float rand() {
        return randomGen.get().nextFloat();
    }
    
    public static int randInt() {
        return randomGen.get().nextInt();
    }
    
    /**
     * Interpoliert linear zwischen v1 und v2. Für t == 0 wird genau v1
     * zurückgegeben, für t == 1 genau v2.
     * 
     * @param t
     * @param v1
     * @param v2
     * @return 
     */
    public static float lerp(float t, float v1, float v2) {
        return (1.0f - t) * v1 + t * v2;
    }
    
    public static float[] uniformSampleTriangle(float u1, float u2) {
        float[] b1b2 = new float[2];
        float su1 = (float)Math.sqrt(u1);
        b1b2[0] = 1.0f - su1;
        b1b2[1] = u2 * su1;
        return b1b2;
    }
    
    /**
     * Gibt für gleichverteilte u1 und u2 gleichverteilte Punkte auf
     * der Oberflächte der Einheitskugel zurück.
     * 
     * @param u1 the first uniform random number.
     * @param u2 the second uniform random number.
     * @return a random direction in the unit sphere.
     */
    public static Vector uniformSampleSphere(float u1, float u2) {
        final float z = 1.0f - 2.0f * u1;
        final float r = (float)Math.sqrt(Math.max(0.0f, 1.0f - z*z));
        final float phi = 2.0f * (float)Math.PI * u2;
        final float x = r * (float)Math.cos(phi);
        final float y = r * (float)Math.sin(phi);
        
        return new Vector(x, y, z);
    }
    
    public static float uniformSpherePDF() {
        return 1.0f / (4.0f * (float)Math.PI);
    }
    
    /**
     * 
     *
     * @param u1 the first uniform random number.
     * @param u2 the second uniform random number.
     * @param costhetamax max. Öffnungswinkel des Konus
     * @return 
     */
    public static Vector uniformSampleCone(float u1, float u2,
            float costhetamax) {
        
        final float costheta = lerp(u1, costhetamax, 1.0f);
        final float sintheta = (float)Math.sqrt(1.0f - costheta * costheta);
        final float phi = u2 * 2.0f * (float)Math.PI;
        
        return new Vector(
                (float)Math.cos(phi) * sintheta,
                (float)Math.sin(phi) * sintheta,
                costheta);
    }
    
    public static Vector uniformSampleCone(float u1, float u2,
            float costhetamax, Vector x, Vector y, Vector z) {
        
        Vector v = uniformSampleCone(u1, u2, costhetamax);
        
        return new Vector(
                x.x * v.x + y.x * v.y + z.x * v.z,
                x.y * v.x + y.y * v.y + z.y * v.z,
                x.z * v.x + y.z * v.y + z.z * v.z);
    }
    
    public static float uniformConePDF(float costhetamax) {
        return 1.0f / (2.0f * (float)Math.PI * (1.0f - costhetamax));
    }
    
    /**
     * Konstruiert zu einem Vektor zwei Vektoren, die senkrecht
     * auf diesem stehen.
     *
     * @param v der gegebene Vektor
     * @return ein zweielementiges Array mit den senkrechten Vektoren
     */
    public static Vector[] coordinateSystem(final Vector v) {
        Vector v2, v3;
        
        if (Math.abs(v.x) > Math.abs(v.y)) {
            float invLen = 1.0f / (float)Math.sqrt(v.x*v.x + v.z*v.z);
            v2 = new Vector(-v.z * invLen, 0.0f, v.x * invLen);
        } else {
            float invLen = 1.0f / (float)Math.sqrt(v.y*v.y + v.z*v.z);
            v2 = new Vector(0.0f, v.z * invLen, -v.y * invLen);
        }
        
        v3 = v.cross(v2);
        return new Vector[] { v2, v3 };
    }

    public static Vector cosineSampleHemisphere(float u1, float u2,
            Vector x, Vector y, Vector z) {

        final Vector v = cosineSampleHemisphere(u1, u2);
        return new Vector(
                x.x * v.x + y.x * v.y + z.x * v.z,
                x.y * v.x + y.y * v.y + z.y * v.z,
                x.z * v.x + y.z * v.y + z.z * v.z);
    }
    
    public static Vector cosineSampleHemisphere(float u1, float u2) {
        final float[] u = concentricSampleDisk(u1, u2);
        return new Vector(u[0], u[1],
                (float)Math.sqrt(Math.max(0.0f, 1.0f - u[0]*u[0] - u[1]*u[1])));
    }
    
    public static float[] concentricSampleDisk(float u1, float u2) {
        float r, theta;
        
        /* auf Intervall [-1,1] abbilden */
        float sx = 2.0f * u1 - 1.0f;
        float sy = 2.0f * u2 - 1.0f;
        
        /* Sonderfall Ursprung */
        if (sx == 0.0 && sy == 0.0) {
            return new float[] { 0, 0 };
        }
        
        if (sx >= -sy) {
            if (sx > sy) {
                /* erster Abschnitt */
                r = sx;
                if (sy > 0.0) theta = sy/r;
                else theta = 8.0f + sy/r;
            } else {
                /* zweiter Abschnitt */
                r = sy;
                theta = 2.0f - sx/r;
            }
        } else {
            if (sx <= sy) {
                /* dritter Abschnitt */
                r = -sx;
                theta = 4.0f - sy/r;
            } else {
                /* vierter Abschnitt */
                r = -sy;
                theta = 6.0f + sx/r;
            }
        }
        
        theta *= (float)Math.PI / 4.0f;
        
        return new float[] {
            r * (float)Math.cos(theta),
            r * (float)Math.sin(theta)
        };
    }
    
    public static float absdot(final Vector v1, final Vector v2) {
        return Math.abs(v1.dot(v2));
    }
    
    /**
     * x, y, z definieren Koordinatensystem
     *
     * @param sint sin(theta)
     * @param cost cos(theta)
     * @param phi Winkel phi
     * @param x
     * @param y
     * @param z 
     * @return der Vektor von (0, 0, 0) in die entsp. Richtung
     */
    public static Vector sphericalDirection(float sint, float cost, float phi,
            Vector x, Vector y, Vector z) {
        
        return x.mul(sint * (float)Math.cos(phi)).add(
                y.mul(sint * (float)Math.sin(phi))).add(
                z.mul(cost));
    }
    
    /**
     * Gibt zu (phi, theta) (Kugelkoordinaten) die Entsprechende
     * Richtung in kartesischen Koordinaten zurück. Das Ergebnis
     * wiederholt sich periodisch, Intervall für phi ist 0 <= phi <= 2*Pi.
     * Theta in 0 <= theta <= Pi.
     *
     * @param sint sin(&lt;Winkel theta&gt;)
     * @param cost cos(&lt;Winkel theta&gt;)
     * @param phi der Winkel phi
     * @return die Richtung in der Einheitskugel, normalisiert
     */
    public static Vector sphericalDirection(float sint, float cost, float phi) {
        return new Vector(
                sint * (float)Math.cos(phi),
                sint * (float)Math.sin(phi),
                cost);
    }
    
    public static double clamp(double x, double min, double max) {
        return min(max, max(x, min));
    }
    
    public static float clamp(float x, float min, float max) {
        return min(max, max(x, min));
    }
    
    public static float sphericalPhi(final Vector v) {
        float p = (float)Math.atan2(v.y, v.x);
        return (p < 0.0f) ? p + 2.0f * (float)Math.PI : p;
    }
    
    public static float sphericalTheta(final Vector v) {
        return (float)Math.acos(clamp(v.z, -1.0f, 1.0f));
    }
    
    public static double radicalInverse(int n, int base) {
        double val = 0;
        double invBase = 1.0 / base, invBi = invBase;
        while (n > 0) {
            /* compute next digit of radical inverse */
            int d_i = (n % base);
            val += d_i * invBi;
            n /= base;
            invBi *= invBase;
        }
        return val;
    }
    
}
