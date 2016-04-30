package rt.testscenes;

import javax.vecmath.*;
import rt.*;
import rt.intersectables.*;
import rt.tonemappers.*;
import rt.integrators.*;
import rt.lightsources.*;
import rt.materials.*;
import rt.samplers.*;
import rt.cameras.*;
import rt.films.*;

public class PathtracingBoxSphere extends Scene {
	
	public PathtracingBoxSphere()
	{
		outputFilename = new String("output/testscenes/assignment5/PathtracingBoxSphere");
				
		// Specify pixel sampler to be used
		samplerFactory = new RandomSamplerFactory();
		
		// Samples per pixel
		SPP = 1024;
		outputFilename = outputFilename + " " + String.format("%d", SPP) + "SPP";
		
		// Make camera and film
		Vector3f eye = new Vector3f(-3.f,1.f,4.f);
		Vector3f lookAt = new Vector3f(0.f,1.f,0.f);
		Vector3f up = new Vector3f(0.f,1.f,0.f);
		float fov = 60.f;
		int width = 128;
		int height = 128;
		float aspect = (float)width/(float)height;
		camera = new PinholeCamera(eye, lookAt, up, fov, aspect, width, height);
		film = new BoxFilterFilm(width, height);						
		tonemapper = new ClampTonemapper();

		int minDepth = 2;
		int maxDepth = 10;
		float rrProbability = 0.5f;

		outputFilename += String.format(" minDepth=%d maxDepth=%d rr=%.2f", minDepth, maxDepth, rrProbability);
		
		// Specify integrator to be used
        PathTracingIntegratorFactory factory = new PathTracingIntegratorFactory();
		factory.setMaxDepth(maxDepth);
		factory.setMinDepth(minDepth);
		factory.setTerminationProbability(rrProbability);
        integratorFactory = factory;
		
		// List of objects
		IntersectableList objects = new IntersectableList();	
		
		Sphere sphere = new Sphere(new Point3f(-.5f,-.2f,1.f), .5f);
		sphere.material = new Diffuse(new Spectrum(0.8f, 0.8f, 0.8f));
		objects.add(sphere);

		// Right, red wall
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
		
		// Left
		rectangle = new Rectangle(new Point3f(-2.f, -.75f, -2.f), new Vector3f(4.f, 0.f, 0.f), new Vector3f(0.f, 4.f, 0.f));
		rectangle.material = new Diffuse(new Spectrum(0.8f, 0.8f, 0.8f));
		objects.add(rectangle);
		
		// Light source
		Point3f bottomLeft = new Point3f(-0.25f, 3.f, 0.25f);
		Vector3f right = new Vector3f(0.f, 0.f, -0.5f);
		Vector3f top = new Vector3f(0.5f, 0.f, 0.f);
		RectangleLight rectangleLight = new RectangleLight(bottomLeft, right, top, new Spectrum(100.f, 100.f, 100.f));
		objects.add(rectangleLight);
		
		// Connect objects to root
		root = objects;
				
		// List of lights
		lightList = new LightList();
		lightList.add(rectangleLight);
	}
	
}
