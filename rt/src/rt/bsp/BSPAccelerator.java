package rt.bsp;

import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import rt.HitRecord;
import rt.Intersectable;
import rt.Ray;
import rt.intersectables.Aggregate;
import rt.intersectables.IntersectableList;

import javax.vecmath.Point3f;
import java.util.Iterator;
import java.util.Stack;

/**
 * Created by adrian on 18.03.16.
 */
public class BSPAccelerator implements Intersectable
{

    public static final float EPSILON = 0.001f;

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
        current.below = left;
        current.above = right;

        return current;
    }


    @Override
    public HitRecord intersect(Ray r)
    {
        HitRecord hitRecord = null;
        Stack<BSPStackItem> stack = new Stack<>();
        BSPNode node = root;
        float isect = Float.MAX_VALUE;
        BSPStackItem rootItem = root.intersect(r);

        if (rootItem == null) {
            // Ray did not intersect with  bounding box of root node
            return null;
        }

        float tmin = rootItem.tmin;
        float tmax = rootItem.tmax;

        while(node != null)
        {
            if( isect < tmin ) break;
            if( !node.isLeaf() )
            {

//                float o = node.axis.getValue(r.origin);
//                float d = node.axis.getValue(r.direction);
//
//                float tsplit = (node.planePos - o) / d;

                float tsplit = node.intersect(r).tsplit;

                // order children
                BSPNode first, second;
                if(node.axis.getValue(r.origin) < node.planePos )
                {
                    first = node.below;
                    second = node.above;
                }
                else
                {
                    first = node.above;
                    second = node.below;
                }

                // process children
                if( tsplit > tmax || tsplit < 0 || (Math.abs(tsplit) < EPSILON && first.intersect(r) != null))
                { // case 1: only first child is hit
                    node = first;
                }
                else if(tsplit < tmin || (Math.abs(tsplit) < EPSILON && second.intersect(r) != null))
                { // case 2: only second child is hit
                    node = second;
                }
                else
                { // case 3: both children are hit
                    node = first;
                    BSPStackItem item = new BSPStackItem();
                    item.node = second;
                    item.tmin = tsplit;
                    item.tmax = tmax;
                    stack.push(item);
                    tmax = tsplit;
                }

            }
            else
            {
                HitRecord hit = node.objects.intersect(r);
                if (hit != null && hit.t < isect && hit.t > 0)
                {
                    hitRecord = hit;
                    isect = hitRecord.t;
                }
                if (stack.isEmpty())
                { // No intersection
                    break;
                }
                BSPStackItem i = stack.pop();
                node = i.node;
                tmin = i.tmin;
                tmax = i.tmax;
            }
        }
        return hitRecord;
    }

//    // TODO: remove once propper intersection is implemented
//    private HitRecord intersect(BSPNode node, Ray r) {
//        if(node.isLeaf())
//        {
//            return node.getObjects().intersect(r);
//        }
//
//        HitRecord hitRecord = null;
//        float t = Float.MAX_VALUE;
//
//        Iterator<BSPNode> iterator = node.children.iterator();
//        while(iterator.hasNext()) {
//            HitRecord tmp = intersect(iterator.next(), r);
//            if(tmp!=null && tmp.t<t)
//            {
//                t = tmp.t;
//                hitRecord = tmp;
//            }
//        }
//
//        return hitRecord;
//    }

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
