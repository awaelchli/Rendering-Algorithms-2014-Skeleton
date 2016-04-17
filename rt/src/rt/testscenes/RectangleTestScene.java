package rt.testscenes;

import rt.*;
import rt.bsp.BSPAccelerator;
import rt.cameras.PinholeCamera;
import rt.films.BoxFilterFilm;
import rt.integrators.PointLightIntegratorFactory;
import rt.intersectables.Instance;
import rt.intersectables.IntersectableList;
import rt.intersectables.Mesh;
import rt.intersectables.Rectangle;
import rt.lightsources.PointLight;
import rt.lightsources.RectangleLight;
import rt.materials.Diffuse;
import rt.samplers.RandomSamplerFactory;
import rt.tonemappers.ClampTonemapper;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import java.io.IOException;

public class RectangleTestScene extends Scene {

	public RectangleTestScene()
	{	
		outputFilename = new String("RectangleTestScene");
				
		// Specify pixel sampler to be used
		samplerFactory = new RandomSamplerFactory();
		
		// Samples per pixel
		SPP = 1; //512;
		outputFilename = outputFilename + " " + String.format("%d", SPP) + "SPP";
		
		// Make camera and film
		Vector3f eye = new Vector3f(0,1.f,6.f);
		Vector3f lookAt = new Vector3f(0,1.f,0.f);
		Vector3f up = new Vector3f(0.f,1.f,0.f);
		float fov = 60.f;
		int width = 256;//512;
		int height = 256;//512;
		float aspect = (float)width/(float)height;
		camera = new PinholeCamera(eye, lookAt, up, fov, aspect, width, height);
		film = new BoxFilterFilm(width, height);						
		tonemapper = new ClampTonemapper();
		
		// Specify integrator to be used
        PointLightIntegratorFactory iF = new PointLightIntegratorFactory();
        //iF.setSamplingDensity(10);
		integratorFactory = iF;

		
		// List of objects
		IntersectableList objects = new IntersectableList();	
						
		Rectangle rectangle = new Rectangle(new Point3f(0, 0, -0.1f), new Vector3f(3, 0, 0), new Vector3f(0, 3, 0));
		System.out.println(rectangle.normal());
		System.out.println(rectangle.anchor());
		rectangle.material = new Diffuse(new Spectrum(0.8f, 0.f, 0.f));
		objects.add(rectangle);

//		Point3f bottomLeft = new Point3f(-0.75f, 3.f, 1.5f);
//		Vector3f right = new Vector3f(0.f, 0.f, -0.5f);
//		Vector3f top = new Vector3f(0.5f, 0.f, 0.f);
//		RectangleLight rectangleLight = new RectangleLight(bottomLeft, right, top, new Spectrum(100.f, 100.f, 100.f));
//		objects.add(rectangleLight);
		
		// Connect objects to root
		root = objects;
				
		// List of lights
		lightList = new LightList();
		//lightList.add(rectangleLight);
		lightList.add(new PointLight(new Vector3f(0, 0, 0), new Spectrum(500, 500, 500)));
	}
	
}
