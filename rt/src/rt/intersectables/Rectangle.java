package rt.intersectables;

import rt.HitRecord;
import rt.Ray;
import rt.StaticVecmath;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

/**
 * Created by Adrian on 10.04.2016.
 */
public class Rectangle extends Plane
{
    Point3f anchor;
    Vector3f right, up;

    /**
     * Precomputed length of each edge vector
     */
    float length1, length2;

    public Rectangle(Point3f anchor, Vector3f right, Vector3f up)
    {
        super(computeNormal(right, up), computeDistance(anchor, right, up));
        this.anchor = new Point3f(anchor);
        this.right = new Vector3f(right);
        this.up = new Vector3f(up);
        length1 = right.length();
        length2 = up.length();
    }

    public float area()
    {
        return (float) Math.sqrt(right.lengthSquared() * up.lengthSquared());
    }

    public Point3f center()
    {
        Point3f center = new Point3f();
        center.add(right, up);
        center.scale(0.5f);
        center.add(anchor());
        return center;
    }

    public Point3f anchor()
    {
        return new Point3f(anchor);
    }

    public Vector3f normal()
    {
        return new Vector3f(this.normal);
    }

    @Override
    public HitRecord intersect(Ray r)
    {
        HitRecord planeHit = super.intersect(r);

        // The ray did not hit the plane
        if(planeHit == null) return null;

        // Difference between rectangle center and hit position
        Vector3f delta = StaticVecmath.sub(planeHit.position, anchor());

        // Projected length of delta vector on edges
        float p1 = right.dot(delta) / length1;
        float p2 = up.dot(delta) / length2;

        if(p1 < 0 || p2 < 0|| p1 > length1 || p2 > length2)
        {
            // Hit position is outside rectangle bounds
            return null;
        }
        else // Rectangle is hit
        {
            planeHit.u = p1 / length1;
            planeHit.v = p2 / length2;
            return planeHit;
        }
    }

    private static Vector3f computeNormal(Vector3f edge1, Vector3f edge2)
    {
        Vector3f normal = StaticVecmath.cross(edge1, edge2);
        normal.normalize();
        return normal;
    }

    private static float computeDistance(Point3f anchor, Vector3f edge1, Vector3f edge2)
    {
        Vector3f normal = computeNormal(edge1, edge2);
        return -normal.dot(new Vector3f(anchor));
    }
}
