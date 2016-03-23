package rt.bsp;

import rt.StaticMath;
import rt.StaticVecmath;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

/**
 * An axis aligned bounding box
 */
public class AABoundingBox
{
    Point3f point1;
    Point3f point2;

    public AABoundingBox(Point3f p1, Point3f p2)
    {
        assert p1.x <= p2.x && p1.y <= p2.y && p1.z <= p2.z;
        this.point1 = new Point3f(p1);
        this.point2 = new Point3f(p2);
    }

    public AABoundingBox(float xmin, float xmax, float ymin, float ymax, float zmin, float zmax)
    {
        this(new Point3f(xmin, ymin, zmin), new Point3f(xmax, ymax, zmax));
    }

    /**
     * Copies an existing bounding box
     */
    public AABoundingBox(AABoundingBox b)
    {
        this(b.point1, b.point2);
    }

    public Point3f center()
    {
        Vector3f halfDiag = StaticVecmath.sub(point2, point1);
        Point3f center = new Point3f();
        center.scaleAdd(0.5f, halfDiag, point1);
        return center;
    }

    public float xmin() { return Math.min(point1.x, point2.x); }

    public float ymin() { return Math.min(point1.y, point2.y); }

    public float zmin() { return Math.min(point1.z, point2.z); }

    public float xmax() { return Math.max(point1.x, point2.x); }

    public float ymax() { return Math.max(point1.y, point2.y); }

    public float zmax() { return Math.max(point1.z, point2.z); }


    public AABoundingBox[] split(Axis axis, float p)
    {
        Point3f p1 = new Point3f(point1);
        Point3f p2 = new Point3f(point2);

        int i = axis.getIndex();

        Point3f q1 = new Point3f(p2);
        StaticVecmath.set(q1, i, p);
        Point3f q2 = new Point3f(p1);
        StaticVecmath.set(q2, i, p);

        AABoundingBox left = new AABoundingBox(p1, q1);
        AABoundingBox right = new AABoundingBox(q2, p2);

        return new AABoundingBox[] {left, right};
    }

    public boolean contains(Point3f point)
    {
        boolean xContains = StaticMath.doesIntervalContain(point1.x, point2.x, point.x);
        boolean yContains = StaticMath.doesIntervalContain(point1.y, point2.y, point.y);
        boolean zContains = StaticMath.doesIntervalContain(point1.z, point2.z, point.z);

        return xContains && yContains && zContains;
    }

    public boolean isIntersecting(AABoundingBox other)
    {

        boolean xIntersects = StaticMath.doesIntervalIntersect(point1.x, point2.x, other.point1.x, other.point2.x);
        boolean yIntersects = StaticMath.doesIntervalIntersect(point1.y, point2.y, other.point1.y, other.point2.y);
        boolean zIntersects = StaticMath.doesIntervalIntersect(point1.z, point2.z, other.point1.z, other.point2.z);

        return xIntersects && yIntersects && zIntersects;
    }

    /**
     * Enlarges the current bounding box such that it contains the {@param other} bounding box.
     */
    public void add(AABoundingBox other)
    {
        float xmin = Math.min(xmin(), other.xmin());
        float ymin = Math.min(ymin(), other.ymin());
        float zmin = Math.min(zmin(), other.zmin());
        float xmax = Math.max(xmax(), other.xmax());
        float ymax = Math.max(ymax(), other.ymax());
        float zmax = Math.max(ymax(), other.ymax());

        point1.set(xmin, ymin, zmin);
        point2.set(xmax, ymax, zmax);
    }



}
