package rt.testscenes;

import rt.Spectrum;
import rt.integrators.PathTracingIntegratorFactory;
import rt.intersectables.IntersectableList;
import rt.intersectables.Sphere;
import rt.materials.Mirror;
import rt.materials.Refractive;

import javax.vecmath.Point3f;

/**
 * Created by adrian on 25.04.16.
 */
public class CornellBoxAndSpheres extends CornellBox
{
    public CornellBoxAndSpheres()
    {
        super();
        outputFilename = new String("CornellBoxAndSpheres");
        SPP = 128;
        outputFilename += " " + SPP + "SPP";

        PathTracingIntegratorFactory iF = new PathTracingIntegratorFactory();
        iF.setMaxDepth(10);
        iF.setMinDepth(2);
        iF.setTerminationProbability(0.5f);
        integratorFactory = iF;
    }

    @Override
    protected void build()
    {
        super.build();

        IntersectableList objects = new IntersectableList();

        // Sphere on top of short block
        float radius = 50;
        Sphere sphere1 = new Sphere(new Point3f(150, 165 + radius, 100), radius);
        sphere1.material = new Refractive(1f);
        objects.add(sphere1);

        // Sphere on top of tall block
        radius = 50;
        Sphere sphere2 = new Sphere(new Point3f(350, 330 + radius, 325), radius);
        sphere2.material = new Mirror();
        objects.add(sphere2);

        objects.add(root);
        root = objects;

    }
}
