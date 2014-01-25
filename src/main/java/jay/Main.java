/*
 * Main.java
 *
 * Created on 9. Juni 2007, 20:13
 */

package jay;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import jay.fileio.XMLSceneReader;
import jay.sampling.ImageAdapter;
import jay.sampling.ImageFilm;
import jay.sampling.ImageFilmListener;
import jay.scene.Scene;
import jay.utils.RenderThread;
import jay.utils.statistics.Statistics;
import jay.utils.statistics.StatsObject;

/**
 *
 * @author Matthias Treydte <waldheinz@gmail.com>
 */
public class Main {
    
    private static Logger log = Logger.getLogger("main");
    
    public static void main(final String[] args) {
        
        if (args.length > 2 || args.length < 1) {
            System.err.println("Need one or two arguments");
            System.exit(-1);
        }
        
        XMLSceneReader sr;
        Scene s;
        
        try {
            InputStream is = new FileInputStream(new File(args[0]));
            sr = new XMLSceneReader(is);
            s = sr.getScene();
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
        
        s.prepare();
        
        final ImageAdapter ia = new ImageAdapter((ImageFilm)s.getCamera().getFilm());
        
        ((ImageFilm)s.getCamera().getFilm()).addListener(new ImageFilmListener() {
            public void filmUpdated(ImageFilm film) {
                try {
                    log.info("writing image to \"" + args[1] + "\"");
                    ia.update();
                    ImageIO.write(ia, "png", new File(args[1]));
                    logStatistics(log, Level.FINE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            
        });
        
        int threadCount = Runtime.getRuntime().availableProcessors();
//        threadCount = 1;
        
        log.info("Using " + threadCount + " threads.");
        
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
    
    public static void logStatistics(Logger log, Level l) {
        StringBuilder sb = new StringBuilder();
        
        for (StatsObject s : Statistics.getStats()) {
            sb.append(s.toString() + "\n");
        }
        
        log.log(l, sb.toString());
    }
    
}
