package rt.testscenes;

import rt.LightList;
import rt.ObjReader;
import rt.Scene;
import rt.Spectrum;
import rt.bsp.BSPAccelerator;
import rt.cameras.PinholeCamera;
import rt.films.BoxFilterFilm;
import rt.importanceSampling.SamplingTechnique;
import rt.integrators.AreaLightIntegratorFactory;
import rt.intersectables.*;
import rt.lightsources.RectangleLight;
import rt.materials.*;
import rt.samplers.OneSamplerFactory;
import rt.samplers.RandomSampler;
import rt.samplers.RandomSamplerFactory;
import rt.tonemappers.ClampTonemapper;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import java.io.IOException;

/**
 * Created by Adrian on 16.04.2016.
 */
public class MicrofacetTest extends Scene
{
    public MicrofacetTest()
    {
        // Output file name
        outputFilename = new String("MicrofacetTest");

        // Image width and height in pixels
        width = 512;
        height = 512;

        // Number of samples per pixel
        SPP = 1;

        // Specify which camera, film, and tonemapper to use
        Vector3f eye = new Vector3f(2.5f, 2.5f, 3.5f);
        Vector3f lookAt = new Vector3f(0.5f, 1, 0);
        Vector3f up = new Vector3f(0.f, 1.f, 0.f);
        float fov = 60.f;
        float aspect = 1;
        camera = new PinholeCamera(eye, lookAt, up, fov, aspect, width, height);
        film = new BoxFilterFilm(width, height);
        tonemapper = new ClampTonemapper();

        epsilon = 0.01f;

        // Specify which integrator and sampler to use
        AreaLightIntegratorFactory factory = new AreaLightIntegratorFactory();
        SamplingTechnique technique = SamplingTechnique.Light;
        factory.setSamplingTechnique(technique);
        integratorFactory = factory;
        samplerFactory = new RandomSamplerFactory();

        outputFilename = outputFilename + " " + width + "x" + height + " " + technique;

        // Materials
        Glossy glossy1 = new Glossy(30, new Spectrum(0.9f, 0.5f, 1f), new Spectrum(0.1f, 0.2f, 0.2f), new Spectrum(0.3f, 0.3f, 0.3f));
        Glossy glossy2 = new Glossy(100, new Spectrum(1.8f, 1, 0.1f), new Spectrum(2, 3, 4), new Spectrum(1, 0.8f, 0));

        // Ground plane
        Plane ground = new Plane(new Vector3f(0.f, 1.f, 0.f), 0);
        ground.material = glossy1;

        // Background plane of room
        Plane back = new Plane(new Vector3f(0.f, 0.f, 1f), 7);
        back.material = new Diffuse(new Spectrum(0.8f, 0, 0));

        // Left plane of room
        Plane left = new Plane(new Vector3f(1.f, 0.f, 0f), 3);
        left.material = new Diffuse(new Spectrum(0, 0.8f, 0));

        // Teapot
        Mesh mesh;
        try
        {

            mesh = ObjReader.read("obj/wt_teapot.obj", 1f);
        } catch(IOException e)
        {
            System.out.printf("Could not read .obj file\n");
            return;
        }
        mesh.material = glossy2;

        BSPAccelerator meshAccelerator = new BSPAccelerator(mesh, 5, 20);
        meshAccelerator.construct();

        // Transform the teapot
        Matrix4f t = new Matrix4f();
        t.setIdentity();
        t.setScale(1.5f);
        t.setTranslation(new Vector3f(0.f, 0.5f, 0));
        Instance teapotInstance = new Instance(meshAccelerator, t);

        // Add objects to scene graph
        IntersectableList sceneObjects = new IntersectableList();
        sceneObjects.add(ground);
        sceneObjects.add(back);
        sceneObjects.add(left);
        sceneObjects.add(teapotInstance);

        // Add the light 1
        Point3f bottomLeft = new Point3f(-0.75f, 3f, 0f);
        Vector3f right = new Vector3f(0.f, 0.f, -0.5f);
        Vector3f top = new Vector3f(0.5f, 0.f, 0.f);
        RectangleLight rectangleLight = new RectangleLight(bottomLeft, right, top, new Spectrum(200, 200, 200));
        sceneObjects.add(rectangleLight);

        // Add light 2
        Point3f bottomLeft2 = new Point3f(2.5f, 1, 1f);
        Vector3f right2 = new Vector3f(-0.5f, 0, 0.5f);
        Vector3f top2 = new Vector3f(0, 0.4f, 0);
        RectangleLight rectangleLight2 = new RectangleLight(bottomLeft2, right2, top2, new Spectrum(100, 100, 100));
        sceneObjects.add(rectangleLight2);

        root = sceneObjects;

        lightList = new LightList();
        lightList.add(rectangleLight);
        lightList.add(rectangleLight2);
    }

}
