package rt;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

/**
 * Created by adrian on 02.03.16.
 */
public class Instance implements Intersectable {

    Intersectable reference;

    Matrix4f transformation;

    public Instance(Intersectable object, Matrix4f transformation) {
        this.reference = object;
        this.transformation = new Matrix4f(transformation);
    }

    public Instance(Intersectable object) {
        this.reference = object;
        transformation = new Matrix4f();
        transformation.setIdentity();
    }

    public void setTransformation(Matrix4f t) {
        transformation = new Matrix4f(t);
    }

    @Override
    public HitRecord intersect(Ray r) {

        Matrix4f invTransformation = new Matrix4f(this.transformation);
        invTransformation.invert();

        Point3f newOrigin = new Point3f(r.origin);
        Vector3f newDirection = new Vector3f(r.direction);
        invTransformation.transform(newOrigin);
        invTransformation.transform(newDirection);

        Ray transformedRay = new Ray(newOrigin, newDirection);
        HitRecord hit = reference.intersect(transformedRay);

        if (hit == null) // No intersection
            return null;

        transformation.transform(hit.position);
        transformation.transform(hit.normal);
        transformation.transform(hit.t1);
        transformation.transform(hit.t2);
        transformation.transform(hit.w);

        return hit;
    }
}
