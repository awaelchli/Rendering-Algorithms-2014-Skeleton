package rt.integrators;

import rt.*;
import rt.samplers.RandomSampler;

import javax.vecmath.Vector3f;

/**
 * Created by Adrian on 10.04.2016.
 */
public class AreaLightIntegrator extends WhittedIntegrator
{
    int numberOfSamples;
    Sampler sampler;

    public AreaLightIntegrator(Scene scene, int recursionDepth)
    {
        super(scene, recursionDepth);
        this.numberOfSamples = 1;
        this.sampler = new RandomSampler();
    }

    @Override
    protected void integrateHemisphere(HitRecord surfaceHit, Spectrum outgoing, int depth)
    {
        float[][] samples = sampler.makeSamples(numberOfSamples, 2);

        // If the surface has emission
        outgoing.add(surfaceHit.material.evaluateEmission(surfaceHit, surfaceHit.w));

        for (float[] sample : samples)
        {
            Material.ShadingSample shadingSample = surfaceHit.material.getShadingSample(surfaceHit, sample);

            LightGeometry lightSource = getRandomLight();
            HitRecord lightHit = lightSource.sample(sample);

            Spectrum s1 = sampleBRDF(surfaceHit, shadingSample, depth);
            Spectrum s2 = sampleLightSource(lightHit, surfaceHit);

            float p1 = shadingSample.p;
            float p2 = lightHit.p;

            // Apply heuristics for multiple importance sampling
            float weight1 = p1 / (p1 + p2);
            float weight2 = p2 / (p1 + p2);

            s1.mult(weight1);
            s2.mult(weight2);

            outgoing.add(s1);
            outgoing.add(s2);
        }
        outgoing.mult(1f / numberOfSamples);
    }

    protected Spectrum sampleBRDF(HitRecord surfaceHit, Material.ShadingSample shadingSample, int depth)
    {
        if(shadingSample.p == 0)
        {
            return new Spectrum(0, 0, 0);
        }

        Vector3f direction = shadingSample.w;
        Ray sampleRay = new Ray(surfaceHit.position, direction);

        Spectrum s = integrate(sampleRay, depth + 1);
        s.mult(1 / shadingSample.p);

        return s;
    }

    protected Spectrum sampleLightSource(HitRecord lightHit, HitRecord surfaceHit)
    {
        // Randomly select a light source
        Vector3f lightDir = StaticVecmath.sub(lightHit.position, surfaceHit.position);

        // Check if point on surface lies in shadow of current light source sample
        if (isInShadow(surfaceHit, lightDir))
        {
            // Shadow ray hit another occluding surface
            return new Spectrum(0, 0, 0);
        }

        // Check if the surface points towards the visible side of the area light
        if (lightHit.normal != null && lightHit.normal.dot(lightDir) > 0)
        {
            return new Spectrum(0, 0, 0);
        }

        float d2 = lightDir.lengthSquared();
        lightDir.normalize();

        /*
         *  Multiply together factors relevant for shading, that is, brdf * emission * geometry term / pdf
         */
        Spectrum s = new Spectrum(surfaceHit.material.evaluateBRDF(surfaceHit, surfaceHit.w, lightDir));

        // Multiply with emission
        s.mult(lightHit.material.evaluateEmission(lightHit, StaticVecmath.negate(lightDir)));

        // Multiply with geometry term
        s.mult(geometryTerm(surfaceHit, lightHit, lightDir, d2));

        // Express pdf in solid angle
        lightHit.p *= lightList.size();
        lightHit.p *= d2 / lightHit.normal.dot(StaticVecmath.negate(lightDir));

        // Divide by probability density function
        s.mult(1 / lightHit.p);;

        return s;
    }

    protected Spectrum geometryTerm(HitRecord surfaceHit, HitRecord lightHit, Vector3f lightDir, float distanceSquared)
    {
        Spectrum geometryTerm = new Spectrum(1, 1, 1);

        // Multiply with cosine of surface normal and incident direction
        float ndotl = surfaceHit.normal.dot(lightDir);
        ndotl = Math.max(ndotl, 0);
        geometryTerm.mult(ndotl);

        // Multiply with cosine of light normal and incident direction on light source
        if(lightHit.normal != null) // Normal can be null for point lights
        {
            float ndotl2 = -lightHit.normal.dot(lightDir);
            ndotl2 = Math.max(ndotl2, 0);
            geometryTerm.mult(ndotl2);
        }

        // Divide by squared distance to sample point
        geometryTerm.mult(1 / distanceSquared);

        return geometryTerm;
    }

    private LightGeometry getRandomLight()
    {
        float[][] rnd = (new RandomSampler()).makeSamples(1, 1);
        int index = (int) Math.floor(rnd[0][0] * lightList.size());
        return lightList.get(index);
    }
}
