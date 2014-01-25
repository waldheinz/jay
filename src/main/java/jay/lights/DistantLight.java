/*
 * DistantLight.java
 *
 * Created on 31. Januar 2006, 19:26
 */

package jay.lights;

import jay.maths.Normal;
import jay.maths.Point;
import jay.maths.Ray;
import jay.maths.Utils;
import jay.maths.Vector;
import jay.scene.Scene;
import jay.utils.Spectrum;

/**
 * @author Matthias Treydte
 */
public class DistantLight extends Light {
    
    Spectrum L;
    Vector lightDir;
    
    public DistantLight() {
        this(Spectrum.WHITE, new Vector(0,0,1));
    }
    
    public DistantLight(Spectrum r, Vector dir) {
        L = r;
        lightDir = dir.normalized();
    }
    
    public Spectrum power(Scene scene) {
        float worldRadius = scene.worldBounds().boundingSphereRadius();
        return L.scale((float)Math.PI * worldRadius * worldRadius);
    }
    
    public LightSample sample(Point p, Normal n, float u, float v) {
        LightSample ls = new LightSample();
        ls.pdf = 1.0f;
        ls.r = L;
        ls.wo = lightDir;
        ls.vt.init(new Ray(p, lightDir));
        ls.deltaLight = isDeltaLight();
        return ls;
    }
    
    public float pdf(Point p, Vector wi) {
        return 0.0f;
    }
    
    public boolean isDeltaLight() {
        return true;
    }
    
    public LightRaySample sampleRay(Scene scene, float u1, float u2,
            float u3, float u4) {
        
        LightRaySample lrs = new LightRaySample();
        
        Point worldCenter = scene.worldBounds().boundingSphereCenter();
        float worldRadius = scene.worldBounds().
                boundingSphereRadius(worldCenter);
        
        Vector[] v12 = Utils.coordinateSystem(lightDir);
        float[] d12 = Utils.concentricSampleDisk(u1, u2);
        
        Point pDisk = worldCenter.add(
                ((v12[0].mul(d12[0])).add(v12[1].mul(d12[1])))
                .mul(worldRadius));
        
        lrs.ray = new Ray(pDisk.add(lightDir.mul(worldRadius)),
                lightDir.neg());
        lrs.l = L;
        lrs.pdf = 1.0f / ((float)Math.PI * worldRadius * worldRadius);
        
        return lrs;
    }
    
}
