package rt.testscenes;

import rt.integrators.BDPathTracingIntegratorFactory;
import rt.integrators.PathTracingIntegratorFactory;
import rt.intersectables.IntersectableList;
import rt.intersectables.Sphere;
import rt.materials.Mirror;
import rt.materials.Refractive;

import javax.vecmath.Point3f;

/**
 * Created by adrian on 25.04.16.
 */
public class BDCornellBoxAndSpheres extends CornellBoxAndSpheres
{
    public BDCornellBoxAndSpheres()
    {
        super();
        outputFilename = new String("output/testscenes/assignment5/BDPathTracing/BDCornellBoxAndSpheres");
        SPP = 128;
        outputFilename += " " + SPP + "SPP";

        int s = 10;
        int t = 10;
        float rrProbability = 0.5f;

        outputFilename += String.format(" s=%d t=%d rr=%.2f", s, t, rrProbability);

        // Specify integrator to be used
        BDPathTracingIntegratorFactory factory = new BDPathTracingIntegratorFactory();
        factory.setMinLightVertices(s);
        factory.setMaxLightVertices(s);
        factory.setMinEyeVertices(t);
        factory.setMaxEyeVertices(t);
        factory.setEyePathTerminationProbability(rrProbability);
        factory.setLightPathTerminationProbability(rrProbability);
        integratorFactory = factory;
    }

    @Override
    public void finish()
    {
        if(integratorFactory instanceof BDPathTracingIntegratorFactory)
        {
            ((BDPathTracingIntegratorFactory) integratorFactory).writeLightImage("output/testscenes/assignment5/BDPathTracing/lightimageBoxSphereGlass");
            ((BDPathTracingIntegratorFactory) integratorFactory).addLightImage(film);
        }
    }

    @Override
    public void prepare()
    {
        super.prepare();
        integratorFactory.prepareScene(this);
    }
}
