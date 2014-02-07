
package jay.scene.primitives.accelerators;

import java.io.File;
import jay.maths.AABB;
import jay.maths.Ray;
import jay.maths.Transform;
import jay.maths.Vector;
import jay.scene.primitives.GeometricPrimitive;
import jay.scene.primitives.Group;
import jay.scene.primitives.Intersection;
import jay.scene.primitives.geometry.Sphere;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Matthias Treydte &lt;waldheinz@gmail.com&gt;
 */
public class KdTreeAcceleratorTest {
    
    public KdTreeAcceleratorTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of intersects method, of class KdTreeAccelerator.
     */
    @Test
    public void testIntersects() {
        System.out.println("intersects");
        Ray ray = null;
        KdTreeAccelerator instance = null;
        boolean expResult = false;
        boolean result = instance.intersects(ray);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of nearestIntersection method, of class KdTreeAccelerator.
     */
    @Test
    public void testNearestIntersection() {
        System.out.println("nearestIntersection");
        Ray ray = null;
        KdTreeAccelerator instance = null;
        Intersection expResult = null;
        Intersection result = instance.nearestIntersection(ray);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of rebuild method, of class KdTreeAccelerator.
     */
    @Test
    public void testRebuild() {
        System.out.println("rebuild");
        
        final Group grp = new Group();
        
        Sphere s = new Sphere(1);
        s.setTransform(Transform.translate(new Vector(0, -5, 0)));
        grp.addChild(new GeometricPrimitive(s));
        
        s = new Sphere(1);
        s.setTransform(Transform.translate(new Vector(0, 5, 0)));
        grp.addChild(new GeometricPrimitive(s));
        
        KdTreeAccelerator instance = new KdTreeAccelerator(grp);
        instance.rebuild();
        
        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of writeTree method, of class KdTreeAccelerator.
     */
    @Test
    public void testWriteTree() throws Exception {
        System.out.println("writeTree");
        File file = null;
        KdTreeAccelerator instance = null;
        instance.writeTree(file);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of worldBounds method, of class KdTreeAccelerator.
     */
    @Test
    public void testWorldBounds() {
        System.out.println("worldBounds");
        KdTreeAccelerator instance = null;
        AABB expResult = null;
        AABB result = instance.worldBounds();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
