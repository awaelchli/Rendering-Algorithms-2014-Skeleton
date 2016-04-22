package rt.testscenes;

import rt.*;
import rt.bsp.BSPAccelerator;
import rt.cameras.PinholeCamera;
import rt.films.BoxFilterFilm;
import rt.importanceSampling.SamplingTechnique;
import rt.integrators.AreaLightIntegratorFactory;
import rt.intersectables.*;
import rt.lightsources.RectangleLight;
import rt.materials.Diffuse;
import rt.materials.Mirror;
import rt.samplers.RandomSamplerFactory;
import rt.tonemappers.ClampTonemapper;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import java.io.IOException;

public class SoftShadows extends Scene {

	public SoftShadows()
	{	
		outputFilename = new String("SoftShadows");
				
		// Specify pixel sampler to be used
		samplerFactory = new RandomSamplerFactory();
		
		// Samples per pixel
		SPP = 128;
		outputFilename = outputFilename + " " + String.format("%d", SPP) + "SPP";
		
		// Make camera and film
		Vector3f eye = new Vector3f(-3.f,1.f,4.f);
		Vector3f lookAt = new Vector3f(0.f,1.f,0.f);
		Vector3f up = new Vector3f(0.f,1.f,0.f);
		float fov = 60.f;
		int width = 512;
		int height = 512;
		float aspect = (float)width/(float)height;
		camera = new PinholeCamera(eye, lookAt, up, fov, aspect, width, height);
		film = new BoxFilterFilm(width, height);						
		tonemapper = new ClampTonemapper();
		
		// Specify integrator to be used
        AreaLightIntegratorFactory iF = new AreaLightIntegratorFactory();
		//PointLightIntegratorFactory iF = new PointLightIntegratorFactory();
		SamplingTechnique technique = SamplingTechnique.MIS;
		iF.setRecursionDepth(2);
		iF.setSamplingTechnique(technique);
		integratorFactory = iF;

		outputFilename = outputFilename + " " + width + "x" + height + " " + technique;

		// List of objects
		IntersectableList objects = new IntersectableList();	
						
		Rectangle rectangle = new Rectangle(new Point3f(2.f, -.75f, 2.f), new Vector3f(0.f, 4.f, 0.f), new Vector3f(0.f, 0.f, -4.f));
		rectangle.material = new Diffuse(new Spectrum(0.8f, 0.f, 0.f));
		objects.add(rectangle);
	
		// Bottom
		rectangle = new Rectangle(new Point3f(-2.f, -.75f, 2.f), new Vector3f(4.f, 0.f, 0.f), new Vector3f(0.f, 0.f, -4.f));
		rectangle.material = new Diffuse(new Spectrum(0.8f, 0.8f, 0.8f));
		objects.add(rectangle);

		// Top
		rectangle = new Rectangle(new Point3f(-2.f, 3.25f, 2.f), new Vector3f(0.f, 0.f, -4.f), new Vector3f(4.f, 0.f, 0.f));
		rectangle.material = new Diffuse(new Spectrum(0.8f, 0.8f, 0.8f));
		objects.add(rectangle);
		
		rectangle = new Rectangle(new Point3f(-2.f, -.75f, -2.f), new Vector3f(4.f, 0.f, 0.f), new Vector3f(0.f, 4.f, 0.f));
		rectangle.material = new Diffuse(new Spectrum(0.8f, 0.8f, 0.8f));
//			rectangle.material = new MirrorMaterial(new Spectrum(0.8f, 0.8f, 0.8f));
		objects.add(rectangle);
		
		// Add objects
		Sphere sphere = new Sphere(new Point3f(0, 0, 0), 1);
		sphere.material = new Diffuse(new Spectrum(1, 1, 1));
		objects.add(sphere);

		Point3f bottomLeft = new Point3f(-0.75f, 3.f, 1.5f);
		Vector3f right = new Vector3f(0.f, 0.f, -1.5f);
		Vector3f top = new Vector3f(1.5f, 0.f, 0.f);
		RectangleLight rectangleLight = new RectangleLight(bottomLeft, right, top, new Spectrum(200, 200, 200.f));
		objects.add(rectangleLight);
		
		// Connect objects to root
		root = objects;
				
		// List of lights
		lightList = new LightList();
		lightList.add(rectangleLight);
	}
	
}
