/*
 * PhotonMapIntegrator.java
 *
 * Created on 1. März 2006, 18:29
 */

package jay.integrators.photonmap;

import java.util.ArrayList;
import jay.integrators.*;
import jay.lights.Light;
import jay.lights.LightRaySample;
import jay.materials.*;
import jay.materials.bxdfs.BxDF;
import jay.maths.*;
import jay.scene.primitives.Intersection;
import jay.sampling.Film;
import jay.scene.Scene;
import jay.utils.kdtree.KdTree;
import jay.utils.Spectrum;
import jay.utils.statistics.StatsCounter;
import jay.utils.statistics.StatsRatio;

/**
 *
 * @author trem
 */
public class PhotonMapIntegrator extends ClassicSurfaceIntegrator {
    
    int[] gatherSampleOffset = new int[2];
    int[] gatherComponentOffset = new int[2];
    int nCausticPhotons, nIndirectPhotons, nDirectPhotons;
    int nLookup;
    int specularDepth;
    int maxSpecularDepth;
    float maxDistSquared, rrTreshold;
    boolean finalGather;
    float cosGatherAngle;
    int gatherSamples;
    int nDirectPaths, nCausticPaths, nIndirectPaths;
    private StatsRatio photonsPerLookup = 
            new StatsRatio("Photons found per lookup");
    
    KdTree<Photon> directMap;
    KdTree<Photon> causticMap;
    KdTree<Photon> indirectMap;
    
    public PhotonMapIntegrator(Film film) {
        this(2000, 10000, 10000, 50, 5, 10.0f, false, 32, 0.05f, 10.0f, film);
    }
    
    public PhotonMapIntegrator(SurfaceIntegrator old) {
        this(old.getFilm());
    }
    
    /** Creates a new instance of PhotonMapIntegrator */
    public PhotonMapIntegrator(int ncaus, int nindir, int ndir, int nLookup,
            int mdepth, float maxdist, boolean finalGather, int gatherSamples,
            float rrt, float ga, Film film) {
        
        super(film);
        
        this.nCausticPhotons = ncaus;
        this.nIndirectPhotons = nindir;
        this.nDirectPhotons = ndir;
        this.nLookup = nLookup;
        this.maxSpecularDepth = mdepth;
        this.maxDistSquared = maxdist * maxdist;
        this.specularDepth = 0;
        this.finalGather = finalGather;
        this.gatherSamples = gatherSamples;
        this.rrTreshold = rrt;
        this.cosGatherAngle = (float)Math.cos(Math.toRadians(ga));
    }
    
    public Spectrum traceRay(final Ray ray, Scene s) {
        Intersection is = s.nearestIntersection(ray);
        if (is == null) return Spectrum.BLACK;
        Spectrum L = Spectrum.BLACK;
        
        Vector wo = ray.d.neg();
        
        /* Strahl hat Flächenlight getroffen? */
//        L = L.add(is.le(wo));
        
        /* BSDF auswerten */
        BSDF bsdf = is.getBSDF();
        final Point p = is.dg.p;
        final Normal n = is.dg.nn;
        
        /* direkte Beleuchtung */
        if (false) {
            /* direkte Beleuchtung aus Photonen - Map */
            L = L.add(lPhoton(directMap, nDirectPaths, nLookup,
                    bsdf, is, wo, maxDistSquared));
        } else {
            /* klassische direkte Beleuchtung */
            L = L.add(uniformSampleOneLight(s, wo, is.dg, bsdf));
        }
        
        if (finalGather) {
            /* indirekte Beleuchtung mit final gathering */
            
        } else {
            /* indirekte Beleuchtung aus Photonmap */
//             L = L.add(lPhoton(indirectMap, nIndirectPaths, nLookup,
//                                 bsdf, is, wo, maxDistSquared));
        }
        
        return L;
    }
    
