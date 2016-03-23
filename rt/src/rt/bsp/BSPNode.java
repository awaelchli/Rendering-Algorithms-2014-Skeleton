package rt.bsp;

import rt.Intersectable;
import rt.Ray;
import rt.StaticVecmath;
import rt.intersectables.Aggregate;
import rt.intersectables.IntersectableList;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by adrian on 18.03.16.
 */
public class BSPNode
{

    float planePos;
    Axis axis;
    BoundingBox bb;
    BSPNode below, above;

    /**
     * Only for leaf nodes
     */
    Aggregate objects;

    BSPNode()
    {
    }

    public BSPNode(float planePos, Axis axis, BoundingBox boundingBox)
    {
        this.planePos = planePos;
        this.axis = axis;
        this.bb = boundingBox;
    }

    public BoundingBox getBoundingBox()
    {
        return bb;
    }

    public boolean isLeaf()
    {
        return false;
    }

    public Aggregate getObjects()
    {
        return objects;
    }

    public BSPStackItem intersect(Ray r)
    {
        Vector3f dirfrac = new Vector3f();
        dirfrac.x = 1.0f / r.direction.x;
        dirfrac.y = 1.0f / r.direction.y;
        dirfrac.z = 1.0f / r.direction.z;

        Point3f[] bounds = new Point3f[]{bb.point1, bb.point2};
        int sign[] = new int[3];
        sign[0] = (dirfrac.x >= 0 ? 0 : 1);
        sign[1] = (dirfrac.y >= 0 ? 0 : 1);
        sign[2] = (dirfrac.z >= 0 ? 0 : 1);

        float txmin, txmax, tymin, tymax, tzmin, tzmax;

        txmin = (bounds[sign[0]].x - r.origin.x) * dirfrac.x;
        txmax = (bounds[1 - sign[0]].x - r.origin.x) * dirfrac.x;
        tymin = (bounds[sign[1]].y - r.origin.y) * dirfrac.y;
        tymax = (bounds[1 - sign[1]].y - r.origin.y) * dirfrac.y;

        if ((txmin > tymax) || (tymin > txmax))
            return null;

        tzmin = (bounds[sign[2]].z - r.origin.z) * dirfrac.z;
        tzmax = (bounds[1 - sign[2]].z - r.origin.z) * dirfrac.z;

        float tmin = Math.max(txmin, tymin);
        float tmax = Math.min(txmax, tymax);

        if ((tmin > tzmax) || (tzmin > tmax))
            return null;

        BSPStackItem item = new BSPStackItem();

        item.tmin = Math.max(tmin, tzmin);
        item.tmax = Math.min(tmax, tzmax);
        item.tsplit = computeRaySplitPlaneIntersection(r);
        item.node = this;
        return item;

//        Vector3f dirfrac = new Vector3f();
//        dirfrac.x = 1.0f / r.direction.x;
//        dirfrac.y = 1.0f / r.direction.y;
//        dirfrac.z = 1.0f / r.direction.z;
//
//        BoundingBox bb = getBoundingBox();
//
//        float t1 = (bb.xmin() - r.origin.x) * dirfrac.x;
//        float t2 = (bb.xmax() - r.origin.x) * dirfrac.x;
//        float t3 = (bb.ymin() - r.origin.y) * dirfrac.y;
//        float t4 = (bb.ymax() - r.origin.y) * dirfrac.y;
//        float t5 = (bb.zmin() - r.origin.z) * dirfrac.z;
//        float t6 = (bb.zmax() - r.origin.z) * dirfrac.z;
//
//        BSPStackItem item = new BSPStackItem();
//
//        item.tmin = Math.max(Math.max(Math.min(t1, t2), Math.min(t3, t4)), Math.min(t5, t6));
//        item.tmax = Math.min(Math.min(Math.max(t1, t2), Math.max(t3, t4)), Math.max(t5, t6));
//        item.tsplit = computeRaySplitPlaneIntersection(r);
//        item.node = this;
//        return item;
    }

    private float computeRaySplitPlaneIntersection(Ray r)
    {
        if (isLeaf()) {
            // no split plane exists for leafs
            return Float.NaN;
        }
        float o = axis.getValue(r.origin);
        float d = axis.getValue(r.direction);

        return (planePos - o) / d;
    }
}
