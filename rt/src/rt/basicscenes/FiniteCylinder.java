package rt.basicscenes;

import rt.LightGeometry;
import rt.LightList;
import rt.Scene;
import rt.Spectrum;
import rt.cameras.FixedCamera;
import rt.films.BoxFilterFilm;
import rt.integrators.PointLightIntegratorFactory;
import rt.intersectables.*;
import rt.lightsources.PointLight;
import rt.materials.Blinn;
import rt.samplers.OneSamplerFactory;
import rt.tonemappers.ClampTonemapper;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

/**
 * Created by Adrian on 09.03.2016.
 */
public class FiniteCylinder extends Scene {

    public FiniteCylinder()
    {
        // Output file name
        outputFilename = new String("FiniteCylinder");

        // Image width and height in pixels
        width = 512;
        height = 512;

        // Number of samples per pixel
        SPP = 1;

        // Specify which camera, film, and tonemapper to use
        camera = new FixedCamera(width, height);
        film = new BoxFilterFilm(width, height);
        tonemapper = new ClampTonemapper();

        // Specify which integrator and sampler to use
        integratorFactory = new PointLightIntegratorFactory();
        samplerFactory = new OneSamplerFactory();

        Matrix4f trafo = new Matrix4f();
        trafo.setIdentity();
        trafo.setTranslation(new Vector3f(0, 0, 0));
        Vector3f rotAxis = new Vector3f(1, 1, 1);
        rotAxis.normalize();
        trafo.setRotation(new AxisAngle4f(rotAxis, (float) Math.toRadians(60)));

        CSGFiniteCylinder cylinder = new CSGFiniteCylinder(0.8f, 1.5f);
        CSGInstance instance = new CSGInstance(cylinder, trafo);

        cylinder.setMaterial(new Blinn(new Spectrum(1, 0, 0), new Spectrum(0.5f, 0.5f, 0), 3), CSGFiniteCylinder.Face.BODY);
        cylinder.setMaterial(new Blinn(new Spectrum(1, 1, 0), new Spectrum(1, 1, 1), 3), CSGFiniteCylinder.Face.BOTTOM);

        CSGNode n1 = new CSGNode(new CSGPlane(new Vector3f(0.f, 1.f, 0.f), 1.f), instance, CSGNode.OperationType.ADD);
        root = new CSGNode(n1, new CSGPlane(new Vector3f(0.f, 0.f, 1.f), 1.f), CSGNode.OperationType.ADD);

        // Light sources
        LightGeometry pointLight = new PointLight(new Vector3f(0.f, 0.f, 3.f), new Spectrum(15.f, 15.f, 15.f));
        lightList = new LightList();
        lightList.add(pointLight);
    }

}
