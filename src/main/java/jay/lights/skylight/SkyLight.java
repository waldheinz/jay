/*
 * SkyLight.java
 *
 * Created on 30. Januar 2006, 22:04
 */

package jay.lights.skylight;

import jay.lights.*;
import jay.maths.*;
import jay.scene.Scene;
import jay.utils.Spectrum;
import static java.lang.Math.*;
import static jay.lights.skylight.SunConstants.*;

/**
 * @author Matthias Treydte
 */
public class SkyLight extends Light {
    
    //public final static float SCALE_FACTOR = 0.00010f;
    
    float latitude;
    float longitude;
    int julianDay;
    float timeOfDay;
    float standardMeridian;
    int turbidity;
    float V;
    float sunSolidAngle;
    Spectrum sunSpectralRad;
    
    /** Phi (Sonne) */
    float phiS;
    
    /** Theta (Sonne) */
    float thetaS;
    
    Vector toSun;
    
    float zenith_Y;
    
    float zenith_x, zenith_y;
    
    float[] perez_Y;
    
    float[] perez_x, perez_y;
    
    DistantLight sunLight;
    
    IrregularSpectralCurve k_oCurve =
          new IrregularSpectralCurve(k_oAmplitudes, k_oWavelengths);
    IrregularSpectralCurve k_gCurve =
          new IrregularSpectralCurve(k_gAmplitudes, k_gWavelengths);
    IrregularSpectralCurve k_waCurve =
          new IrregularSpectralCurve(k_waAmplitudes, k_waWavelengths);
    RegularSpectralCurve   solCurve =
          new RegularSpectralCurve(solAmplitudes, 380, 750);  // every 10 nm  IN WRONG UNITS

    
    public SkyLight() {
        this(51, 11, 0, 150, 15.50f, 3);
    }
    
    /**
     *
     * @param lat Breitengrad (0..360)
     * @param longi Längengrad (0..180)
     * @param sm Standard Meridian
     * @param jd Tag (Julianischer Kalender)
     * @param tod Zeit (0.0,23.99) 14.25 = 14:15 Uhr
     * @param turb Tr�bung (1.0,30+) 2-6 für klare Tage
     */
    public SkyLight(float lat, float longi, int sm,
          int jd, float tod, int turb) {
        
        latitude = lat;
        longitude = longi;
        standardMeridian = sm * 15;
        julianDay = jd;
        timeOfDay = tod;
        turbidity = turb;
        
        V = 4.0f; /* Junge's Exponent */
        
        initSunThetaPhi();
        
        toSun = Utils.sphericalDirection(
              (float)sin(thetaS), (float)cos(thetaS), phiS);
        
        sunSpectralRad =  computeAttenuatedSunlight(thetaS, turbidity);
        sunSolidAngle =  (float)(0.25*PI*1.39*1.39/(150*150));  // = 6.7443e-05
        
        float theta2 = thetaS * thetaS;
        float theta3 = theta2 * thetaS;
        float T = turb;
        float T2 = turb * turb;
        
        float chi = (float)((4.0/9.0 - T / 120.0) * (PI - 2 * thetaS));
        zenith_Y = (float)((4.0453 * T - 4.9710) * tan(chi) -
              .2155 * T + 2.4192);
        
        /* conversion from kcd/m^2 to cd/m^2 */
        zenith_Y *= 1000;
        
        zenith_x = (float)(
              (+0.00165*theta3 - 0.00374*theta2 + 0.00208*thetaS + 0)          * T2 +
              (-0.02902*theta3 + 0.06377*theta2 - 0.03202*thetaS + 0.00394) * T +
              (+0.11693*theta3 - 0.21196*theta2 + 0.06052*thetaS + 0.25885));
        
        zenith_y = (float)(
              (+0.00275*theta3 - 0.00610*theta2 + 0.00316*thetaS  + 0) * T2 +
              (-0.04214*theta3 + 0.08970*theta2 - 0.04153*thetaS  + 0.00515) * T +
              (+0.15346*theta3 - 0.26756*theta2 + 0.06669*thetaS  + 0.26688));
        
        perez_Y = new float[6];
        perez_Y[1] =    0.17872f * T - 1.46303f;
        perez_Y[2] =   -0.35540f * T + 0.42749f;
        perez_Y[3] =   -0.02266f * T + 5.32505f;
        perez_Y[4] =    0.12064f * T - 2.57705f;
        perez_Y[5] =   -0.06696f * T + 0.37027f;
        
        perez_x = new float[6];
        perez_x[1] =   -0.01925f * T - 0.25922f;
        perez_x[2] =   -0.06651f * T + 0.00081f;
        perez_x[3] =   -0.00041f * T + 0.21247f;
        perez_x[4] =   -0.06409f * T - 0.89887f;
        perez_x[5] =   -0.00325f * T + 0.04517f;
        
        perez_y = new float[6];
        perez_y[1] =   -0.01669f * T - 0.26078f;
        perez_y[2] =   -0.09495f * T + 0.00921f;
        perez_y[3] =   -0.00792f * T + 0.21023f;
        perez_y[4] =   -0.04405f * T - 1.65369f;
        perez_y[5] =   -0.01092f * T + 0.05291f;
        
        /* Sonne initialisieren */
        sunLight = new DistantLight(sunSpectralRad, 
              new Vector(toSun.x, toSun.z, toSun.y));
    }
    
