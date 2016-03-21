package rt.bsp;

import rt.HitRecord;
import rt.Intersectable;
import rt.Ray;
import rt.intersectables.Aggregate;
import rt.intersectables.IntersectableList;

import javax.vecmath.Point3f;
import java.util.Iterator;

/**
 * Created by adrian on 18.03.16.
 */
public class BSPAccelerator implements Intersectable
{

    int maxDepth;
    int maxObjectsPerNode;

    BSPNode root;

    public BSPAccelerator(int maxDepth, int maxObjectsPerNode)
    {
        this.maxDepth = maxDepth;
        this.maxObjectsPerNode = maxObjectsPerNode;
    }

    public void construct(Aggregate objects)
    {
        root = buildTree(objects, objects.getBoundingBox(), Axis.X, 0);
    }

    private BSPNode buildTree(Aggregate objects, BoundingBox boundingBox, Axis currentAxis, float depth)
    {
        if (depth == maxDepth || objects.count() <= maxObjectsPerNode)
        {
            // Create leaf node
            BSPLeaf current = new BSPLeaf(boundingBox);
            IntersectableList list = new IntersectableList();
            Iterator<Intersectable> iterator = objects.iterator();
            while (iterator.hasNext())
            {
                Intersectable object = iterator.next();
                if (object.getBoundingBox().isIntersecting(boundingBox))
                {
                    list.add(object);
                }
            }
            current.objects = list;
            return current;
        }

        float splitPos = findSplitPlane(objects, currentAxis);

        BoundingBox leftBB = new BoundingBox();
        BoundingBox rightBB = new BoundingBox();

        boundingBox.split(currentAxis, splitPos, leftBB, rightBB);
        Axis nextAxis = Axis.nextAxis(currentAxis);

        BSPNode left = buildTree(objects, leftBB, nextAxis, depth + 1);
        BSPNode right = buildTree(objects, leftBB, nextAxis, depth + 1);

        BSPNode current = new BSPNode(splitPos, currentAxis, boundingBox);
        current.children.add(left);
        current.children.add(right);

        return current;
    }


    @Override
    public HitRecord intersect(Ray r)
    {
        return null;
    }

    @Override
    public BoundingBox getBoundingBox()
    {
        return root.getBoundingBox();
    }

    private float findSplitPlane(Aggregate objects, Axis axis)
    {

        BoundingBox bbox = null;

        Point3f centerOfMass = new Point3f();
        int numObjects = 0;
        Iterator<Intersectable> iterator = objects.iterator();
        while (iterator.hasNext())
        {
            BoundingBox b = iterator.next().getBoundingBox();
            centerOfMass.add(b.center());
            numObjects++;
        }
        centerOfMass.scale(1f / numObjects);

        return axis.getValue(centerOfMass);
    }

}
