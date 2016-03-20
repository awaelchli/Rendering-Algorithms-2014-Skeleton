package rt;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

/**
 * A axis aligned bounding box
 */
public class BoundingBox {

    Point3f point1;
    Point3f point2;

    public BoundingBox(Point3f p1, Point3f p2) {
        this.point1 = new Point3f(p1);
        this.point2 = new Point3f(p2);
    }

    public BoundingBox(float xmin, float xmax, float ymin, float ymax, float zmin, float zmax){
        this.point1 = new Point3f(xmin, ymin, zmin);
        this.point2 = new Point3f(xmax, ymax, zmax);
    }

    /**
     * Copies an existing bounding box
     */
    public BoundingBox(BoundingBox b) {
        this(b.point1, b.point2);
    }

    public Point3f center() {
        Vector3f halfDiag = StaticVecmath.sub(point2, point1);
        Point3f center = new Point3f();
        center.scaleAdd(0.5f, halfDiag, point1);
        return center;
    }

    public boolean contains(Point3f point) {
        boolean xContains = doesIntervalContain(point1.x, point2.x, point.x);
        boolean yContains = doesIntervalContain(point1.y, point2.y, point.y);
        boolean zContains = doesIntervalContain(point1.z, point2.z, point.z);

        return xContains && yContains && zContains;
    }

    public boolean isIntersecting(BoundingBox other) {

        boolean xIntersects = doesIntervalIntersect(point1.x, point2.x, other.point1.x, other.point2.x);
        boolean yIntersects = doesIntervalIntersect(point1.y, point2.y, other.point1.y, other.point2.y);
        boolean zIntersects = doesIntervalIntersect(point1.z, point2.z, other.point1.z, other.point2.z);

        return xIntersects && yIntersects && zIntersects;
    }

    /**
     * Returns true if the intervals [a, b] and [c, d] intersect, and false otherwise.
     */
    private boolean doesIntervalIntersect(float a, float b, float c, float d) {
        // Correct ordering of interval boundaries
        if (a > b) {
            return doesIntervalIntersect(b, a, c, d);
        }
        if (c > d) {
            return doesIntervalIntersect(a, b, d, c);
        }

        return !(c > b || a > d);

    }

    private boolean doesIntervalContain(float a, float b, float v) {
        if(a > b) {
            return doesIntervalContain(b, a, v);
        }
        return v >= a && v <= b;
    }

}
