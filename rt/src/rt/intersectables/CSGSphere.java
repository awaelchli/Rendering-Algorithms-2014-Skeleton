package rt.intersectables;

import rt.*;
import rt.materials.Diffuse;

import javax.vecmath.Point3f;
import javax.vecmath.Tuple2f;
import javax.vecmath.Vector3f;
import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by Adrian on 28.02.2016.
 */
public class CSGSphere extends CSGSolid {

    Point3f center;
    float radius;

    public Material material = Sphere.DEFAULT_MATERIAL;

    public CSGSphere(Point3f center, float radius){
        this.center = center;
        this.radius = radius;
    }

    public CSGSphere() {
        this(Sphere.DEFAULT_CENTER, Sphere.DEFAULT_RADIUS);
    }

    @Override
    ArrayList<IntervalBoundary> getIntervalBoundaries(Ray r) {
        // Notation following http://www.siggraph.org/education/materials/HyperGraph/raytrace/rtinter1.htm

        Vector3f d = new Vector3f(r.direction);
        Point3f o = new Point3f(r.origin);
        float c1 = (o.x - center.x);
        float c2 = (o.y - center.y);
        float c3 = (o.z - center.z);

        float a = d.dot(d);
        float b = 2 * (d.x * c1 + d.y * c2 + d.z * c3);
        float c = c1 * c1 + c2 * c2 + c3 * c3 - radius * radius;

        float discriminant = b * b - 4 * a * c;

        if (discriminant < 0){
            // No intersection
            return createBoundaries(null, null);
        }

        // t-Parameters at boundaries
        float t1 = (-b - (float) Math.sqrt(discriminant)) / (2 * a);
        float t2 = (-b + (float) Math.sqrt(discriminant)) / (2 * a);

        // Position of first boundary
        Point3f position1 = r.pointAt(t1);

        // Position of second boundary
        Point3f position2 = r.pointAt(t2);

        // Normal at first boundary
        Vector3f normal1 = StaticVecmath.sub(position1, center);
        normal1.scale(1 / radius);

        // Normal at second boundary
        Vector3f normal2 = StaticVecmath.sub(position2, center);
        normal2.scale(1 / radius);

        // Ray direction
        Vector3f w = new Vector3f(d);
        w.negate();

        Tuple2f uv1 = Sphere.getUVcoordinates(normal1);
        Tuple2f uv2 = Sphere.getUVcoordinates(normal2);

        HitRecord hit1 = new HitRecord(t1, position1, normal1, w, this, material, uv1.x, uv1.y);
        HitRecord hit2 = new HitRecord(t2, position2, normal2, w, this, material, uv2.x, uv2.y);

        return createBoundaries(hit1, hit2);
    }

    private ArrayList<IntervalBoundary> createBoundaries(HitRecord hit1, HitRecord hit2) {
        // First Boundary
        IntervalBoundary b1 = new IntervalBoundary();
        b1.type = BoundaryType.START;
        b1.hitRecord = hit1;

        // Second Boundary
        IntervalBoundary b2 = new IntervalBoundary();
        b2.type = BoundaryType.END;
        b2.hitRecord = hit2;

        if (hit1 != null)
            b1.t = hit1.t;

        if (hit2 != null)
            b2.t = hit2.t;

        ArrayList<IntervalBoundary> boundaries = new ArrayList<>();
        boundaries.add(b1);
        boundaries.add(b2);

        return boundaries;
    }
}