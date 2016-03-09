package rt.basicscenes;

import rt.LightGeometry;
import rt.LightList;
import rt.Scene;
import rt.Spectrum;
import rt.cameras.PinholeCamera;
import rt.films.BoxFilterFilm;
import rt.integrators.PointLightIntegratorFactory;
import rt.intersectables.*;
import rt.lightsources.PointLight;
import rt.materials.Blinn;
import rt.materials.XYZGrid;
import rt.samplers.OneSamplerFactory;
import rt.tonemappers.ClampTonemapper;

import javax.vecmath.*;

/**
 * Created by Adrian on 09.03.2016.
 */
public class CSGDemo extends Scene {

    public CSGDemo () {

        // Output file name
        outputFilename = new String("CSGDemo");

        // Image width and height in pixels
        width = 1024;
        height = 1024;

        // Number of samples per pixel
        SPP = 1;

        // Specify which camera, film, and tonemapper to use
        Vector3f eye = new Vector3f(2, 3, 3);
        Vector3f lookAt = new Vector3f(0, 1, 0);
        Vector3f up = new Vector3f(0.f, 1.f, 0.f);
        float fov = 60.f;
        float aspect = 1;
        camera = new PinholeCamera(eye, lookAt, up, fov, aspect, width, height);
        film = new BoxFilterFilm(width, height);
        tonemapper = new ClampTonemapper();

        // Specify which integrator and sampler to use
        integratorFactory = new PointLightIntegratorFactory();
        samplerFactory = new OneSamplerFactory();

        // Ground plane
        Plane plane = new Plane(new Vector3f(0.f, 1.f, 0.f), 0);
        plane.material = new XYZGrid(new Spectrum(1.f,0.f,0.f), new Spectrum(1.f, 1.f, 1.f), 1f);

        Matrix4f trafo = new Matrix4f();
        trafo.setIdentity();
        CSGCube cube_ = new CSGCube();
        cube_.setMaterial(new Blinn(new Spectrum(0, 0.5f, 0), new Spectrum(1, 1, 1), 4), CSGCube.Face.FRONT);
        cube_.setMaterial(new Blinn(new Spectrum(0.5f, 0.5f, 0), new Spectrum(1, 1, 1), 50), CSGCube.Face.TOP);
        cube_.setMaterial(new Blinn(new Spectrum(0.9f, 0, 0), new Spectrum(1, 1, 1), 50), CSGCube.Face.RIGHT);
        CSGInstance cube = new CSGInstance(cube_, trafo);
        CSGSphere sphere_ = new CSGSphere(new Point3f(0, 0, 0), 1.35f);
        sphere_.material = new Blinn(new Spectrum(0, 0, 0.5f), new Spectrum(1, 1, 1), 50);
        CSGInstance sphere = new CSGInstance(sphere_);

        // Intersection of cube and sphere
        CSGNode node1 = new CSGNode(cube, sphere, CSGNode.OperationType.INTERSECT);

        // Union of cylinders
        CSGInfiniteCylinder cylinderZ = new CSGInfiniteCylinder(0.7f);
        cylinderZ.material = new Blinn(new Spectrum(0.2f, 0.2f, 0.2f), new Spectrum(0.2f, 0.2f, 0.2f), 3);

        trafo.setIdentity();
        trafo.setRotation(new AxisAngle4f(new Vector3f(0, 1, 0), (float) Math.PI / 2));
        CSGInstance cylinderX = new CSGInstance(cylinderZ, trafo);

        trafo.setIdentity();
        trafo.setRotation(new AxisAngle4f(new Vector3f(1, 0, 0), (float) Math.PI / 2));
        CSGInstance cylinderY = new CSGInstance(cylinderZ, trafo);

        CSGNode node2 = new CSGNode(cylinderX, cylinderY, CSGNode.OperationType.ADD);
        CSGNode node3 = new CSGNode(node2, cylinderZ, CSGNode.OperationType.ADD);

        // Subtract cylinder cross from cube
        CSGNode node4 = new CSGNode(node1, node3, CSGNode.OperationType.SUBTRACT);

        // translate everything
        trafo.setIdentity();
        trafo.setTranslation(new Vector3f(0, 1, 0));
        CSGInstance cube_cylinders_sphere = new CSGInstance(node4, trafo);

        IntersectableList sceneObjects = new IntersectableList();
        sceneObjects.add(plane);
        sceneObjects.add(cube_cylinders_sphere);
        root = sceneObjects;

        // Light sources
        LightGeometry pointLight1 = new PointLight(new Vector3f(0.f, 3, -3), new Spectrum(30, 30, 30));
        LightGeometry pointLight2 = new PointLight(new Vector3f(0, 4, 0), new Spectrum(20, 20, 20));
        LightGeometry pointLight3 = new PointLight(new Vector3f(3, 4.5f, 4.5f), new Spectrum(30, 30, 30));
        lightList = new LightList();
        lightList.add(pointLight1);
        lightList.add(pointLight2);
        lightList.add(pointLight3);
    }

}
