package rt.testscenes;

import rt.*;
import rt.bsp.BSPAccelerator;
import rt.cameras.PinholeCamera;
import rt.films.BoxFilterFilm;
import rt.integrators.WhittedIntegratorFactory;
import rt.intersectables.Instance;
import rt.intersectables.IntersectableList;
import rt.intersectables.Mesh;
import rt.intersectables.Plane;
import rt.lightsources.PointLight;
import rt.materials.Diffuse;
import rt.materials.XYZCheckerboard;
import rt.materials.XYZGrid;
import rt.samplers.OneSamplerFactory;
import rt.tonemappers.ClampTonemapper;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;
import java.io.IOException;

/**
 * Test scene for instancing and rendering triangle meshes using the teapot model.
 */
public class BSPTeapotLand extends Scene {

	public IntersectableList objects;

	/**
	 * Timing: 8.5 sec on 12 core Xeon 2.5GHz, 24 threads
	 */
	public BSPTeapotLand()
	{	
		outputFilename = new String("BSPTeapotLand");
		
		// Specify integrator to be used
		integratorFactory = new WhittedIntegratorFactory();
		
		// Specify pixel sampler to be used
		samplerFactory = new OneSamplerFactory();

		width = 1024;
		height = 512;

		SPP = 1;

		// Make camera and film
		Vector3f eye = new Vector3f(5.f, 2.5f, 10.f);
		Vector3f lookAt = new Vector3f(0.f, 0.f, 0.f);
		Vector3f up = new Vector3f(0.f, 1.f, 0.f);
		float fov = 30.f;
		float aspect = 2.f;
		camera = new PinholeCamera(eye, lookAt, up, fov, aspect, width, height);
		film = new BoxFilterFilm(width, height);
		tonemapper = new ClampTonemapper();

		
		// List of objects
		objects = new IntersectableList();

		// Ground plane
		Plane plane = new Plane(new Vector3f(0.f, 1.f, 0.f), 1.f);
		plane.material = new XYZCheckerboard();
		objects.add(plane);
		
		/*
		 * Load the teapot model
		 */
		Mesh mesh;
		try
		{
			mesh = ObjReader.read("obj/teapot.obj", 0.3f);
		} catch(IOException e) 
		{
			System.out.printf("Could not read .obj file\n");
			return;
		}

		int nTriangles = mesh.count();
		int maxTeapotDepth = (int) Math.ceil(8 + 1.3 * Math.log(nTriangles));

		BSPAccelerator teapotAccelerator = new BSPAccelerator(mesh, 5, maxTeapotDepth);
		teapotAccelerator.construct();

		// Holds all teapots
		IntersectableList teapots = new IntersectableList();

		int numX = 20;
		int numY = 20;
		float dx = 0.7f;
		float dy = 0.7f;

		float startX = -(numX - 1) * dx / 2f;
		float startY = -(numY - 1) * dy / 2f;

		Matrix4f transf = new Matrix4f();

		/*
		 * Create a ton of instances of the mesh accelerator
		 */
		for(int ix = 0; ix < numX; ix++)
		{
			for(int iy = 0; iy < numY; iy++)
			{
				float x = startX + dx * ix;
				float y = startY + dy * iy;
				Vector3f p = new Vector3f(x, 0, y);
				transf.setIdentity();
				transf.setTranslation(p);

				Instance teapotInstance = new Instance(teapotAccelerator, transf);
				//Instance teapotInstance = new Instance(mesh, transf);
				teapots.add(teapotInstance);
			}
		}

		/*
		 * One final accelerator for all teapot instances
		 */
		int teapotGridDepth = (int) Math.ceil(8 + 1.3 * Math.log(numX * numY));
		BSPAccelerator allTeapots = new BSPAccelerator(teapots, 5, teapotGridDepth);
		allTeapots.construct();

		objects.add(allTeapots);
		root = objects;

		// Light source, relatively far away but strong
		LightGeometry pointLight = new PointLight(new Vector3f(0.f, 20.f, 20.f), new Spectrum(2500.f, 2500.f, 2500.f));
		lightList = new LightList();
		lightList.add(pointLight);
	}
}
