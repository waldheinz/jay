/*
 * BidirPathIntegrator.java
 *
 * Created on 5. Januar 2006, 03:07
 */

package jay.integrators;

import java.util.ArrayList;
import java.util.List;
import jay.lights.Light;
import jay.lights.LightRaySample;
import jay.materials.BSDF;
import jay.materials.BxDFSample;
import jay.materials.bxdfs.BxDF;
import jay.maths.*;
import jay.sampling.Film;
import jay.scene.primitives.Intersection;
import jay.scene.Scene;
import jay.utils.*;

/**
 *
 * @author Matthias Treydte <waldheinz@gmail.com>
 */
public class BidirPathIntegrator extends ClassicSurfaceIntegrator {
    
    public final static int MAX_VERTS = 32;
   
    public BidirPathIntegrator(Film film) {
        super(film);
    }
    
    public BidirPathIntegrator(SurfaceIntegrator old) {
        this(old.film);
    }
    
    public Spectrum traceRay(final Ray ray, final Scene s) {
        SpectrumCalc L = new SpectrumCalc(Spectrum.BLACK);
        // Generate eye and light sub-paths
        List<Vertex> eyePath, lightPath;
        
        eyePath = generatePath(s, ray, MAX_VERTS);
        
        if (eyePath.size() == 0) {
            return L.s;
        }
        
        /* choose light source */
        int lightNum = (int)(Utils.rand() * s.getLights().size());
        lightNum = Math.min(lightNum, s.getLights().size() - 1);
        Light light = s.getLight(lightNum);
        float lightWeight = s.getLights().size();
        
        /* sample ray from light source to start light path */
        
        LightRaySample lrs = light.sampleRay(s,
                Utils.rand(), Utils.rand(),
                Utils.rand(), Utils.rand());
        
        if (lrs.pdf == 0.0f) return Spectrum.BLACK;
        Spectrum Le = new Spectrum(lightWeight / lrs.pdf);
        
        lightPath = generatePath(s, lrs.ray, MAX_VERTS);
        
        // Connect bidirectional path prefixes and evaluate throughput
        SpectrumCalc directWt = new SpectrumCalc(Spectrum.WHITE);
        for (int i = 1; i <= eyePath.size(); ++i) {
            // Handle direct lighting for bidirectional integrator
            directWt.scale(1.0f / eyePath.get(i-1).rrWeight);
            
            L.add(directWt.s.scale(uniformSampleOneLight(s,
                    eyePath.get(i-1).wi,
                    eyePath.get(i-1).p,
                    eyePath.get(i-1).ng,
                    eyePath.get(i-1).bsdf
                    ).scale(1.0f / weightPath(eyePath, i, lightPath, 0))));
            System.out.println(i);
            System.out.println(eyePath.size());
            directWt.scale(eyePath.get(i-1).bsdf.eval(
                    eyePath.get(i-1).wi, eyePath.get(i-1).wo));
            directWt.scale(Utils.absdot(eyePath.get(i-1).wo,
                    eyePath.get(i-1).ng));
            directWt.scale(1.0f / eyePath.get(i-1).bsdfWeight);
            
            for (int j = 1; j <= lightPath.size(); ++j) {
                L.add(Le.scale(evalPath(s, eyePath, i, lightPath, j).
                        scale(1.0f / weightPath(eyePath, i, lightPath, j))));
            }
            
        }
        
        return L.s;
    }
    
