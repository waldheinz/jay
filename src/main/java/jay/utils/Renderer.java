
package jay.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import jay.scene.Scene;

/**
 *
 * @author Matthias Treydte &lt;waldheinz at gmail.com&gt;
 */
public class Renderer implements Runnable {
    private final Scene scene;
    private final ExecutorService exec = Executors.newFixedThreadPool(2);

    public Renderer(Scene scene) {
        this.scene = scene;
        
    }

    public void run() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
//    private Runnable 
}
