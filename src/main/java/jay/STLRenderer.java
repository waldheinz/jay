
package jay;

import java.io.File;
import java.util.List;
import javax.imageio.ImageIO;
import jay.fileio.stl.StlMesh;
import jay.fileio.stl.StlTriangle;
import jay.integrators.AmbientOcclusion;
import jay.sampling.ImageAdapter;
import jay.sampling.ImageFilm;
import jay.sampling.ImageFilmListener;
import jay.scene.AutoScene;
import jay.scene.Scene;
import jay.scene.primitives.GeometricPrimitive;
import jay.scene.primitives.Primitive;
import jay.scene.primitives.geometry.TriangleMesh;
import jay.utils.RenderThread;

/**
 * Standalone version of Jay which can read STL files and renders them.
 *
 * @author Matthias Treydte <waldheinz at gmail.com>
 */
public class STLRenderer {

    /**
     * @param args the command line arguments
     */
    public static void main(final String[] args) {
        
        if (args.length != 1) {
            System.err.println(
                    "Need exactly one argument, which must be the " +
                    "name of a STL - file.");
            return;
        }

        List<StlTriangle> stlTris = null;

        try {
            stlTris = StlMesh.read(new File(args[0]));
        } catch (Exception ex) {
            System.err.println("Error reading " + args[0] + ": " + ex +
                    " -> It is not a valid STL-File.");
            return;
        }
        
        TriangleMesh mesh = StlMesh.getTriangleMesh(stlTris);
        Primitive prim = new GeometricPrimitive(mesh);

        Scene s = new AutoScene(prim);
        s.setSurfaceIntegrator(new AmbientOcclusion(s.getFilm()));
        s.prepare();
        
        final ImageAdapter ia = new ImageAdapter((ImageFilm)s.getCamera().getFilm());

        ((ImageFilm)s.getCamera().getFilm()).addListener(new ImageFilmListener() {
            public void filmUpdated(ImageFilm film) {
                try {
                    System.err.println("writing image to \"" + args[0] + "\"");
                    ia.update();
                    ImageIO.write(ia, "png", new File(args[0] + ".png"));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        int threadCount = Runtime.getRuntime().availableProcessors();
        System.err.println("Using " + threadCount + " threads.");

        Thread threads[] = new Thread[threadCount];
        for (int i=0; i < threadCount; i++) {
//            threads[i] = new RenderThread(s);
//            threads[i].start();
        }
        
        try {
            for (Thread t : threads)
                t.join();
        } catch (InterruptedException ex) {
            /* can live with that */
        }
    }
}
