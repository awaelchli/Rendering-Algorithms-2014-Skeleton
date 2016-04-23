package rt.integrators;

import rt.*;
import rt.samplers.RandomSampler;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

/**
 * Created by Adrian on 23.04.2016.
 */
public abstract class AbstractIntegrator implements Integrator
{
    LightList lightList;
    Intersectable root;

    float epsilon;

    public AbstractIntegrator(Scene scene)
    {
        this.lightList = scene.getLightList();
        this.root = scene.getIntersectable();
        this.epsilon = scene.getEpsilon();
    }

    @Override
    public float[][] makePixelSamples(Sampler sampler, int n)
    {
        return sampler.makeSamples(n, 2);
    }

    /**
     * @param hit {@link HitRecord} of intersection on the surface
     * @param lightDir Direction pointing to light source with length that corresponds to the distance of the light source to the object
     * @return true, if shadow ray hits a different object between the surface and light source and returns false otherwise
     */
    protected boolean isInShadow(HitRecord hit, Vector3f lightDir)
    {
        Point3f origin = new Point3f();
        origin.scaleAdd(this.epsilon, hit.normal, hit.position);

        Ray shadowRay = new Ray(origin, lightDir);

        HitRecord shadowRayHit = root.intersect(shadowRay);

        if (shadowRayHit == null) {
            // No object hit
            return false;
        }

        if (shadowRayHit.t > 1 - epsilon) {
            // Hit is behind the light source
            return false;
        }

        if (!shadowRayHit.material.castsShadows()) {
            return false;
        }

        return true;
    }

    protected void epsilonTranslation(Ray ray, Vector3f direction)
    {
        Vector3f d = new Vector3f(direction);
        d.scale(epsilon);
        ray.translate(d);
    }

    protected LightGeometry getRandomLight()
    {
        float[][] rnd = (new RandomSampler()).makeSamples(1, 1);
        int index = (int) Math.floor(rnd[0][0] * lightList.size());
        return lightList.get(index);
    }
}
