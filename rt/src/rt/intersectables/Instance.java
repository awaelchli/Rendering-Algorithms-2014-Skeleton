package rt.intersectables;

import rt.bsp.AABoundingBox;
import rt.HitRecord;
import rt.Intersectable;
import rt.Ray;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

/**
 * Created by adrian on 02.03.16.
 */
public class Instance implements Intersectable {

    /**
     * Reference to the actual object
     */
    Intersectable reference;

    /**
     * The transformation from object to world coordinates
     */
    Matrix4f transformation;

    /**
     * The transformation from world to object coordinates
     */
    Matrix4f inv_transformation;

    public Instance(Intersectable object, Matrix4f transformation) {
        this.reference = object;
        setTransformation(transformation);
    }

    public Instance(Intersectable object) {
        this.reference = object;
        Matrix4f identity = new Matrix4f();
        identity.setIdentity();
        setTransformation(identity);
    }

    public void setTransformation(Matrix4f t) {
        this.transformation = new Matrix4f(t);
        // Pre-compute inverse for later use
        this.inv_transformation = new Matrix4f(t);
        this.inv_transformation.invert();
    }

    @Override
    public HitRecord intersect(Ray r) {

        Point3f newOrigin = new Point3f(r.origin);
        Vector3f newDirection = new Vector3f(r.direction);
        inv_transformation.transform(newOrigin);
        inv_transformation.transform(newDirection);

        Ray transformedRay = new Ray(newOrigin, newDirection);
        HitRecord hit = reference.intersect(transformedRay);

        if (hit == null) // No intersection
            return null;

        hit.transform(transformation, inv_transformation);

        return hit;
    }

    @Override
    /**
     * @return null. Axis aligned bounding boxes are not supported for instancing.
     */
    public AABoundingBox getBoundingBox() {
        return null;
    }
}
