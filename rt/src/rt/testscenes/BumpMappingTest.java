package rt.testscenes;

import rt.LightGeometry;
import rt.LightList;
import rt.Scene;
import rt.Spectrum;
import rt.cameras.PinholeCamera;
import rt.films.BoxFilterFilm;
import rt.integrators.PointLightIntegratorFactory;
import rt.integrators.WhittedIntegratorFactory;
import rt.intersectables.*;
import rt.lightsources.PointLight;
import rt.materials.*;
import rt.samplers.OneSamplerFactory;
import rt.textures.BumpMap;
import rt.textures.NormalMap;
import rt.textures.Texture;
import rt.tonemappers.ClampTonemapper;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import javax.xml.soap.Text;

/**
 * Created by Adrian on 09.03.2016.
 */
public class BumpMappingTest extends Scene {

    public BumpMappingTest() {

        // Output file name
        outputFilename = new String("BumpMappingTest");

        // Image width and height in pixels
        width = 2 * 1024;
        height = 2 * 1024;

        // Number of samples per pixel
        SPP = 1;

        // Specify which camera, film, and tonemapper to use
        Vector3f eye = new Vector3f(2.5f, 3f, 3.5f);
        Vector3f lookAt = new Vector3f(0.5f, 1, 0);
        Vector3f up = new Vector3f(0.f, 1.f, 0.f);
        float fov = 60.f;
        float aspect = 1;
        camera = new PinholeCamera(eye, lookAt, up, fov, aspect, width, height);
        film = new BoxFilterFilm(width, height);
        tonemapper = new ClampTonemapper();

        epsilon = 0.01f;

        // Specify which integrator and sampler to use
        WhittedIntegratorFactory factory = new WhittedIntegratorFactory();
        factory.setRecursionDepth(7);
        integratorFactory = factory;
        samplerFactory = new OneSamplerFactory();

        // Textures
        Texture map1 = new Texture("textures/normal-maps/map1.jpg");
        Texture map2 = new Texture("textures/normal-maps/circle-grid.png");
        Texture map_brick = new Texture("textures/normal-maps/178_norm.JPG");
        Texture tex_brick = new Texture("textures/normal-maps/178.JPG");
        Texture tex_wood = new Texture("textures/wood2.jpg");
        Texture tex_stone = new Texture("textures/normal-maps/200.JPG");
        Texture map_stone = new Texture("textures/normal-maps/200_norm.JPG");
        Texture tex_earth = new Texture("textures/earth1.jpg");
        Texture map_earth = new Texture("textures/earth1_norm.jpg");
        Texture orientTest = new Texture("textures/orientation_test.PNG");
        map2.setScale(0.1f);
        tex_wood.setScale(10);
        tex_brick.setScale(5);
        map_brick.setScale(5);
        tex_stone.setScale(5);
        map_stone.setScale(5);

        // Materials
        Refractive glass = new Refractive(1.5f);
        BlinnTexture blinn_brick = new BlinnTexture(tex_brick, new Spectrum(1, 1, 1), 20);
        BlinnTexture blinn_stone = new BlinnTexture(tex_stone, new Spectrum(1, 1, 1), 50);
        BumpMaterial bumpBrickWall = new BumpMaterial(blinn_brick, new NormalMap(map_brick));
        BumpMaterial bumpStoneWall = new BumpMaterial(blinn_stone, new NormalMap(map_stone));

        // Ground plane
        Plane ground = new Plane(new Vector3f(0.f, 1.f, 0.f), 0);
        ground.material = new DiffuseTexture(tex_wood);

        // Background plane of room
        Plane back = new Plane(new Vector3f(0.f, 0.f, 1f), 7);
        back.material = bumpStoneWall;

        // Left plane of room
        Plane left = new Plane(new Vector3f(1.f, 0.f, 0f), 3);
        left.material = bumpBrickWall;

        // Front plane of room
        Plane front = new Plane(new Vector3f(0.f, 0.f, -1f), 6);
        front.material = new XYZGrid(new Spectrum(1.f,1.f,0.f), new Spectrum(1.f, 1.f, 1.f), 1f);

        Matrix4f trafo = new Matrix4f();
        trafo.setIdentity();

//        CSGCube cube_ = new CSGCube();
//        NormalMap normalMap1 = new NormalMap(map1);
//        Blinn m1 = new Blinn(new Spectrum(0, 0.5f, 0), new Spectrum(1, 1, 1), 4);
//        Blinn m2 = new Blinn(new Spectrum(0.5f, 0.5f, 0), new Spectrum(1, 1, 1), 50);
//        Blinn m3 = new Blinn(new Spectrum(0.9f, 0, 0), new Spectrum(1, 1, 1), 50);
//        BumpMaterial cubeMaterial1 = new BumpMaterial(m1, normalMap1);
//        BumpMaterial cubeMaterial2 = new BumpMaterial(m2, normalMap1);
//        BumpMaterial cubeMaterial3 = new BumpMaterial(m3, normalMap1);
//        cube_.setMaterial(cubeMaterial1, CSGCube.Face.FRONT);
//        cube_.setMaterial(cubeMaterial2, CSGCube.Face.TOP);
//        cube_.setMaterial(cubeMaterial3, CSGCube.Face.RIGHT);
//        CSGInstance cube = new CSGInstance(cube_, trafo);


        Sphere sphere = new Sphere(new Point3f(), 1);
        BlinnTexture blinn_earth = new BlinnTexture(tex_earth, new Spectrum(0.1f, 0.1f, 0.1f), 20);
        sphere.material = new BumpMaterial(blinn_earth, new NormalMap(map_earth));
        trafo.setIdentity();
        Vector3f ax = new Vector3f(1, 1, 0);
        ax.normalize();
        trafo.setRotation(new AxisAngle4f(ax, (float) Math.toRadians(100)));
        trafo.setTranslation(new Vector3f(0.5f, 1.1f, 0));
        Instance sphereInstance = new Instance(sphere, trafo);

        // Mirror cube
        CSGCube mirrorCube_ = new CSGCube();
        mirrorCube_.setMaterial(new BlinnPlusMirror(new Spectrum(0, 0, 0), new Spectrum(0, 0, 0), 1, new Spectrum(1, 1, 1)));
        trafo.setIdentity();
        trafo.setTranslation(new Vector3f(1, 1f, -3f));
        trafo.setRotation(new AxisAngle4f(new Vector3f(0, 1, 0), (float) Math.toRadians(10)));
        CSGInstance mirrorCube = new CSGInstance(mirrorCube_, trafo);

        // Add objects to scene graph
        IntersectableList sceneObjects = new IntersectableList();
        sceneObjects.add(ground);
        sceneObjects.add(back);
        sceneObjects.add(left);
        //sceneObjects.add(front);
        sceneObjects.add(mirrorCube);
        sceneObjects.add(sphereInstance);
        root = sceneObjects;

        // Light sources
        LightGeometry pointLight1 = new PointLight(new Vector3f(-1, 3, -2), new Spectrum(30, 30, 30));
        LightGeometry pointLight2 = new PointLight(new Vector3f(0, 4, -1), new Spectrum(20, 20, 20));
        LightGeometry pointLight3 = new PointLight(new Vector3f(3, 4.5f, 4.5f), new Spectrum(30, 30, 30));
        LightGeometry pointLight4 = new PointLight(new Vector3f(-2, 4.5f, 3.5f), new Spectrum(30, 30, 30));
        LightGeometry pointLight5 = new PointLight(new Vector3f(2, 4f, 3.5f), new Spectrum(30, 30, 30));
        lightList = new LightList();
        lightList.add(pointLight1);
        lightList.add(pointLight2);
        lightList.add(pointLight3);
        lightList.add(pointLight4);
        lightList.add(pointLight5);
    }

}
