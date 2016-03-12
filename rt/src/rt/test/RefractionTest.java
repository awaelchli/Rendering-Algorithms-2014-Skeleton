package rt.test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import rt.HitRecord;
import rt.Material;
import rt.Ray;
import rt.materials.Refractive;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;


public class RefractionTest {

    @Before
    public void setUp() {
    }

    @Test
    public void reflectionTestPerpendicular() {

        Vector3f normal = new Vector3f(0, 1, 0);

        HitRecord hit = new HitRecord();
        hit.normal = normal;
        hit.position = new Point3f(0, 0, 0);
        hit.w = normal;

        Ray ray = Ray.reflect(hit);

        assertEquals(ray.direction.x, normal.x, 0.00001);
        assertEquals(ray.direction.y, normal.y, 0.00001);
        assertEquals(ray.direction.z, normal.z, 0.00001);
    }

    @Test
    public void reflectionTest45Degrees() {

        Vector3f normal = new Vector3f(0, 1, 0);

        HitRecord hit = new HitRecord();
        hit.normal = normal;
        hit.position = new Point3f(0, 0, 0);
        hit.w = new Vector3f(-1, 1, 0);
        hit.w.normalize();

        Ray ray = Ray.reflect(hit);

        Vector3f out = new Vector3f(1, 1, 0);
        out.normalize();

        assertEquals(ray.direction.x, out.x, 0.00001);
        assertEquals(ray.direction.y, out.y, 0.00001);
        assertEquals(ray.direction.z, out.z, 0.00001);
    }

    @Test
    public void refractionIndex1() {

        Vector3f normal = new Vector3f(0, 1, 0);

        HitRecord hit = new HitRecord();
        hit.normal = normal;
        hit.position = new Point3f(0, 0, 0);
        hit.w = new Vector3f(-1, 1, 0);
        hit.w.normalize();

        Refractive material = new Refractive(1f);

        Material.ShadingSample sample = material.evaluateSpecularRefraction(hit);

        Vector3f refractedDir = sample.w;

        Vector3f out = new Vector3f(hit.w);
        out.negate();

        assertEquals(refractedDir.x, out.x, 0.00001);
        assertEquals(refractedDir.y, out.y, 0.00001);
        assertEquals(refractedDir.z, out.z, 0.00001);

        assertEquals(sample.brdf.r, 1, 0.01);
        assertEquals(sample.brdf.g, 1, 0.01);
        assertEquals(sample.brdf.b, 1, 0.01);

        sample = material.evaluateSpecularReflection(hit);

    }

    @Test
    public void refractionIndex1_3() {

        Vector3f normal = new Vector3f(0, 1, 0);

        HitRecord hit = new HitRecord();
        hit.normal = normal;
        hit.position = new Point3f(0, 0, 0);
        hit.w = new Vector3f(-1, 1, 0);
        hit.w.normalize();

        Refractive material = new Refractive(1.3f);

        Material.ShadingSample sample = material.evaluateSpecularRefraction(hit);

        Vector3f refractedDir = sample.w;

        Vector3f out = new Vector3f(hit.w);
        out.negate();

        float outAngleExpected = (float) Math.toRadians(33);
        float cosExpected = -(float) Math.cos(outAngleExpected);
        float cos = normal.dot(refractedDir);

        assertEquals(cos, cosExpected, 0.01);
        assertEquals(sample.brdf.r, 0.9547, 0.1);

        sample = material.evaluateSpecularReflection(hit);

    }
}
