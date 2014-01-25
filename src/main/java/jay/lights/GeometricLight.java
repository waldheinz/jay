/*
 * GeometricLight.java
 *
 * Created on 23. Dezember 2005, 18:47
 */

package jay.lights;

import java.util.Stack;
import jay.maths.*;
import jay.scene.primitives.geometry.Geometry;
import jay.scene.primitives.geometry.GeometrySample;
import jay.scene.primitives.geometry.GeometrySet;
import jay.scene.Scene;
import jay.utils.*;

/**
 *
 * @author trem
 */
public final class GeometricLight extends Light {
    
    Geometry geometry;
    Spectrum lEmit;
    
    /** Creates a new instance of GeometricLight */
    public GeometricLight(Geometry g, Spectrum i) {
        lEmit = i;
        
        if (g.canIntersect()) {
            geometry = g;
        } else {
            GeometryList done = new GeometryList();
            GeometryList todo = new GeometryList();
            todo.add(g);
            
            while (todo.size() > 0) {
                Geometry c = todo.get(todo.size() - 1);
                todo.remove(todo.size()-1);
                
                if (c.canIntersect()) done.add(c);
                else c.refine(todo);
            }
      
            geometry = new GeometrySet(done);
        }
    }
    
    public Spectrum power(Scene scene) {
        return lEmit.scale(geometry.getArea() * (float)Math.PI);
    }
    
    public LightSample sample(Point p, Normal n, float u, float v) {
        LightSample sample =  new LightSample();
        
        GeometrySample gs = geometry.sample(u, v, p);
        
        sample.deltaLight = isDeltaLight();
        sample.wo = gs.p.sub(p).normalized();
        sample.pdf = geometry.pdf(p, sample.wo);
        sample.vt.init(gs.p, p);
        sample.r = l(gs.p, gs.n, sample.wo.neg());
        
        return sample;
    }
    
    /**
     * 492
     */
    public Spectrum l(Point p, Normal n, Vector w) {
        if (n.dot(w) > 0.0f) return lEmit;
        else return Spectrum.BLACK;
    }
    
    public float pdf(Point p, Normal n, Vector wi) {
        return geometry.pdf(p, wi);
    }
    
    public float pdf(Point p, Vector wi) {
        return geometry.pdf(p, wi);
    }
    
    public LightRaySample sampleRay(Scene scene,
            float u1, float u2, float u3, float u4) {
        
        LightRaySample lrs = new LightRaySample();
        GeometrySample gs = geometry.sample(u1, u2);
        Normal ns = gs.n;
        lrs.ray = new Ray(gs.p, Utils.uniformSampleSphere(u3, u4));
        if (lrs.ray.d.dot(ns) < 0.0f) 
            lrs.ray = lrs.ray.setDirection(lrs.ray.d.neg());
        
        lrs.pdf = geometry.pdf(lrs.ray.o) * Utils.INV_2PI;
        lrs.l = this.l(lrs.ray.o, ns, lrs.ray.d);
        
        return lrs;
    }

    public float getArea(final Scene scene) {
        return geometry.getArea();
    }
    
}
