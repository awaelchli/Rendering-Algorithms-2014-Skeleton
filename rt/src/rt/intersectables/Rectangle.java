package rt.intersectables;

import rt.HitRecord;
import rt.Intersectable;
import rt.Ray;
import rt.StaticVecmath;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

/**
 * Created by Adrian on 10.04.2016.
 */
public class Rectangle extends Plane
{
    Point3f center;
    Vector3f edge1, edge2;

    /**
     * Precomputed length of each edge vector
     */
    float length1, length2;

    public Rectangle(Point3f center, Vector3f edge1, Vector3f edge2)
    {
        super(computeNormal(edge1, edge2), computeDistance(center, edge1, edge2));
        this.center = center;
        this.edge1 = edge1;
        this.edge2 = edge2;
        float length1 = edge1.length();
        float length2 = edge2.length();
    }

    public float area()
    {
        return (float) Math.sqrt(edge1.lengthSquared() * edge2.lengthSquared());
    }

    public Point3f center()
    {
        return new Point3f(center);
    }

    public Point3f anchor()
    {
        Point3f anchor = new Point3f();
        anchor.add(edge1, edge2);
        anchor.scale(-0.5f);
        anchor.add(center());
        return anchor;
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
        Vector3f delta = StaticVecmath.sub(planeHit.position, center);

        // Projected length of delta vector on edges
        float p1 = edge1.dot(delta) / length1;
        float p2 = edge2.dot(delta) / length2;

        if(Math.abs(p1) > length1 / 2 && Math.abs(p2) > length2 / 2)
        {
            // Hit position is outside rectangle bounds
            return null;
        }
        else // Rectangle is hit
        {
            planeHit.u = (p1 + length1 / 2) / length1;
            planeHit.v = (p2 + length2 / 2) / length2;
            return planeHit;
        }
    }

    private static Vector3f computeNormal(Vector3f edge1, Vector3f edge2)
    {
        Vector3f normal = StaticVecmath.cross(edge1, edge2);
        normal.normalize();
        return normal;
    }

    private static float computeDistance(Point3f center, Vector3f edge1, Vector3f edge2)
    {
        Vector3f normal = computeNormal(edge1, edge2);
        return normal.dot(new Vector3f(center));
    }
}
