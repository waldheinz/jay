/*
 * InfiniteAreaLight.java
 *
 * Created on 4. Januar 2006, 01:37
 */

package jay.lights;

import jay.fileio.ImageData;
import jay.maths.*;
import jay.scene.Scene;
import jay.utils.Spectrum;

/**
 *
 * @author Matthias Treydze
 */
public class InfiniteAreaLight extends Light {
    
    /**
     * Grundhelligkeit
     */
    Spectrum lBase;
    
    ImageData map;
    
    /** Erstellt eine neue Instanz von InfiniteAreaLight */
    public InfiniteAreaLight(Spectrum r) {
        lBase = r;
        map = null;
    }
    
    public InfiniteAreaLight(ImageData map) {
        lBase = new Spectrum(1);
        this.map = map;
    }
    
    public Spectrum power(Scene scene) {
        float rad = scene.worldBounds().boundingSphereRadius();
        return lBase.scale((float)Math.PI * rad * rad);
    }
    
    public LightSample sample(Point p, Normal n, float u1, float u2) {
        LightSample smp = new LightSample();
        float xy[] = Utils.concentricSampleDisk(u1, u2);
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
    
    Spectrum mapLookup(Vector dir) {
        if (map == null) return Spectrum.WHITE;
        
        dir = w2l.apply(dir).normalized();
        
        float s = Utils.sphericalPhi(dir) * Utils.INV_2PI;
        float t = Utils.sphericalTheta(dir) * Utils.INV_PI;
        
        Spectrum l = map.getPixelBilin(s, t);
        return l;
    }
    
    public Spectrum direct(Ray ray) {
        return lBase.scale(mapLookup(ray.d));
    }
    
    public LightRaySample sampleRay(Scene scene, float u1, float u2, float u3, float u4) {
        LightRaySample lrs = new LightRaySample();
        
        // Choose two points _p1_ and _p2_ on scene bounding sphere
        Point worldCenter = scene.worldBounds().boundingSphereCenter();
        float worldRadius = scene.worldBounds().boundingSphereRadius(
              worldCenter);
        
        worldRadius *= 1.01f;
        Point p1 = worldCenter.add(Utils.uniformSampleSphere(u1, u2).mul(
              worldRadius));
        
        Point p2 = worldCenter.add(Utils.uniformSampleSphere(u3, u4).mul(
              worldRadius));
        
        // Construct ray between _p1_ and _p2_
        
        lrs.ray = new Ray(p1, p2.sub(p1).normalized());
        
        // Compute _InfiniteAreaLight_ ray weight
        Vector to_center = worldCenter.sub(p1).normalized();
        float costheta = Utils.absdot(to_center, lrs.ray.d);
        lrs.pdf = costheta /
              ((4.0f * (float)Math.PI * worldRadius * worldRadius));
        
        lrs.l = direct(new Ray(lrs.ray.o, lrs.ray.d.neg()));
        
        return lrs;
    }
    
}
