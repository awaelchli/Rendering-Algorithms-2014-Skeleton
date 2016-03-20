package rt.bsp;

import rt.BoundingBox;
import rt.HitRecord;
import rt.Intersectable;
import rt.Ray;
import rt.intersectables.Aggregate;

import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;
import java.util.Iterator;

/**
 * Created by adrian on 18.03.16.
 */
public class BSPAccelerator implements Intersectable {

    int maxDepth;
    int maxObjectsPerNode;

    BSPNode root;

    public BSPAccelerator(int maxDepth, int maxObjectsPerNode) {
        this.maxDepth = maxDepth;
        this.maxObjectsPerNode = maxObjectsPerNode;
    }

    public void construct(Aggregate objects) {

    }

    @Override
    public HitRecord intersect(Ray r) {
        return null;
    }

    @Override
    public BoundingBox getBoundingBox() {
        return null;
    }

    private float findSplitPlane(Aggregate objects, Axis axis) {

        BoundingBox bbox = null;

        Point3f centerOfMass = new Point3f();
        int numObjects = 0;
        Iterator<Intersectable> iterator = objects.iterator();
        while (iterator.hasNext()) {
            BoundingBox b = iterator.next().getBoundingBox();
            centerOfMass.add(b.center());
            numObjects++;
        }
        centerOfMass.scale(1f / numObjects);

        return Axis.getValueFromAxis(centerOfMass, axis);;
    }


}
