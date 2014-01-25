
package jay.scene;

import jay.cameras.PerspectiveCamera;
import jay.lights.DistantLight;
import jay.lights.Light;
import jay.materials.BluePaint;
import jay.maths.AABB;
import jay.maths.Point;
import jay.maths.Transform;
import jay.maths.Vector;
import jay.scene.primitives.GeometricPrimitive;
import jay.scene.primitives.Primitive;
import jay.scene.primitives.geometry.Box;
import jay.scene.primitives.geometry.Disk;
import jay.utils.Spectrum;

/**
 *
 * @author Matthias Treydte <waldheinz@gmail.com>
 */
public class AutoScene extends Scene {

    public AutoScene(Primitive prim) {
        this(prim, true);
    }

    public AutoScene(Primitive prim, boolean areaLight) {
        addChild(prim);
        prim.setMaterial(new BluePaint());
        final AABB bounds = prim.worldBounds();
        System.out.println("bounds: " + bounds);
        final Point center = bounds.boundingSphereCenter();
        final float toBottom = bounds.min.y - center.y;

        /*
         * place a light straight above the primitive
         */
        Point pos = center.add(new Vector(0, bounds.h()*5, 0));
        if (areaLight) {
            Transform lTrans = Transform.translate(pos.vectorTo());
            lTrans = lTrans.compose(Transform.rotateX(90));
            Disk disk = new Disk(lTrans, false, 0,
                    bounds.boundingSphereRadius() / 2, 0);
            Primitive lPrim = new GeometricPrimitive(disk);
//            lPrim.makeLight(Spectrum.WHITE, this);
//            addChild(lPrim);
        } else {
            Light l = new DistantLight(Spectrum.WHITE, new Vector(0, 1, 0));
            addLight(l);
        }
        
        /*
         * add a camera
         */
        final Vector diag = bounds.diagonal();
        final float diagLen = diag.length();
        pos = center.add(new Vector(0, diagLen/2, diagLen/2));
        System.out.println("cam: " + pos);
        PerspectiveCamera c = new PerspectiveCamera();
        
        Transform t = Transform.translate(pos.vectorTo());//.
                //compose(Transform.rotateX(45));
        t = t.compose(Transform.rotateY(180));
        t = t.compose(Transform.rotateX(45));
        c.setFieldOfView(60);
        c.setTransform(t.getInverse());
        setCamera(c);

        /*
         * put it on a box
         */
        Box b = new Box(diag.x * 100, 1, diag.z * 100);
        t = Transform.translate(center.vectorTo());
        t = t.compose(Transform.translate(new Vector(0, toBottom - 0.5f, 0)));
        b.setTransform(t);
        GeometricPrimitive gp = new GeometricPrimitive(b);
//        gp.setMaterial(new MaterialClay());
        addChild(gp);

        /*
         * front wall
         */
        b = new Box(bounds.w() * 3, bounds.h() * 3, 1);
        t = Transform.translate(center.add(
                new Vector(0, center.y, bounds.d() * 3)).vectorTo());
        b.setTransform(t);
        gp = new GeometricPrimitive(b);
//        addChild(gp);

        /*
         * left wall
         */
        b = new Box(1, bounds.h() * 3, bounds.d() * 3);
        b.setTransform(t);
        t = Transform.translate(center.add(
                new Vector(-bounds.w() * 3, center.y, 0)).vectorTo());
        gp = new GeometricPrimitive(b);
//        addChild(gp);

    }
}
