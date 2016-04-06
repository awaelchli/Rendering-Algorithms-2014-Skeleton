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

    /**
     * Precomputed {@link AABoundingBox},
     * @see #precomputeBoundingBox()
     */
    AABoundingBox boundingBox;

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
        precomputeBoundingBox();
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
     * @return Returns the smallest bounding box that contains the transformed original bounding box.
     */
    public AABoundingBox getBoundingBox()
    {
        return this.boundingBox;
    }

    private void precomputeBoundingBox()
    {
        AABoundingBox bb = reference.getBoundingBox();
        Point3f p000 = new Point3f(bb.xmin(), bb.ymin(), bb.zmin());
        Point3f p001 = new Point3f(bb.xmin(), bb.ymin(), bb.zmax());
        Point3f p010 = new Point3f(bb.xmin(), bb.ymax(), bb.zmin());
        Point3f p011 = new Point3f(bb.xmin(), bb.ymax(), bb.zmax());
        Point3f p100 = new Point3f(bb.xmax(), bb.ymin(), bb.zmin());
        Point3f p101 = new Point3f(bb.xmax(), bb.ymin(), bb.zmax());
        Point3f p110 = new Point3f(bb.xmax(), bb.ymax(), bb.zmin());
        Point3f p111 = new Point3f(bb.xmax(), bb.ymax(), bb.zmax());

        transformation.transform(p000);
        transformation.transform(p001);
        transformation.transform(p010);
        transformation.transform(p011);
        transformation.transform(p100);
        transformation.transform(p101);
        transformation.transform(p110);
        transformation.transform(p111);

        float xmin = Math.min(Math.min(Math.min(p000.x, p001.x), Math.min(p010.x, p011.x)), Math.min(Math.min(p100.x, p101.x), Math.min(p110.x, p111.x)));
        float ymin = Math.min(Math.min(Math.min(p000.y, p001.y), Math.min(p010.y, p011.y)), Math.min(Math.min(p100.y, p101.y), Math.min(p110.y, p111.y)));
        float zmin = Math.min(Math.min(Math.min(p000.z, p001.z), Math.min(p010.z, p011.z)), Math.min(Math.min(p100.z, p101.z), Math.min(p110.z, p111.z)));

        float xmax = Math.max(Math.max(Math.max(p000.x, p001.x), Math.max(p010.x, p011.x)), Math.max(Math.max(p100.x, p101.x), Math.max(p110.x, p111.x)));
        float ymax = Math.max(Math.max(Math.max(p000.y, p001.y), Math.max(p010.y, p011.y)), Math.max(Math.max(p100.y, p101.y), Math.max(p110.y, p111.y)));
        float zmax = Math.max(Math.max(Math.max(p000.z, p001.z), Math.max(p010.z, p011.z)), Math.max(Math.max(p100.z, p101.z), Math.max(p110.z, p111.z)));

        this.boundingBox = new AABoundingBox(xmin, xmax, ymin, ymax, zmin, zmax);
    }
}
