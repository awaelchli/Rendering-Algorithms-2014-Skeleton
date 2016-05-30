package rt.testscenes;

import rt.*;
import rt.bsp.BSPAccelerator;
import rt.cameras.PinholeCamera;
import rt.films.BoxFilterFilm;
import rt.importanceSampling.SamplingTechnique;
import rt.integrators.AreaLightIntegratorFactory;
import rt.integrators.PathTracingIntegratorFactory;
import rt.intersectables.*;
import rt.lightsources.RectangleLight;
import rt.materials.Diffuse;
import rt.materials.Refractive;
import rt.media.Homogeneous;
import rt.samplers.RandomSamplerFactory;
import rt.tonemappers.ClampTonemapper;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import java.io.IOException;

public class ParticipatingMedia extends CornellBox {

	private Sphere sphere;

	public ParticipatingMedia()
	{
		outputFilename = new String("output/testscenes/assignment6/ParticipatingMedia");
				
		// Specify pixel sampler to be used
		samplerFactory = new RandomSamplerFactory();
		
		// Samples per pixel
		SPP = 128;
		outputFilename = outputFilename + " " + String.format("%d", SPP) + "SPP";

		// Make camera and film
		Vector3f eye = new Vector3f(278, 273, -800);
		Vector3f lookAt = new Vector3f(278, 273, 0);
		Vector3f up = new Vector3f(0, 1, 0);
		float fov = 40;
		int width = 128;
		int height = 128;
		float aspect = (float) width / (float) height;
		camera = new PinholeCamera(eye, lookAt, up, fov, aspect, width, height);
		film = new BoxFilterFilm(width, height);
		tonemapper = new ClampTonemapper();
		
		// Specify integrator to be used
		PathTracingIntegratorFactory iF = new PathTracingIntegratorFactory();
        iF.setMinDepth(4);
		iF.setMaxDepth(50);
		iF.setTerminationProbability(0.5f);
		integratorFactory = iF;

		build();
// 0.009126 0.0010748 0.0012500
// 0.009124 0.0010744 0.0012492
		Spectrum sigmaMilk = new Spectrum(0.000002f, 0.0000004f, 0.0000008f);
		sigmaMilk.mult(1);
		Spectrum test = new Spectrum(0.000008f, 0.0000008f, 0.0000008f);
		Spectrum sigmaWater = new Spectrum(0.001886f, 0.0018308f, 0.0020025f);

		Spectrum g = new Spectrum(0.932f, 0.902f, 0.859f);
		sphere.material = new Homogeneous(sigmaMilk, new Spectrum(0, 0, 0), 100);
	}

