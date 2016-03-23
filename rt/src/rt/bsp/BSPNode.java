package rt.bsp;

import rt.Ray;
import rt.intersectables.Aggregate;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

/**
 * Created by adrian on 18.03.16.
 */
public class BSPNode
{

    float planePos;
    Axis axis;
    AABoundingBox bb;
    BSPNode below, above;

    /**
     * Only for leaf nodes
     */
    Aggregate objects;

    BSPNode()
    {
    }

    public BSPNode(float planePos, Axis axis, AABoundingBox boundingBox)
    {
        this.planePos = planePos;
        this.axis = axis;
        this.bb = boundingBox;
    }

    public AABoundingBox getBoundingBox()
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

    public float[] intersect(Ray r)
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

        tmin = Math.max(tmin, tzmin);
        tmax = Math.min(tmax, tzmax);
        return new float[] {tmin, tmax};
    }
}
