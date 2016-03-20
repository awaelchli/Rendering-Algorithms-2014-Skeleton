package rt.intersectables;

import rt.BoundingBox;
import rt.HitRecord;
import rt.Intersectable;
import rt.Ray;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Adrian on 03.03.2016.
 */
public class CSGInstance extends CSGSolid {

    /**
     * Reference to the actual object
     */
    CSGSolid reference;

    /**
     * The transformation from object to world coordinates
     */
    Matrix4f transformation;

    /**
     * The transformation from world to object coordinates
     */
    Matrix4f inv_transformation;

    public CSGInstance(CSGSolid object, Matrix4f transformation) {
        this.reference = object;
        setTransformation(transformation);
    }

    public CSGInstance(CSGSolid object) {
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
    ArrayList<IntervalBoundary> getIntervalBoundaries(Ray r) {

        Point3f newOrigin = new Point3f(r.origin);
        Vector3f newDirection = new Vector3f(r.direction);
        inv_transformation.transform(newOrigin);
        inv_transformation.transform(newDirection);

        Ray transformedRay = new Ray(newOrigin, newDirection);
        ArrayList<IntervalBoundary> boundaries = reference.getIntervalBoundaries(transformedRay);

        Iterator<IntervalBoundary> iter = boundaries.iterator();
        while (iter.hasNext()){
            IntervalBoundary boundary = iter.next();
            if (boundary.hitRecord != null) {
                boundary.hitRecord.transform(transformation, inv_transformation);
            }
        }

        return boundaries;
    }

    @Override
    public BoundingBox getBoundingBox() {
        BoundingBox bb = new BoundingBox(reference.getBoundingBox());
        // TODO: Transform bounding box and return
        return bb;
    }
}
