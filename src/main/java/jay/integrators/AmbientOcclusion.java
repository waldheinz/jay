
package jay.integrators;

import jay.materials.BSDF;
import jay.materials.BxDFSample;
import jay.materials.bxdfs.BxDF;
import jay.maths.Ray;
import jay.maths.Utils;
import jay.maths.Vector;
import jay.sampling.Film;
import jay.sampling.Stratified2D;
import jay.scene.Scene;
import jay.scene.primitives.Intersection;
import jay.utils.Spectrum;
import jay.utils.SpectrumCalc;

/**
 *
 *
 * @author Matthias Treydte <waldheinz@gmail.com>
 */
public class AmbientOcclusion extends ClassicSurfaceIntegrator {
    private final static int SAMPLES = 3;
    private final ThreadLocal<Stratified2D> sampler =
            new ThreadLocal<Stratified2D>() {
        
        @Override
        protected Stratified2D initialValue() {
            return new Stratified2D(SAMPLES, SAMPLES);
        }
    };

    public AmbientOcclusion(Film film) {
        super(film);
    }

    @Override
    public Spectrum traceRay(Ray ray, Scene scene) {
        Intersection is = scene.nearestIntersection(ray);
        if (is == null) return Spectrum.BLACK;
        final float sceneDiag = scene.worldBounds().diagonal().length();
        final float[] sample = new float[2];
        final SpectrumCalc result = new SpectrumCalc();
        final BSDF bsdf = is.getBSDF();
        final Vector wo = ray.d.neg();

        while (sampler.get().nextSample(sample)) {    
            BxDFSample smp = bsdf.sample(wo, sample[0], sample[1],
                    BxDF.ALL_REFLECTION);
            if (smp.pdf == 0.0f || smp.f.isBlack()) continue;

            final Ray testRay = new Ray(
                    is.dg.p, smp.wi, Utils.EPSILON * sceneDiag,
                    Float.MAX_VALUE);
            
            if (!scene.intersects(testRay))
                result.add(smp.f);
        }

        sampler.get().reset();
        return result.s;
    }

}
