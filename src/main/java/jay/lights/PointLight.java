/*
 * PointLight.java
 *
 * Created on 16. Dezember 2005, 01:38
 */

package jay.lights;

import jay.maths.*;
import jay.scene.Scene;
import jay.utils.*;

/**
 * @author Matthias Treydte
 */
public final class PointLight extends Light {
    
    private Point position;
    private Spectrum intensity;

    public PointLight() {
        this(Spectrum.WHITE);
    }

    public PointLight(final Point pos) {
        this();
        setPosition(pos);
    }

    public PointLight(final Spectrum intensity) {
        this.position = l2w.apply(new Point(0.0f, 0.0f, 0.0f));
        this.intensity = intensity;
    }
    
    @Override
    public boolean isDeltaLight() { return true; }
    
    public Spectrum power(final Scene scene) {
        return intensity.scale(4.0f * (float)Math.PI);
    }
    
    @Override
    public LightRaySample sampleRay(final Scene scene,
            float u1, float u2, float u3, float u4) {
            
        LightRaySample lrs = new LightRaySample();
        
        lrs.l = intensity;
        lrs.pdf = 1.0f / (4.0f * (float)Math.PI);
        lrs.ray = new Ray(position, Utils.uniformSampleSphere(u1, u2));
        
        return lrs;
    }
    
    @Override
    public LightSample sample(final Point p, final Normal n, float u, float v) {
        LightSample sample = new LightSample();
        
        sample.deltaLight = isDeltaLight();
        sample.wo = position.sub(p).normalized();
        sample.pdf = 1.0f;
        sample.vt.init(p, position);
        sample.r = intensity.scale(Math.abs(sample.wo.dot(n)) /
                position.sub(p).lengthSquared());
        
        return sample;
    }
    
    /**
     * It is impossible for an arbitary ray to hit
     * a point light source, so this method returns
     * just 0.
     */
    public float pdf(final Point p, final Vector wi) {
        return 0.0f;
    }
    
    /**
     * Does <em>not</em> update the transformation! Main
     * use is for virtual point light sources.
     */
    public void setPosition(final Point position) {
        this.position = position;
    }
    
    public Point getPosition() {
        return position;
    }
    
    @Override
    public void setTransform(final Transform t) {
        super.setTransform(t);
        this.position = l2w.apply(new Point(0, 0, 0));
        System.out.println("point light pos: " + position);
    }
}
