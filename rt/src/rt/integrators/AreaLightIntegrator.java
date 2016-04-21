package rt.integrators;

import rt.*;
import rt.importanceSampling.Heuristic;
import rt.importanceSampling.ImportanceSample;
import rt.importanceSampling.PowerHeuristic;
import rt.importanceSampling.SamplingTechnique;
import rt.samplers.RandomSampler;

import javax.vecmath.Vector3f;

/**
 * Created by Adrian on 10.04.2016.
 */
public class AreaLightIntegrator extends WhittedIntegrator
{

    int numberOfSamples;
    Sampler sampler;
    SamplingTechnique samplingTechnique;
    Heuristic heuristic;

    public AreaLightIntegrator(Scene scene)
    {
        super(scene);
    }

    @Override
    protected void integrateHemisphere(HitRecord surfaceHit, Spectrum outgoing, int depth)
    {
        // If the surface has emission
        outgoing.add(surfaceHit.material.evaluateEmission(surfaceHit, surfaceHit.w));

        // TODO: Try to find a better way than testing with instanceof
        if(surfaceHit.intersectable instanceof LightGeometry)
        {   // The surface point is on a light source
            return;
        }

        Spectrum hemisphere = new Spectrum();

        // Iterate over samples
        float[][] samples = sampler.makeSamples(numberOfSamples, 2);
        for (float[] sample : samples)
        {
            ImportanceSample brdfSample = new ImportanceSample();
            ImportanceSample lightSample = new ImportanceSample();

            if(samplingTechnique == SamplingTechnique.BRDF || samplingTechnique == SamplingTechnique.MIS)
            {
                Material.ShadingSample shadingSample = surfaceHit.material.getShadingSample(surfaceHit, sample);
                brdfSample = sampleBRDF(surfaceHit, shadingSample);
            }
            if(samplingTechnique == SamplingTechnique.Light || samplingTechnique == SamplingTechnique.MIS)
            {
                // Randomly select a light source
                LightGeometry lightSource = getRandomLight();
                HitRecord lightHit = lightSource.sample(sample);
                lightHit.p /= lightList.size();
                lightHit.w = StaticVecmath.sub(surfaceHit.position, lightHit.position);
                lightHit.w.normalize();
                lightSample = sampleLightSource(lightHit, surfaceHit);
            }

            // Apply heuristics for multiple importance sampling
            float weight1 = heuristic.evaluate(brdfSample.directionalProbability, lightSample.directionalProbability);
            float weight2 = heuristic.evaluate(lightSample.areaProbability, brdfSample.areaProbability);

            Spectrum s1 = new Spectrum(brdfSample.spectrum);
            Spectrum s2 = new Spectrum(lightSample.spectrum);
            s1.mult(weight1);
            s2.mult(weight2);

            hemisphere.add(s1);
            hemisphere.add(s2);
        }
        hemisphere.mult(1f / numberOfSamples);
        outgoing.add(hemisphere);
    }

    protected ImportanceSample sampleBRDF(HitRecord surfaceHit, Material.ShadingSample shadingSample)
    {
        if(shadingSample.p == 0)
        {
            return new ImportanceSample();
        }

        Vector3f direction = shadingSample.w;
        Ray sampleRay = new Ray(surfaceHit.position, direction);

        epsilonTranslation(sampleRay, sampleRay.direction);

        HitRecord lightHit = root.intersect(sampleRay);
        if(lightHit == null)
        {   // No object hit in the sampled direction
            return new ImportanceSample();
        }

        if(lightHit.normal.dot(lightHit.w) <= 0)
        {   // Light source is hit from the back
            return new ImportanceSample();
        }

        Vector3f lightDir = StaticVecmath.sub(lightHit.position, surfaceHit.position);
        float d2 = lightDir.lengthSquared();
        lightDir.normalize();

        Spectrum s = lightHit.material.evaluateEmission(lightHit, lightHit.w);
        s.mult(shadingSample.brdf);

        // Multiply with cosine of angle between light direction and surface normal
        float ndotl = surfaceHit.normal.dot(lightDir);
        s.mult(ndotl);

        // Divide by probability density function
        s.mult(1 / shadingSample.p);

        // Return probabilities and spectrum for multiple importance sampling
        ImportanceSample importanceSample = new ImportanceSample();
        importanceSample.areaProbability = shadingSample.p * d2 / lightHit.w.dot(lightHit.normal);
        importanceSample.directionalProbability = shadingSample.p;;
        importanceSample.spectrum = s;

        return importanceSample;
    }

    protected ImportanceSample sampleLightSource(HitRecord lightHit, HitRecord surfaceHit)
    {
        Vector3f lightDir = StaticVecmath.sub(lightHit.position, surfaceHit.position);

        // Check if point on surface lies in shadow of current light source sample
        if (isInShadow(surfaceHit, lightDir))
        {
            // Shadow ray hit another occluding surface
            return new ImportanceSample();
        }

        // Check if the surface points towards the visible side of the area light
        if (lightHit.normal != null && lightHit.normal.dot(lightDir) > 0)
        {
            return new ImportanceSample();
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

        // Divide by probability density function
        s.mult(1 / lightHit.p);

        // Return probabilities and spectrum for multiple importance sampling
        ImportanceSample importanceSample = new ImportanceSample();
        importanceSample.areaProbability = lightHit.p;
        importanceSample.directionalProbability = lightHit.p * lightHit.w.dot(lightHit.normal) / d2;
        importanceSample.spectrum = s;

        return importanceSample;
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
