package rt.basicscenes;

import rt.*;
import rt.cameras.PinholeCamera;
import rt.films.BoxFilterFilm;
import rt.integrators.PointLightIntegratorFactory;
import rt.intersectables.*;
import rt.lightsources.PointLight;
import rt.materials.BlinnTexture;
import rt.materials.DiffuseTexture;
import rt.materials.XYZGrid;
import rt.samplers.OneSamplerFactory;
import rt.textures.Texture;
import rt.tonemappers.ClampTonemapper;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

/**
 * Created by Adrian on 09.03.2016.
 */
public class TextureMappingDemo extends Scene {

    public TextureMappingDemo() {

        // Output file name
        outputFilename = new String("TextureMappingDemo");

        // Image width and height in pixels
        width = 1024;
        height = 1024;

        // Number of samples per pixel
        SPP = 1;

        // Specify which camera, film, and tonemapper to use
        Vector3f eye = new Vector3f(0, 0, 3);
        Vector3f lookAt = new Vector3f(0, 0.5f, 0);
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

        // Create sphere with texture of earth
        Texture earthTex = new Texture("textures/earth1.jpg", Texture.InterpolationMethod.bilinear);
        Material earthMaterial = new BlinnTexture(earthTex, new Spectrum(0.3f, 0.3f, 0.3f), 20);

        earthMaterial = new DiffuseTexture(earthTex);

        Sphere earth = new Sphere(new Point3f(0, 0, 0), 1);
        earth.material = earthMaterial;

        Matrix4f trafo = new Matrix4f();
        trafo.setIdentity();
        trafo.setRotation(new AxisAngle4f(new Vector3f(0, 1, 0), (float) Math.toRadians(-30)));
        Instance earth_rot = new Instance(earth, trafo);

        IntersectableList sceneObjects = new IntersectableList();
        sceneObjects.add(earth_rot);
        root = sceneObjects;

        // Light sources
        float strength = 500;
        LightGeometry sun = new PointLight(new Vector3f(0, 10, 0), new Spectrum(strength, strength, strength));
        lightList = new LightList();
        lightList.add(sun);
    }

}