    public void prepare(Scene scene, Film film) {
        super.prepare(scene, film);
        
        if (scene.getLights().size() == 0) return;
        
        ArrayList<Photon> causticPhotons =
                new ArrayList<Photon>(nCausticPhotons);
        ArrayList<Photon> directPhotons =
                new ArrayList<Photon>(nDirectPhotons);
        ArrayList<Photon> indirectPhotons =
                new ArrayList<Photon>(nIndirectPhotons);
        
        boolean causticDone = false;
        boolean directDone = false;
        boolean indirectDone = false;
        StatsCounter nshot = new StatsCounter("Photons shot");
        StatsCounter directFound = new StatsCounter("Direct Photons");
        StatsCounter indirectFound = new StatsCounter("Indirect Photons");
        StatsCounter causticFound = new StatsCounter("Caustic Photons");
        
        while (!causticDone || !directDone || !indirectDone) {
            nshot.increment();
            if (nshot.get() > 200000 &&
                    (unsuccessful(nCausticPhotons,
                    causticPhotons.size(),
                    nshot.get()) ||
                    unsuccessful(nDirectPhotons,
                    directPhotons.size(),
                    nshot.get()) ||
                    unsuccessful(nIndirectPhotons,
                    indirectPhotons.size(),
                    nshot.get()))) {
                System.out.println("Unable to store enough photons.  Giving up.\n");
                return;
            }
            
            float[] u = new float[4];
            u[0] = (float)Utils.radicalInverse((int)nshot.get() + 1, 2);
            u[1] = (float)Utils.radicalInverse((int)nshot.get() + 1, 3);
            u[2] = (float)Utils.radicalInverse((int)nshot.get() + 1, 5);
            u[3] = (float)Utils.radicalInverse((int)nshot.get() + 1, 7);
            
            /* Licht wählen, von dem aus geschossen wird */
            int nLights = scene.getLights().size();
            int lightNum = Math.min((int)Math.floor(nLights *
                    (float)Utils.radicalInverse((int)nshot.get()+1, 11)),
                    nLights-1);
            Light light = scene.getLight(lightNum);
            float lightPdf = 1.0f / nLights;
            
            /* Lichtstrahl erstellen */
            
            LightRaySample lrs = light.sampleRay(scene, u[0], u[1], u[2], u[3]);
            Spectrum alpha = lrs.l;
            Ray photonRay = lrs.ray;
            float pdf = lrs.pdf;
            
            if (pdf == 0.0f) continue;
            alpha = alpha.scale(1.0f / (pdf * lightPdf));
            if (alpha.isBlack()) continue;
            
            int nIntersections = 0;
            boolean specularPath = false;
            Intersection photonIs = scene.nearestIntersection(photonRay);
            
            while (photonIs != null) {
                ++nIntersections;
                Vector wo = photonRay.d.neg();
                BSDF photonBSDF = photonIs.getBSDF();
                int specularType = BxDF.SPECULAR;
                boolean hasNonSpecular = (photonBSDF.numComponents() >
                        photonBSDF.matchingComponents(specularType));
                
                if (hasNonSpecular) {
                    /* Photon merken */
                    
                    Photon photon = new Photon(photonIs.dg.p, alpha, wo);
                    if (nIntersections == 1) {
                        /* direkte Beleuchtung */
                        if (!directDone) {
                            directPhotons.add(photon);
                            directFound.increment();
                            if (directPhotons.size() == nDirectPhotons) {
                                directDone = true;
                                nDirectPaths = nshot.get();
                                directMap = new KdTree<Photon>(directPhotons);
                            }
                        }
                    } else if (specularPath) {
                        /* Caustic - Photon */
                        if (!causticDone) {
                            causticPhotons.add(photon);
                            causticFound.increment();
                            if (causticPhotons.size() == nCausticPhotons) {
                                causticDone = true;
                                nCausticPaths = nshot.get();
                                causticMap =
                                        new KdTree<Photon>(causticPhotons);
                            }
                        }
                    } else {
                        /* indirekte Beleuchtung */
                        if (!indirectDone) {
                            indirectPhotons.add(photon);
                            indirectFound.increment();
                            if (indirectPhotons.size() == nIndirectPhotons) {
                                indirectDone = true;
                                nIndirectPaths = nshot.get();
                                indirectMap =
                                        new KdTree<Photon>(indirectPhotons);
                            }
                        }
                    }
                }
                
                /* neue Richtung bestimmen */
                float u1, u2, u3;
                if (nIntersections == 1) {
                    u1 = (float)Utils.radicalInverse(nshot.get()+1, 13);
                    u1 = (float)Utils.radicalInverse(nshot.get()+1, 17);
                    u1 = (float)Utils.radicalInverse(nshot.get()+1, 19);
                } else {
                    u1 = Utils.rand();
                    u2 = Utils.rand();
                    u3 = Utils.rand();
                }
                
                BxDFSample smp = photonBSDF.sample(wo, u1, u1, BxDF.ALL);
                if (smp.f.isBlack() || smp.pdf == 0.0f) break;
                
                specularPath = (nIntersections==1 || specularPath) &&
                        ((smp.type & BxDF.SPECULAR) != 0);
                
                alpha = alpha.scale(smp.f.scale(
                        Utils.absdot(smp.wi, photonIs.dg.nn) / smp.pdf));
                
                photonRay = new Ray(photonIs.dg.p, smp.wi);
                
                /* russisch Roulette */
                if (nIntersections > 3) {
                    float continueProbability = 0.5f;
                    if (Utils.rand() > continueProbability) break;
                    alpha = alpha.scale(1.0f / continueProbability);
                }
                
                photonIs = scene.nearestIntersection(photonRay);
            }
            
        }
        
    }
    
