package rt.integrators;

import java.util.Iterator;

import javax.vecmath.*;

import rt.HitRecord;
import rt.Integrator;
import rt.Intersectable;
import rt.LightList;
import rt.LightGeometry;
import rt.Ray;
import rt.Sampler;
import rt.Scene;
import rt.Spectrum;
import rt.StaticVecmath;

/**
 * Integrator for Whitted style ray tracing.
 */
public class PointLightIntegrator implements Integrator {

	public static float EPSILON = 0.00001f;

	LightList lightList;
	Intersectable root;
	
	public PointLightIntegrator(Scene scene)
	{
		this.lightList = scene.getLightList();
		this.root = scene.getIntersectable();
	}

	/**
	 * Basic integrator that simply iterates over the light sources and accumulates
	 * their contributions. No shadow testing, reflection, refraction, or 
	 * area light sources, etc. supported.
	 */
	public Spectrum integrate(Ray r) {

		HitRecord hitRecord = root.intersect(r);
		// immediately return background color if nothing was hit
		if(hitRecord == null) { 
			return new Spectrum(0,0,0);
		}	
		Spectrum outgoing = new Spectrum(0.f, 0.f, 0.f);	
		// Iterate over all light sources
		Iterator<LightGeometry> it = lightList.iterator();
		while(it.hasNext()) {
			LightGeometry lightSource = it.next();
			
			// Make direction from hit point to light source position; this is only supposed to work with point lights
			float dummySample[] = new float[2];
			HitRecord lightHit = lightSource.sample(dummySample);
			Vector3f lightDir = StaticVecmath.sub(lightHit.position, hitRecord.position);
			float d2 = lightDir.lengthSquared();
			lightDir.normalize();

			// Check if point on surface lies in shadow of current light source
			Point3f shadowRayOrigin = new Point3f(hitRecord.position);
			Ray shadowRay = new Ray(shadowRayOrigin, lightDir);
			HitRecord shadowRayHit = root.intersect(shadowRay);
			if (shadowRayHit != null && shadowRayHit.t >= EPSILON) {
				// Shadow ray hit another occluding surface
				continue;
			}
			
			// Evaluate the BRDF
			Spectrum brdfValue = hitRecord.material.evaluateBRDF(hitRecord, hitRecord.w, lightDir);
			
			// Multiply together factors relevant for shading, that is, brdf * emission * ndotl * geometry term
			Spectrum s = new Spectrum(brdfValue);
			
			// Multiply with emission
			s.mult(lightHit.material.evaluateEmission(lightHit, StaticVecmath.negate(lightDir)));
			
			// Multiply with cosine of surface normal and incident direction
			float ndotl = hitRecord.normal.dot(lightDir);
			ndotl = Math.max(ndotl, 0.f);
			s.mult(ndotl);
			
			// Geometry term: multiply with 1/(squared distance), only correct like this 
			// for point lights (not area lights)!
			s.mult(1.f/d2);
			
			// Accumulate
			outgoing.add(s);
		}
		return outgoing;	
	}

	public float[][] makePixelSamples(Sampler sampler, int n) {
		return sampler.makeSamples(n, 2);
	}

}