    void initSunThetaPhi() {
        float solarTime = (float)(timeOfDay + (
              0.170 * sin(4 * PI * (julianDay - 80)/373) -
              0.129 * sin(2 * PI * (julianDay - 8)/355)) +
              (standardMeridian - longitude) / 15.0);
        
        float solarDeclination = (float)(0.4093 *
              sin(2 * PI * (julianDay - 81)/368));
        
        float solarAltitude = (float)asin(sin(toRadians(latitude)) *
              sin(solarDeclination) - cos(toRadians(latitude)) *
              cos(solarDeclination) * cos(PI * solarTime / 12));
        
        float opp = (float)(-cos(solarDeclination) * sin(PI * solarTime / 12));
        
        float adj = (float)(-(cos(toRadians(latitude)) *
              sin(solarDeclination) + sin(toRadians(latitude)) *
              cos(solarDeclination) * cos(PI * solarTime / 12)));
        
        float solarAzimuth = (float)atan2(opp, adj);
        
        phiS = -solarAzimuth;
        thetaS = (float)(PI / 2.0 - solarAltitude);
    }
    
    Spectrum computeAttenuatedSunlight(float theta, int turbidity) {
        
        /* Need a factor of 100 (done below) */
        float  data[] = new float[91];  // (800 - 350) / 5  + 1
        
        float beta = 0.04608365822050f * turbidity - 0.04586025928522f;
        float tauR, tauA, tauO, tauG, tauWA;
        
        /* Relative Optical Mass */
        float m = 1.0f / (float)(cos(theta) + 0.15 *
              pow(93.885-theta / PI*180.0,-1.253));
        
        /* equivalent
           float m = 1.0/(cos(theta) + 0.000940 * pow(1.6386 - theta,-1.253));  // Relative Optical Mass
         */
        
        int i;
        float lambda;
        for(i = 0, lambda = 350; i < 91; i++, lambda+=5) {
            // Rayleigh Scattering
            // Results agree with the graph (pg 115, MI) */
            tauR = (float)exp( -m * 0.008735 * pow(lambda/1000, -4.08f));
            
            // Aerosal (water + dust) attenuation
            // beta - amount of aerosols present
            // alpha - ratio of small to large particle sizes. (0:4,usually 1.3)
            // Results agree with the graph (pg 121, MI)
            final float alpha = 1.3f;
            /* lambda should be in um */
            tauA = (float)exp(-m * beta * pow(lambda/1000, -alpha));
            
            // Attenuation due to ozone absorption
            // lOzone - amount of ozone in cm(NTP)
            // Results agree with the graph (pg 128, MI)
            final float lOzone = .35f;
            tauO = (float)exp(-m * k_oCurve.getValue(lambda) * lOzone);
            
            // Attenuation due to mixed gases absorption
            // Results agree with the graph (pg 131, MI)
            tauG = (float)exp(-1.41 * k_gCurve.getValue(lambda) * m /
                  pow(1 + 118.93 * k_gCurve.getValue(lambda) * m, 0.45));
            
            // Attenuation due to water vapor absorbtion
            // w - precipitable water vapor in centimeters (standard = 2)
            // Results agree with the graph (pg 132, MI)
            final float w = 2.0f;
            tauWA = (float)exp(-0.2385 * k_waCurve.getValue(lambda) * w * m /
                  pow(1 + 20.07 * k_waCurve.getValue(lambda) * w * m, 0.45));
            
            /* 100 comes from solCurve being in wrong units */
            data[i] = 100 * solCurve.getValue(lambda) * tauR * tauA *
                  tauO * tauG * tauWA;
            
        }
        
        /* Converts to Spectrum */
        return new RegularSpectralCurve(data, 350,800).getSpectrum();
    }
    