    Spectrum lPhoton(
            KdTree<Photon> map,
            int nPaths, int nLookup, BSDF bsdf,
            Intersection isect, Vector wo,
            float maxDistSquared) {
        
        if (map == null) return Spectrum.BLACK;
        
        Spectrum L = Spectrum.BLACK;
        
        int nonSpecular = BxDF.DIFFUSE | BxDF.GLOSSY;
        
        if (bsdf.matchingComponents(nonSpecular) == 0)
            return L;
        
        PhotonProcess proc = new PhotonProcess(nLookup, isect.dg.p);
        proc.photons = new ClosePhoton[nLookup];
        
        map.lookup(isect.dg.p, proc, maxDistSquared);
        
        photonsPerLookup.add(1, proc.foundPhotons);
        
        float scale = 1.0f / (nPaths * maxDistSquared * (float)Math.PI);
        // Estimate reflected light from photons
        ClosePhoton[] photons = proc.photons;
        int nFound = proc.foundPhotons;
        Normal Nf = wo.dot(isect.dg.nn) < 0 ? isect.dg.nn.neg() : isect.dg.nn;

        if (bsdf.matchingComponents(BxDF.GLOSSY) > 0) {
            /* glossy Oberfläche */
            for (int i=0; i < nFound; i++) {
                
                L = L.add(bsdf.eval(wo, photons[i].photon.wi).scale(
                        photons[i].photon.alpha.scale(scale)));
            }
            
        } else {
            /* diffuse Oberfläche */
            Spectrum Lr = Spectrum.BLACK, Lt = Spectrum.BLACK;
            //System.out.println("nicht Diffus!!");
            for (int i=0; i < nFound; i++) {
                if (Nf.dot(photons[i].photon.wi) > 0.0f) {
                    Lr = Lr.add(photons[i].photon.alpha);
                } else {
                    Lt = Lt.add(photons[i].photon.alpha);
                }
            }
            
            //            L += (scale * INV_PI) * (Lr * bsdf->rho(wo, BSDF_ALL_REFLECTION) +
//                    Lt * bsdf->rho(wo, BSDF_ALL_TRANSMISSION));
            
        }
        
        return L;
    }
    
    boolean unsuccessful(int needed, int found, int shot) {
        return (found < needed && (found == 0 || found < shot / 1024));
    }
}
