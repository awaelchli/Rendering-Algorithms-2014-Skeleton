package rt.test;

import org.junit.Before;
import org.junit.Test;
import rt.HitRecord;
import rt.Material;
import rt.Ray;
import rt.bsp.Axis;
import rt.bsp.BoundingBox;
import rt.materials.Refractive;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import static org.junit.Assert.*;


public class BSPAccelerationTest
{

    private static final float EPS = 0.0001f;

    @Before
    public void setUp()
    {
    }

    @Test
    public void testBoundingBoxIntersection()
    {
        BoundingBox b1 = new BoundingBox(-1, 1, -1, 1, -1, 1);
        BoundingBox b2 = new BoundingBox(0, 2, 0, 2, 0, 2);
        BoundingBox b3 = new BoundingBox(1, 2, 1, 2, 1, 2);
        BoundingBox b4 = new BoundingBox(0, 2, 0, 2, 3, 4);

        assertTrue(b1.isIntersecting(b2));
        assertTrue(b2.isIntersecting(b1));
        assertTrue(b1.isIntersecting(b3));
        assertFalse(b1.isIntersecting(b4));
    }

    @Test
    public void testBoundingBoxAddition()
    {
        BoundingBox b1 = new BoundingBox(-1, 0, 0, 1, 0, 1);
        BoundingBox b2 = new BoundingBox(0, 1, 0, 1, 0, 1);

        b1.add(b2);
        assertEquals(-1, b1.xmin(), EPS);
        assertEquals(1, b1.xmax(), EPS);
        assertEquals(0, b1.ymin(), EPS);
        assertEquals(1, b1.ymax(), EPS);
        assertEquals(0, b1.zmin(), EPS);
        assertEquals(1, b1.zmax(), EPS);
    }

    @Test
    public void testSplitBoundingBox()
    {
        BoundingBox b1 = new BoundingBox(-1, 0, 0, 1, 0, 1);
        float splitX = -0.5f;
        Axis xaxis = Axis.X;

        BoundingBox left = new BoundingBox(0, 0, 0, 0, 0, 0);
        BoundingBox right = new BoundingBox(0, 0, 0, 0, 0, 0);
        b1.split(xaxis, splitX, left, right);

        assertEquals(-1, left.xmin(), EPS);
        assertEquals(-0.5f, right.xmin(), EPS);
        assertEquals(-0.5f, left.xmax(), EPS);
        assertEquals(0, right.xmax(), EPS);
    }
}
