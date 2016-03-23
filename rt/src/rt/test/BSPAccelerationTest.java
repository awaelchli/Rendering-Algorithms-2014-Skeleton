package rt.test;

import org.junit.Before;
import org.junit.Test;
import rt.bsp.Axis;
import rt.bsp.AABoundingBox;

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
        AABoundingBox b1 = new AABoundingBox(-1, 1, -1, 1, -1, 1);
        AABoundingBox b2 = new AABoundingBox(0, 2, 0, 2, 0, 2);
        AABoundingBox b3 = new AABoundingBox(1, 2, 1, 2, 1, 2);
        AABoundingBox b4 = new AABoundingBox(0, 2, 0, 2, 3, 4);

        assertTrue(b1.isIntersecting(b2));
        assertTrue(b2.isIntersecting(b1));
        assertTrue(b1.isIntersecting(b3));
        assertFalse(b1.isIntersecting(b4));
    }

    @Test
    public void testBoundingBoxAddition()
    {
        AABoundingBox b1 = new AABoundingBox(-1, 0, 0, 1, 0, 1);
        AABoundingBox b2 = new AABoundingBox(0, 1, 0, 1, 0, 1);

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
        AABoundingBox b1 = new AABoundingBox(-1, 0, 0, 1, 0, 1);
        float splitX = -0.5f;
        Axis xaxis = Axis.X;

        AABoundingBox[] boxes = b1.split(xaxis, splitX);
        AABoundingBox left = boxes[0];
        AABoundingBox right = boxes[1];

        assertEquals(-1, left.xmin(), EPS);
        assertEquals(-0.5f, right.xmin(), EPS);
        assertEquals(-0.5f, left.xmax(), EPS);
        assertEquals(0, right.xmax(), EPS);
    }
}
