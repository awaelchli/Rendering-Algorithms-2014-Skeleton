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

		Spectrum sigmaMilk = new Spectrum(0.0015333f, 0.0046f, 0.019933f);
		Spectrum sigmaWater = new Spectrum(0.001886f, 0.0018308f, 0.0020025f);
		short_block.material = new Homogeneous(sigmaMilk, new Spectrum(0, 0, 0), 100);
	}
	
}