    float perezFunction(float[] lam, float theta, float gamma, float lvz) {
        float den = (float)(((1 + lam[1]*exp(lam[2])) *
              (1 + lam[3]*exp(lam[4]*thetaS) +
              lam[5]*cos(thetaS)*cos(thetaS))));
        
        float num = (float)(((1 + lam[1]*exp(lam[2]/cos(theta))) *
              (1 + lam[3]*exp(lam[4]*gamma) +
              lam[5]*cos(gamma)*cos(gamma))));
        
        return lvz * num / den;
    }
    
    Spectrum getSkySpectralRadiance(float theta, float phi) {
        float gamma = angleBetween(theta, phi, thetaS, phiS);
        
        /* Compute xyY values */
        float x = perezFunction(perez_x, theta, gamma, zenith_x);
        float y = perezFunction(perez_y, theta, gamma, zenith_y);
        float Y = perezFunction(perez_Y, theta, gamma, zenith_Y);
        
        /* nach CIE XYZ konvertieren */
        float X = x * (Y / y);
        float Z = (1 - x - y) * (Y / y);
        
        return Spectrum.fromXYZ(X, Y, Z);
    }
    
    public Spectrum power(Scene scene) {
        float worldRadius = scene.worldBounds().boundingSphereRadius();
        return sunSpectralRad.scale((float)Math.PI * 
              worldRadius * worldRadius);
    }
    
    public LightSample sample(Point p, Normal n, float u, float v) {
        
//        if (Utils.rand() > 0.5f) return sunLight.sample(p, n, u, v);
        
        LightSample smp = new LightSample();
        float xy[] = Utils.concentricSampleDisk(u, v);
        float z = (float)Math.sqrt(Math.max(0.0f, 1.0f -
              xy[0]*xy[0] - xy[0]*xy[0]));
        
        if (Utils.rand() < 0.5) z *= -1;
        
        Vector wi = new Vector(xy[0], xy[1], z);
        
        // Compute _pdf_ for cosine-weighted infinite light direction
        smp.pdf = Math.abs(z) * Utils.INV_2PI;
        
        // Transform direction to world space
        Vector v1, v2;
        Vector v1v2[] = Utils.coordinateSystem(n.normalized());
        v1 = v1v2[0]; v2 = v1v2[1];
        
        smp.wo = new Vector(
              v1.x * wi.x + v2.x * wi.y + n.x * wi.z,
              v1.y * wi.x + v2.y * wi.y + n.y * wi.z,
              v1.z * wi.x + v2.z * wi.y + n.z * wi.z);
        
        Ray r = new Ray(p, wi);
        smp.vt.init(r);
        smp.r = direct(r);
        return smp;
    }
    
    public float pdf(Point p, Vector wi) {
        return 1.0f / (4.0f * (float)Math.PI);
    }
    
    float angleBetween(float thetav, float phiv, float theta, float phi) {
        double cospsi = sin(thetav) * sin(theta) *
              cos(phi-phiv) + cos(thetav) * cos(theta);
        if (cospsi > 1) return 0;
        if (cospsi < -1) return (float)PI;
        return  (float)acos(cospsi);
    }
    
    @Override
    public Spectrum direct(Ray ray) {
        float theta, phi;
        
        Vector v = new Vector(ray.d);
        
        if (v.z < 0) return Spectrum.BLACK;
        if (v.z < 0.001f)
            v = new Vector(v.x,v.y,0.001f);
        
        theta = (float)acos(v.z);
        if (abs(theta) < 1e-5) phi = 0;
        else  phi = (float)atan2(v.y,v.x);
        
        return getSkySpectralRadiance(theta, phi);
    }
    
}
