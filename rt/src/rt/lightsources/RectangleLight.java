package rt.lightsources;

import rt.*;
import rt.bsp.AABoundingBox;
import rt.intersectables.Rectangle;
import rt.materials.AreaLightMaterial;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

/**
 * Created by Adrian on 10.04.2016.
 */
public class RectangleLight implements LightGeometry
{
    Rectangle rectangle;

    Vector3f right, up;

    public RectangleLight(Point3f anchor, Vector3f right, Vector3f up, Spectrum emission)
    {
        this.rectangle = new Rectangle(anchor, right, up);
        this.right = new Vector3f(right);
        this.up = new Vector3f(up);
        rectangle.material = new AreaLightMaterial(emission, area());
    }

    @Override
    public HitRecord sample(float[] s)
    {
        Vector3f dir1 = new Vector3f(right);
        Vector3f dir2 = new Vector3f(up);

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