    private List<Vertex> generatePath(final Scene scene, final Ray r, int maxVerts) {
        Ray ray = new Ray(r);
        ArrayList<Vertex> vertices = new ArrayList<Vertex>(maxVerts);
        
        while (vertices.size() < maxVerts) {
            /* nächsten Schnittpunkt finden */
            Intersection isect = scene.nearestIntersection(ray);
            if (isect == null) break;
            
            final Vertex v = new Vertex();
            vertices.add(v);
            
            v.bsdf = isect.getBSDF();
            v.p = isect.dg.p;
            v.ng = isect.dg.nn;
            v.ns = v.bsdf.dgS.nn;
            v.wi = ray.d.neg();
            
            // Possibly terminate bidirectional path sampling
            if (vertices.size() > 3) {
                final float rrProb = .2f;
                if (Utils.rand() > rrProb) break;
                v.rrWeight = 1.f / rrProb;
            }
            
            /* den Strahl für das nächste Segment initialisieren */
            float u1 = Utils.rand();
            float u2 = Utils.rand();
            
            BxDFSample bs = v.bsdf.sample(v.wi, u1, u2, BxDF.ALL);
            v.wo = bs.wi;
            v.flags = bs.type;
            v.bsdfWeight = bs.pdf;
            final Spectrum fr = bs.f;
            if (fr.isBlack() && v.bsdfWeight == 0.0f) break;
            
            ray = new Ray(v.p, v.wo);
        }
        
        /* dAWeight initialisieren */
//        for (int i=0; i < vertices.size() - 1; ++i) {
//            final Vertex v = vertices.get(i);
//            v.dAWeight = v.bsdfWeight *
//                    Utils.absdot(v.wo.neg(), vertices.get(i+1).ng) /
//                    v.p.sub(vertices.get(i+1).p).lengthSquared();
//        }
//        
        return vertices;
    }
    
    Spectrum evalPath(final Scene scene,
            final List<Vertex> eye, int nEye,
            final List<Vertex> light, int nLight) {
        
        SpectrumCalc L = new SpectrumCalc(Spectrum.WHITE);
        final Vertex lEye = eye.get(nEye - 1);
        final Vertex lLight = light.get(nLight - 1);
        
        for (int i = 0; i < nEye-1; ++i) {
            Vertex e = eye.get(i);
            L.scale(e.bsdf.eval(e.wi, e.wo));
            L.scale(Utils.absdot(e.wo, e.ng));
            L.scale(1.0f / (e.bsdfWeight * e.rrWeight));
        }
        
        final Vector w = light.get(nLight-1).p.sub(eye.get(nEye-1).p).normalized();
        
        L.scale(lEye.bsdf.eval(lEye.wi, w));
        L.scale(G(lEye, lLight));
        L.scale(lLight.bsdf.eval(w.neg(), lLight.wi));
        L.scale(1.0f / (lEye.rrWeight * lLight.rrWeight));
        
        for (int i = nLight-2; i >= 0; --i) {
            Vertex l = light.get(i);
            L.scale(l.bsdf.eval(l.wi, l.wo));
            L.scale(Utils.absdot(l.wo, l.ng));
            L.scale(1.0f / (l.bsdfWeight * l.rrWeight));
        }
        
        if (L.s.isBlack()) return L.s;
        
        if (!visible(eye.get(nEye-1).p, light.get(nLight-1).p, scene))
            return Spectrum.BLACK;
        
        return L.s;
    }
    
    float weightPath(List<Vertex> eye, int nEye,
            List<Vertex> light, int nLight) {
        
        return (nEye + nLight);
    }
    
    /**
     * Entscheidet, ob p1 und p2 sich gegenseitig sehen können.
     *
     * @param p1 der erste Punkt
     * @param p2 der zweite Punkt
     * @param scene die Szene
     * @return ob p1 p2 sehen kann
     */
    static boolean visible(final Point p1, final Point p2, final Scene scene) {
        Ray r = new Ray(p1, p2.sub(p1), Ray.EPSILON, 1.0f - Ray.EPSILON);
        return !scene.intersects(r);
    }
    
    /**
     * Berechnet die geometrische Dämpfung zwischen v1.p und v2.p
     */
    static float G(final Vertex v1, final Vertex v2) {
        final Vector w = v2.p.sub(v1.p);
        final Vector wn = w.normalized();
        return Utils.absdot(v1.ng, wn) * Utils.absdot(v2.ng, wn.neg()) /
                w.lengthSquared();
    }
    
    protected static class Vertex {
        
        public BSDF bsdf;
        public Point p;
        public Normal ng, ns;
        public Vector wi, wo;
        public float bsdfWeight, /*dAWeight,*/ rrWeight;
        public int flags;
        
        public Vertex() {
            bsdfWeight = /*dAWeight = */0.0f; rrWeight = 1.0f;
            flags = 0; bsdf = null;
            wo = null;
        }
    }
}
