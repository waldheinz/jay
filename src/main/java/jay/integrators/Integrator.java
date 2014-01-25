/*
 * Integrator.java
 *
 * Created on 15. Dezember 2005, 17:07
 */

package jay.integrators;

import jay.maths.*;
import jay.scene.Scene;
import jay.utils.*;

/**
 *
 * @author Matthias Treydte <waldheinz@gmail.com>
 */
public interface Integrator {
    
    public Spectrum traceRay(final Ray ray, Scene scene);
    
}
