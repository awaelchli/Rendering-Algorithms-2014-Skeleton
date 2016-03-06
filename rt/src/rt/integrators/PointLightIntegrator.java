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

			// Check if point on surface lies in shadow of current light source
			if (hitRecord.material.castsShadows() && isInShadow(hitRecord.position, lightDir)) {
				// Shadow ray hit another occluding surface
				continue;
			}

			float d2 = lightDir.lengthSquared();
			lightDir.normalize();
			
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

	/**
	 * @param position Position of intersection on the surface
	 * @param lightDir Direction pointing to light source with length that corresponds to the distance of the light source to the object
     * @return true, if shadow ray hits a different object between the surface and light source and returns false otherwise
     */
	private boolean isInShadow(Point3f position, Vector3f lightDir) {

		Point3f origin = new Point3f();
		origin.scaleAdd(EPSILON, lightDir, position);

		Ray shadowRay = new Ray(origin, lightDir);

		HitRecord shadowRayHit = root.intersect(shadowRay);

		if (shadowRayHit == null) {
			// No object hit
			return false;
		}

		if (shadowRayHit.t > 1 + EPSILON) {
			// Hit is behind the light source
			return false;
		}

		return true;
	}

}
