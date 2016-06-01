package rt.testscenes;

/**
 * Created by Adrian on 5/31/2016.
 */

import rt.LightList;
import rt.Scene;
import rt.Spectrum;
import rt.cameras.ApertureCamera;
import rt.cameras.PinholeCamera;
import rt.films.BoxFilterFilm;
import rt.integrators.BDPathTracingIntegratorFactory;
import rt.integrators.PathTracingIntegratorFactory;
import rt.intersectables.*;
import rt.lightsources.RectangleLight;
import rt.materials.*;
import rt.media.Beer;
import rt.media.Milk;
import rt.samplers.RandomSamplerFactory;
import rt.textures.NormalMap;
import rt.textures.Texture;
import rt.tonemappers.ClampTonemapper;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;


/**
 * Created by Adrian on 09.03.2016.
 */
public class ParticipatingMediaTestScene extends Scene
{

    public ParticipatingMediaTestScene() {

        // Output file name
        outputFilename = new String("output/testscenes/assignment6/ParticipatingMedia/PM");

        // Image width and height in pixels
        width = 128;
        height = 128;

        // Number of samples per pixel
        SPP = 40;
        outputFilename += " " + SPP + "SPP";

        // Specify which camera, film, and tonemapper to use
        Vector3f eye = new Vector3f(0, 2f, 3.5f);
        Vector3f lookAt = new Vector3f(0, 2f, -1f); //1.2, 1, -1
        Vector3f up = new Vector3f(0.f, 1.f, 0.f);
        float fov = 60.f;
        float aspect = 1;
        float aperture = 0.02f;
        float focalDistance = 0f;
        camera = new PinholeCamera(eye, lookAt, up, fov, aspect, width, height);
        film = new BoxFilterFilm(width, height);
        tonemapper = new ClampTonemapper();

        epsilon = 0.01f;

        int s = 5;
        int t = 10;
        float rrProbability = 0.5f;

        outputFilename += String.format(" s=%d t=%d rr=%.2f", s, t, rrProbability);

        // Specify integrator to be used
//        BDPathTracingIntegratorFactory factory = new BDPathTracingIntegratorFactory();
//        factory.setMinLightVertices(s);
//        factory.setMaxLightVertices(s);
//        factory.setMinEyeVertices(t);
//        factory.setMaxEyeVertices(t);
//        factory.setEyePathTerminationProbability(rrProbability);
//        factory.setLightPathTerminationProbability(rrProbability);

        PathTracingIntegratorFactory factory = new PathTracingIntegratorFactory();
        factory.setMaxDepth(t);
        factory.setMinDepth(t);
        factory.setTerminationProbability(rrProbability);
        factory.setShadowRayContributionThreshold(0);

        integratorFactory = factory;
        samplerFactory = new RandomSamplerFactory();

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
//        BlinnTexture blinn_brick = new BlinnTexture(tex_brick, new Spectrum(1, 1, 1), 20);
//        BlinnTexture blinn_stone = new BlinnTexture(tex_stone, new Spectrum(1, 1, 1), 50);
        DiffuseTexture blinn_brick = new DiffuseTexture(tex_brick);
        DiffuseTexture blinn_stone = new DiffuseTexture(tex_stone);
        BumpMaterial bumpBrickWall = new BumpMaterial(blinn_brick, new NormalMap(map_brick));
        BumpMaterial bumpStoneWall = new BumpMaterial(blinn_stone, new NormalMap(map_stone));

        // Ground plane
        Plane ground = new Plane(new Vector3f(0.f, 1.f, 0.f), 0);
        ground.material = new DiffuseTexture(tex_wood);

        // Background plane of room
        Plane back = new Plane(new Vector3f(0.f, 0.f, 1f), 4);
        back.material = blinn_stone;//bumpStoneWall;

        // Left plane of room
        Plane left = new Plane(new Vector3f(1.f, 0.f, 0f), 3);
        left.material = blinn_brick;//bumpBrickWall;

        // Front plane of room
        Plane front = new Plane(new Vector3f(0.f, 0.f, -1f), 4);
        front.material = blinn_stone;//bumpStoneWall;

        // Ground plane
        Plane roof = new Plane(new Vector3f(0.f, -1.f, 0.f), 4);
        roof.material = new DiffuseTexture(tex_wood);

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
        DiffuseTexture blinn_earth = new DiffuseTexture(tex_earth);
        sphere.material = new BumpMaterial(blinn_earth, new NormalMap(map_earth));
        trafo.setIdentity();
        Vector3f ax = new Vector3f(1, 1, 0);
        ax.normalize();
        trafo.setRotation(new AxisAngle4f(ax, (float) Math.toRadians(120)));
        trafo.setTranslation(new Vector3f(0.7f, 1.1f, 0.5f));
        Instance sphereInstance = new Instance(sphere, trafo);

        Sphere glassSphere = new Sphere(new Point3f(-1f, 1.6f, -1), 1);
        glassSphere.material = new Beer();
        //trafo.setIdentity();
        //trafo.setTranslation(new Vector3f(-1f, 1.6f, -1));
        //Instance glassSphereInstance = new Instance(glassSphere, trafo);

        Sphere diffuseSphere = new Sphere(new Point3f(), 1);
//        diffuseSphere.material = new Diffuse(new Spectrum(0, 0.7f, 0.9f));
        diffuseSphere.material = new Milk();
        trafo.setIdentity();
        trafo.setTranslation(new Vector3f(-1f, 1.4f, -1));
        Instance diffuseSphereInstance = new Instance(diffuseSphere, trafo);

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
        sceneObjects.add(front);
        sceneObjects.add(roof);
//        sceneObjects.add(mirrorCube);
//        sceneObjects.add(sphereInstance);
//        sceneObjects.add(glassSphereInstance);
        //sceneObjects.add(glassSphere);
        sceneObjects.add(diffuseSphereInstance);
        root = sceneObjects;

        // Light source
        Point3f bottomLeft = new Point3f(1f, 3.7f, 0f);
        Vector3f right = new Vector3f(0, 0, 1);
        Vector3f top = new Vector3f(-1, 0, 0);
        Spectrum emission = new Spectrum(100, 100, 100);
        emission.mult(2);
        RectangleLight light1 = new RectangleLight(bottomLeft, right, top, emission);

        lightList = new LightList();
        lightList.add(light1);
        sceneObjects.add(light1);
    }

    @Override
    public void prepare()
    {
        super.prepare();
        integratorFactory.prepareScene(this);
    }

    @Override
    public void finish()
    {
        if(integratorFactory instanceof BDPathTracingIntegratorFactory)
        {
            ((BDPathTracingIntegratorFactory) integratorFactory).writeLightImage("output/testscenes/assignment6/DepthOfField/lightimageDOF");
            ((BDPathTracingIntegratorFactory) integratorFactory).addLightImage(film);
        }
    }

}

