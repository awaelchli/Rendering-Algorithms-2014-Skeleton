package rt.testscenes;

import rt.*;
import rt.bsp.BSPAccelerator;
import rt.cameras.PinholeCamera;
import rt.films.BoxFilterFilm;
import rt.importanceSampling.SamplingTechnique;
import rt.integrators.AreaLightIntegratorFactory;
import rt.intersectables.Instance;
import rt.intersectables.IntersectableList;
import rt.intersectables.Mesh;
import rt.intersectables.Rectangle;
import rt.lightsources.RectangleLight;
import rt.materials.Diffuse;
import rt.samplers.RandomSamplerFactory;
import rt.tonemappers.ClampTonemapper;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import java.io.IOException;

public class PathtracingBoxCar extends Scene {
	
	public PathtracingBoxCar()
	{	
		outputFilename = new String("PathtracingBoxCar");
				
		// Specify pixel sampler to be used
		samplerFactory = new RandomSamplerFactory();
		
		// Samples per pixel
		SPP = 1;
		outputFilename = outputFilename + " " + String.format("%d", SPP) + "SPP";
		
		// Make camera and film
		Vector3f eye = new Vector3f(-3.f,1.f,4.f);
		Vector3f lookAt = new Vector3f(0.f,1.f,0.f);
		Vector3f up = new Vector3f(0.f,1.f,0.f);
		float fov = 60.f;
		int width = 1024;
		int height = 1024;
		float aspect = (float)width/(float)height;
		camera = new PinholeCamera(eye, lookAt, up, fov, aspect, width, height);
		film = new BoxFilterFilm(width, height);						
		tonemapper = new ClampTonemapper();
		
		// Specify integrator to be used
        AreaLightIntegratorFactory iF = new AreaLightIntegratorFactory();
		iF.setSamplingTechnique(SamplingTechnique.Light);
		integratorFactory = iF;

		
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
		Timer timer = new Timer();
		Mesh mesh;
		BSPAccelerator accelerator;
		try
		{
			
			mesh = ObjReader.read("obj/Specter_GT3.obj", 1.f);
			timer.reset();
			accelerator = new BSPAccelerator(mesh, 5, 10);
            accelerator.construct();
			System.out.printf("Accelerator computed in %d ms.\n", timer.timeElapsed());
			
			Matrix4f t = new Matrix4f();
			t.setIdentity();
			t.setScale(2.f);
			t.setTranslation(new Vector3f(0.f, -0.2f, 0.f));
			Instance instance = new Instance(accelerator, t);
			objects.add(instance);
		} catch(IOException e) 
		{
			System.out.printf("Could not read .obj file\n");
		}
	
		Point3f bottomLeft = new Point3f(-0.75f, 3.f, 1.5f);
		Vector3f right = new Vector3f(0.f, 0.f, -0.5f);
		Vector3f top = new Vector3f(0.5f, 0.f, 0.f);
		RectangleLight rectangleLight = new RectangleLight(bottomLeft, right, top, new Spectrum(100, 100, 100.f));
		objects.add(rectangleLight);
		
		// Connect objects to root
		root = objects;
				
		// List of lights
		lightList = new LightList();
		lightList.add(rectangleLight);
	}
	
}
