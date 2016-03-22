package rt.bsp;

import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
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
            current.objects = objects;
            return current;
        }

        /*
         *  Split the bounding box into left and right
         */
        float splitPos = findSplitPlane(objects, currentAxis);

        BoundingBox leftBB = new BoundingBox();
        BoundingBox rightBB = new BoundingBox();

        boundingBox.split(currentAxis, splitPos, leftBB, rightBB);
        Axis nextAxis = Axis.nextAxis(currentAxis);

        /*
         * Collect objects for the left and right half
         */
        IntersectableList leftObjs = new IntersectableList();
        IntersectableList rightObjs = new IntersectableList();
        Iterator<Intersectable> iterator = objects.iterator();
        while (iterator.hasNext())
        {
            Intersectable object = iterator.next();
            BoundingBox bb = object.getBoundingBox();

            if (bb.isIntersecting(leftBB))
            {
                leftObjs.add(object);
            }
            if (bb.isIntersecting(rightBB))
            {
                rightObjs.add(object);
            }
        }

        /*
         *  Recursively build the sub-tree on the left and right node
         */
        BSPNode left = buildTree(leftObjs, leftBB, nextAxis, depth + 1);
        BSPNode right = buildTree(rightObjs, rightBB, nextAxis, depth + 1);

        BSPNode current = new BSPNode(splitPos, currentAxis, boundingBox);
        current.children.add(left);
        current.children.add(right);

        return current;
    }


    @Override
    public HitRecord intersect(Ray r)
    {
        return intersect(root, r);
    }

    // TODO: remove once propper intersection is implemented
    private HitRecord intersect(BSPNode node, Ray r) {
        if(node.isLeaf())
        {
            return node.getObjects().intersect(r);
        }

        HitRecord hitRecord = null;
        float t = Float.MAX_VALUE;

        Iterator<BSPNode> iterator = node.children.iterator();
        while(iterator.hasNext()) {
            HitRecord tmp = intersect(iterator.next(), r);
            if(tmp!=null && tmp.t<t)
            {
                t = tmp.t;
                hitRecord = tmp;
            }
        }

        return hitRecord;
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
