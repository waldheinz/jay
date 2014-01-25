/*
 * RenderThread.java
 *
 * Created on 21. Juni 2007, 15:55
 */

package jay.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Logger;
import jay.cameras.Camera;
import jay.integrators.SurfaceIntegrator;
import jay.maths.Ray;
import jay.sampling.Film;
import jay.sampling.ImageSample;
import jay.sampling.Stratified2D;
import jay.sampling.buckets.ImagePlaneSampler;
import jay.scene.Scene;

/**
 *
 * @author Matthias Treydte <waldheinz@gmail.com>
 */
public class RenderThread implements Callable<List<ImageSample>> {
    
    /**
     * The number of samples we gather before flushing
     * the cache to the {@link Film}.
     */
    public final static int SAMPLE_GOAL = 1000;
    
    /**
     * The cache for gathered image samples.
     */
    private final ArrayList<ImageSample> samples;
    
    /**
     * The scene this thread renders.
     */
    private final Scene scene;
    private final Film film;
    private final Camera cam;
    private final ImagePlaneSampler sampler;
    
    private static Logger log = Logger.getLogger(RenderThread.class.getName());
    private SurfaceIntegrator integrator;
    
    /**
     * Creates a new RenderThread for the given scene.
     * 
     * @param scene the scene to render.
     * @param sampler 
     */
    public RenderThread(Scene scene, ImagePlaneSampler sampler) {
        this.scene = scene;
        this.integrator = this.scene.getSurfaceIntegrator();
        this.sampler = sampler;
        this.cam = this.scene.getCamera();
        this.film = this.cam.getFilm();
        this.samples = new ArrayList<ImageSample>(SAMPLE_GOAL);
    }
    
    public List<ImageSample> call() throws Exception {
        Stratified2D offset = new Stratified2D(2, 2);

        float[] off = new float[2];
        int[] pixelXY = new int[2];

        while (true) {
            ImagePlaneSampler.Bucket b = sampler.nextBucket();

            while (b.nextPixel(pixelXY)) {
                offset.reset();

                while (offset.nextSample(off)) {
                    float fx = (float)pixelXY[0] + off[0] - 0.5f;
                    float fy = (float)pixelXY[1] + off[1] - 0.5f;

                    Ray ray = cam.fireRay(fx, fy);
                    Spectrum li = integrator.traceRay(ray, scene);
                    samples.add(new ImageSample(fx, fy, ray, li));
                }
            }
        }
    }
}