	protected void build()
	{
		// List of objects
		IntersectableList objects = new IntersectableList();

		// Materials
		Diffuse white = new Diffuse(new Spectrum(0.73f, 0.739f, 0.729f));
		Diffuse green = new Diffuse(new Spectrum(0.117f, 0.435f, 0.115f));
		Diffuse red = new Diffuse(new Spectrum(0.61f, 0.056f, 0.062f));

		// Floor
		float[] floor_vertices = {552.8f, 0, 0, 0, 0, 0, 0, 0, 559.2f, 549.6f, 0, 559.2f};
		float[] floor_normals = {0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0};
		int[] floor_indices = {0, 1, 2, 0, 2, 3};
		Mesh floor = new Mesh(floor_vertices, floor_normals, null, floor_indices);
		floor.material = white;
		objects.add(floor);

		// Ceiling
		float[] ceiling_vertices = {556.0f, 548.8f, 0, 556.0f, 548.8f, 559.2f, 0, 548.8f, 559.2f, 0, 548.8f, 0};
		float[] ceiling_normals = {0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0};
		int[] ceiling_indices = {0, 1, 2, 0, 2, 3};
		Mesh ceiling = new Mesh(ceiling_vertices, ceiling_normals, null, ceiling_indices);
		ceiling.material = white;
		objects.add(ceiling);

		// Back wall
		float[] back_wall_vertices = {549.6f, 0, 559.2f, 0, 0, 559.2f, 0, 548.8f, 559.2f, 556.0f, 548.8f, 559.2f};
		float[] back_wall_normals = {0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1};
		int[] back_wall_indices = {0, 1, 2, 0, 2, 3};
		Mesh back_wall = new Mesh(back_wall_vertices, back_wall_normals, null, back_wall_indices);
		back_wall.material = white;
		objects.add(back_wall);

		// Right wall
		float[] right_wall_vertices = {0, 0, 559.2f, 0, 0, 0, 0, 548.8f, 0, 0, 548.8f, 559.2f};
		float[] right_wall_normals = {1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0};
		int[] right_wall_indices = {0, 1, 2, 0, 2, 3};
		Mesh right_wall = new Mesh(right_wall_vertices, right_wall_normals, null, right_wall_indices);
		right_wall.material = green;
		objects.add(right_wall);

		// Left wall
		float[] left_wall_vertices = {552.8f, 0, 0, 549.6f, 0, 559.2f, 556.0f, 548.8f, 559.2f, 556.0f, 548.8f, 0};
		float[] left_wall_normals = {-1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0};
		int[] left_wall_indices = {0, 2, 3, 0, 1, 2};
		Mesh left_wall = new Mesh(left_wall_vertices, left_wall_normals, null, left_wall_indices);
		left_wall.material = red;
		objects.add(left_wall);

		// Short block
		sphere = new Sphere(new Point3f(130, 100, 65), 100);
		objects.add(sphere);
//		float[] short_block_vertices = {130, 165, 65, 82, 165, 225, 240, 165, 272, 290, 165, 114, // top
//				290, 0, 114, 290, 165, 114, 240, 165, 272, 240, 0, 272, // left
//				130, 0, 65, 130, 165, 65, 290, 165, 114, 290, 0, 114, // front
//				82, 0, 225, 82, 165, 225, 130, 165, 65, 130, 0, 65, // right
//				240, 0, 272, 240, 165, 272, 82, 165, 225, 82, 0, 225}; // back
//		float[] short_block_normals = null; // Will create normals based on indices order
//		int[] short_block_indices =    {0, 1, 2, 0, 2, 3, // top
//				4, 6, 5, 4, 7, 6, // left
//				8, 9, 10, 8, 10, 11, // front
//				12, 13, 14, 12, 14, 15, // right
//				16, 18, 17, 16, 19, 18}; // back
//		short_block = new Mesh(short_block_vertices, short_block_normals, null, short_block_indices);
//		short_block.material = white;
//		objects.add(short_block);

		// Tall block
		float[] tall_block_vertices =  {423, 330, 247, 265, 330, 296, 314, 330, 456, 472, 330, 406,
				423, 0, 247, 423, 330, 247, 472, 330, 406, 472, 0, 406,
				472, 0, 406, 472, 330, 406, 314, 330, 456, 314, 0, 456,
				314, 0, 456, 314, 330, 456, 265, 330, 296, 265, 0, 296,
				265, 0, 296, 265, 330, 296, 423, 330, 247, 423, 0, 247};
		float[] tall_block_normals = null; // Will create normals based on indices order
		int[] tall_block_indices =     {0, 2, 1, 0, 3, 2, // top
				4, 6, 5, 4, 7, 6, // left
				8, 9, 10, 8, 10, 11, // back
				12, 14, 13, 12, 15, 14, // right
				16, 17, 18, 16, 18, 19}; // front
		tall_block = new Mesh(tall_block_vertices, tall_block_normals, null, tall_block_indices);
		tall_block.material = white;
		objects.add(tall_block);

		// Light source
		Point3f bottomLeft = new Point3f(343.0f, 548.7f, 227.0f);
		Vector3f right = new Vector3f(0, 0, 332 - 227);
		Vector3f top = new Vector3f(213 - 343, 0, 0);
		Spectrum emission = new Spectrum(100, 100, 100);
		emission.mult(30000);
		RectangleLight light = new RectangleLight(bottomLeft, right, top, emission);

		lightList = new LightList();
		lightList.add(light);
		objects.add(light);

		root = objects;
	}
	
}
