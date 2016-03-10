package rt.integrators;

import java.util.Iterator;

import javax.vecmath.*;

import rt.*;

/**
 * Integrator for Whitted style ray tracing.
 */
public class WhittedIntegrator implements Integrator {

    LightList lightList;
    Intersectable root;

    float epsilon;
    int recursionDepth;

    public WhittedIntegrator(Scene scene, int recursionDepth)
    {
        this.lightList = scene.getLightList();
        this.root = scene.getIntersectable();
        this.epsilon = scene.getEpsilon();
        this.recursionDepth = recursionDepth;
    }

    public Spectrum integrate(Ray r) {
        return integrate(r, 0);
    }

    private Spectrum integrate(Ray r, int depth) {

        if (depth == this.recursionDepth){
            return new Spectrum(0, 0, 0);
        }

        HitRecord hitRecord = root.intersect(r);
        // immediately return background color if nothing was hit
        if (hitRecord == null) {
            return new Spectrum(0, 0, 0);
        }

        Spectrum outgoing = new Spectrum(0, 0, 0);
        Material material = hitRecord.material;

        // Iterate over all light sources
        Iterator<LightGeometry> it = lightList.iterator();
        while(it.hasNext()) {
            LightGeometry lightSource = it.next();
            Spectrum spectrum = integrateLightSource(lightSource, hitRecord);
            // Accumulate
            outgoing.add(spectrum);
        }

        if (material.hasSpecularReflection()) {
            Ray reflectedRay = Ray.reflect(hitRecord);
            epsilonTranslation(reflectedRay, hitRecord.normal);
            Spectrum reflection = material.evaluateSpecularReflection(hitRecord).brdf;
            reflection.mult(integrate(reflectedRay, depth + 1));
            outgoing.add(reflection);
        }

        if (material.hasSpecularRefraction()) {

        }

        return outgoing;
    }

    private Spectrum integrateLightSource(LightGeometry lightSource, HitRecord hitRecord) {

        // Make direction from hit point to light source position; this is only supposed to work with point lights
        float dummySample[] = new float[2];
        HitRecord lightHit = lightSource.sample(dummySample);
        Vector3f lightDir = StaticVecmath.sub(lightHit.position, hitRecord.position);

        // Check if point on surface lies in shadow of current light source
        if (hitRecord.material.castsShadows() && isInShadow(hitRecord, lightDir)) {
            // Shadow ray hit another occluding surface
            return new Spectrum(0, 0, 0);
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

        return s;
    }

    private void epsilonTranslation(Ray ray, Vector3f direction) {
        Vector3f d = new Vector3f(direction);
        d.scale(epsilon);
        ray.translate(d);
    }

    public float[][] makePixelSamples(Sampler sampler, int n) {
        return sampler.makeSamples(n, 2);
    }

    /**
     * @param hit {@link HitRecord} of intersection on the surface
     * @param lightDir Direction pointing to light source with length that corresponds to the distance of the light source to the object
     * @return true, if shadow ray hits a different object between the surface and light source and returns false otherwise
     */
    private boolean isInShadow(HitRecord hit, Vector3f lightDir) {

        Point3f origin = new Point3f();
        origin.scaleAdd(this.epsilon, hit.normal, hit.position);

        Ray shadowRay = new Ray(origin, lightDir);

        HitRecord shadowRayHit = root.intersect(shadowRay);

        if (shadowRayHit == null) {
            // No object hit
            return false;
        }

        if (shadowRayHit.t > 1) {
            // Hit is behind the light source
            return false;
        }

        return true;
    }

}

