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

    /**
     * Creates an axis aligned bounding box defined by two points representing the minimum and maximum interval boundaries.
     * The constructor automatically checks if the values in {@param p1} are smaller than the corresponding values in {@param p2} and swaps them if necessary.
     *
     * @param p1    First corner of the box
     * @param p2    Second corner of the box, opposite of {@param p1}
     */
    public AABoundingBox(Point3f p1, Point3f p2)
    {
        this.point1 = new Point3f(Math.min(p1.x, p2.x), Math.min(p1.y, p2.y), Math.min(p1.z, p2.z));
        this.point2 = new Point3f(Math.max(p1.x, p2.x), Math.max(p1.y, p2.y), Math.max(p1.z, p2.z));
    }

    /**
     * Creates an axis aligned bounding box defined by three intervals [{@param xmin}, {@param xmax}], [{@param ymin}, {@param ymax}] and [{@param zmin}, {@param zmax}].
     *
     * @param xmin  Lower bound for the interval on the X-Axis.
     * @param xmax  Upper bound for the interval on the X-Axis.
     * @param ymin  Lower bound for the interval on the Y-Axis.
     * @param ymax  Upper bound for the interval on the Y-Axis.
     * @param zmin  Lower bound for the interval on the Z-Axis.
     * @param zmax  Upper bound for the interval on the Z-Axis.
     */
    public AABoundingBox(float xmin, float xmax, float ymin, float ymax, float zmin, float zmax)
    {
        this(new Point3f(xmin, ymin, zmin), new Point3f(xmax, ymax, zmax));
    }

    /**
     * Copies an existing {@link AABoundingBox}.
     */
    public AABoundingBox(AABoundingBox b)
    {
        this(b.point1, b.point2);
    }

    /**
     * Returns the center point of this bounding box.
     */
    public Point3f center()
    {
        Vector3f halfDiag = StaticVecmath.sub(point2, point1);
        Point3f center = new Point3f();
        center.scaleAdd(0.5f, halfDiag, point1);
        return center;
    }

    public float xmin() { return point1.x; }

    public float ymin() { return point1.y; }

    public float zmin() { return point1.z; }

    public float xmax() { return point2.x; }

    public float ymax() { return point2.y; }

    public float zmax() { return point2.z; }

    /**
     * Splits this bounding box into two bounding boxes.
     *
     * @param axis  The {@link Axis} along which the box should be split.
     * @param p     The point on the {@param axis} at which the box should be split.
     * @return      An array of size two containing the bounding boxes.
     */
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

    /**
     * Returns true if this bounding box contains the given {@param point}, and false otherwise.
     */
    public boolean contains(Point3f point)
    {
        boolean xContains = StaticMath.doesIntervalContain(xmin(), xmax(), point.x);
        boolean yContains = StaticMath.doesIntervalContain(ymin(), ymax(), point.y);
        boolean zContains = StaticMath.doesIntervalContain(zmin(), zmax(), point.z);

        return xContains && yContains && zContains;
    }

    /**
     * Returns true if this bounding box intersects the {@param other} bounding box.
     */
    public boolean isIntersecting(AABoundingBox other)
    {

        boolean xIntersects = StaticMath.doesIntervalIntersect(xmin(), xmax(), other.xmin(), other.xmax());
        boolean yIntersects = StaticMath.doesIntervalIntersect(ymin(), ymax(), other.ymin(), other.ymax());
        boolean zIntersects = StaticMath.doesIntervalIntersect(zmin(), zmax(), other.zmin(), other.zmax());

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
