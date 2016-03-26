package rt.bsp;

import rt.HitRecord;
import rt.Intersectable;
import rt.Ray;
import rt.intersectables.Aggregate;
import rt.intersectables.IntersectableList;

import javax.vecmath.Point3f;
import java.util.Iterator;
import java.util.Stack;

/**
 * Implements an acceleration structure using axis aligned bounding boxes
 */
public class BSPAccelerator implements Intersectable
{

    public static final float EPSILON = 0.001f;

    private int maxDepth, maxObjectsPerNode;
    private BSPNode root;

    /**
     * Initializes an acceleration structure with parameters for the stopping criteria.
     * @param maxObjectsPerNode     If this number is reached in the construction process, the node will not be split again.
     * @param maxDepth              The maximum depth of the tree. If this number is reached, the node will not be further split.
     */
    public BSPAccelerator(int maxObjectsPerNode, int maxDepth)
    {
        this.maxDepth = maxDepth;
        this.maxObjectsPerNode = maxObjectsPerNode;
    }

    /**
     * Recursively constructs the acceleration structure on the given {@param objects}.
     */
    public void construct(Aggregate objects)
    {
        root = buildTree(objects, objects.getBoundingBox(), Axis.X, 0);
    }

    private BSPNode buildTree(Aggregate objects, AABoundingBox boundingBox, Axis currentAxis, float depth)
    {
        if (depth == maxDepth || objects.count() <= maxObjectsPerNode)
        {   // Stopping criteria are met: Create leaf node
            BSPLeaf current = new BSPLeaf(boundingBox, objects);
            return current;
        }

        /*
         *  Split the bounding box into left and right
         */
        float splitPos = findSplitPlane(objects, currentAxis);

        AABoundingBox[] boxes = boundingBox.split(currentAxis, splitPos);
        AABoundingBox leftBB = boxes[0];
        AABoundingBox rightBB = boxes[1];

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
            AABoundingBox bb = object.getBoundingBox();

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


    /**
     * Accelerated intersection test by kd-tree traversal.
     * The tree must be built with {@link #construct(Aggregate)} beforehand.
     */
    @Override
    public HitRecord intersect(Ray r)
    {
        HitRecord closest = null;
        Stack<BSPStackItem> stack = new Stack<>();
        BSPNode node = root;
        float isect = Float.MAX_VALUE;
        float[] interval = root.intersect(r);

        if (interval == null) {
            // Ray did not intersect with  bounding box of root node
            return null;
        }

        float tmin = interval[0];
        float tmax = interval[1];

        while(node != null)
        {
            if( isect < tmin ) break;
            if( !node.isLeaf() )
            {
                float tsplit = computeRaySplitPlaneIntersection(r, node);

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
                    closest = hit;
                    isect = closest.t;
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
        return closest;
    }

    @Override
    public AABoundingBox getBoundingBox()
    {
        return root.getBoundingBox();
    }

    private float findSplitPlane(Aggregate objects, Axis axis)
    {

        AABoundingBox bbox = null;

        Point3f centerOfMass = new Point3f();
        int numObjects = 0;
        Iterator<Intersectable> iterator = objects.iterator();
        while (iterator.hasNext())
        {
            AABoundingBox b = iterator.next().getBoundingBox();
            centerOfMass.add(b.center());
            numObjects++;
        }
        centerOfMass.scale(1f / numObjects);

        return axis.getValue(centerOfMass);
    }

    private float computeRaySplitPlaneIntersection(Ray r, BSPNode node)
    {
        if (node.isLeaf()) {
            // no split plane exists for leafs
            return Float.NaN;
        }
        assert node.axis != null;

        float o = node.axis.getValue(r.origin);
        float d = node.axis.getValue(r.direction);

        return (node.planePos - o) / d;
    }

    /**
     * A simple stack item used to calculate intersections.
     */
    class BSPStackItem
    {
        BSPNode node;
        float tmin, tmax, tsplit;
    }
}
