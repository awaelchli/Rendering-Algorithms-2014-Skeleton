package rt.bsp;

import rt.Ray;
import rt.intersectables.Aggregate;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

/**
 * A node for the BSP tree built by the {@link BSPAccelerator}.
 */
public class BSPNode
{
    /**
     * The position of the split plane
     */
    float planePos;

    /**
     * The split axis
     */
    Axis axis;

    /**
     * The bounding box of this node
     */
    AABoundingBox bb;

    /**
     * The two nodes above and below the split plane
     */
    BSPNode below, above;

    /**
     * Objects associated to this node, only for leaf nodes.
     */
    Aggregate objects;

    BSPNode() {}

    /**
     * Creates a node for the BSP tree.
     *
     * @param planePos      The position where the bounding box of this node was split.
     * @param axis          The axis along which the bounding box was split.
     * @param boundingBox   The bounding box of this node.
     */
    public BSPNode(float planePos, Axis axis, AABoundingBox boundingBox)
    {
        this.planePos = planePos;
        this.axis = axis;
        this.bb = boundingBox;
    }

    /**
     * Returns the {@link AABoundingBox} of this node.
     */
    public AABoundingBox getBoundingBox()
    {
        return bb;
    }

    public boolean isLeaf()
    {
        return false;
    }

    /**
     * If this node is a leaf, it returns all objects that lie within the bounding box of this node.
     *
     * @return  null, if this node is not a leaf.
     * @see #isLeaf()
     */
    public Aggregate getObjects()
    {
        return objects;
    }

    /**
     * Intersects the {@param ray} with the bounding box of this node.
     *
     * @return  An array containing the minimum and maximum t-values for the {@param ray} if it intersects with the bounding box.
     *          The method returns null if the {@param ray} does not intersect the bounding box.
     */
    public float[] intersect(Ray ray)
    {
        Vector3f dirfrac = new Vector3f();
        dirfrac.x = 1.0f / ray.direction.x;
        dirfrac.y = 1.0f / ray.direction.y;
        dirfrac.z = 1.0f / ray.direction.z;

        Point3f[] bounds = new Point3f[]{bb.point1, bb.point2};
        int sign[] = new int[3];
        sign[0] = (dirfrac.x >= 0 ? 0 : 1);
        sign[1] = (dirfrac.y >= 0 ? 0 : 1);
        sign[2] = (dirfrac.z >= 0 ? 0 : 1);

        float txmin, txmax, tymin, tymax, tzmin, tzmax;

        txmin = (bounds[sign[0]].x - ray.origin.x) * dirfrac.x;
        txmax = (bounds[1 - sign[0]].x - ray.origin.x) * dirfrac.x;
        tymin = (bounds[sign[1]].y - ray.origin.y) * dirfrac.y;
        tymax = (bounds[1 - sign[1]].y - ray.origin.y) * dirfrac.y;

        if ((txmin > tymax) || (tymin > txmax))
            return null;

        tzmin = (bounds[sign[2]].z - ray.origin.z) * dirfrac.z;
        tzmax = (bounds[1 - sign[2]].z - ray.origin.z) * dirfrac.z;

        float tmin = Math.max(txmin, tymin);
        float tmax = Math.min(txmax, tymax);

        if ((tmin > tzmax) || (tzmin > tmax))
            return null;

        tmin = Math.max(tmin, tzmin);
        tmax = Math.min(tmax, tzmax);
        return new float[] {tmin, tmax};
    }
}
