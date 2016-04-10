package rt.lightsources;

import rt.*;
import rt.bsp.AABoundingBox;
import rt.intersectables.Rectangle;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

/**
 * Created by Adrian on 10.04.2016.
 */
public class RectangleLight implements LightGeometry
{
    Rectangle rectangle;

    Vector3f edge1, edge2;

    public RectangleLight(Point3f center, Vector3f edge1, Vector3f edge2, Spectrum emission)
    {
        this.rectangle = new Rectangle(center, edge1, edge2);
        this.edge1 = edge1;
        this.edge2 = edge2;
    }

    public RectangleLight(Point3f bottomLeft, Point3f right, Point3f top, Spectrum emission)
    {
        this.edge1 = StaticVecmath.sub(right, bottomLeft);
        this.edge2 = StaticVecmath.sub(top, bottomLeft);
        Point3f center = new Point3f(bottomLeft);
        center.add(edge1);
        center.add(edge2);
        center.scale(0.5f);
        this.rectangle = new Rectangle(center, edge1, edge2);
    }

    @Override
    public HitRecord sample(float[] s)
    {
        Vector3f dir1 = new Vector3f(edge1);
        Vector3f dir2 = new Vector3f(edge2);

        dir1.scale(s[0]);
        dir2.scale(s[1]);

        Point3f pos = new Point3f(rectangle.anchor());
        pos.add(dir1);
        pos.add(dir2);

        HitRecord sample = new HitRecord();
        sample.position = pos;
        sample.normal = rectangle.normal();
        sample.p = 1 / rectangle.area();
        sample.intersectable = this;
        sample.material = rectangle.material;

        return sample;
    }

    @Override
    public float area()
    {
        return rectangle.area();
    }

    @Override
    public HitRecord intersect(Ray r)
    {
        HitRecord hit = rectangle.intersect(r);
        if (hit != null)
        {
            hit.intersectable = this;
            hit.p = 1 / rectangle.area();
        }
        return hit;
    }

    @Override
    public AABoundingBox getBoundingBox()
    {
        return null;
    }
}
