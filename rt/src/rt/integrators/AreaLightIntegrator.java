package rt.integrators;

import rt.*;
import rt.importanceSampling.Heuristic;
import rt.importanceSampling.ImportanceSample;
import rt.importanceSampling.PowerHeuristic;
import rt.importanceSampling.SamplingTechnique;
import rt.samplers.RandomSampler;

import javax.sound.sampled.FloatControl;
import javax.vecmath.Vector3f;

/**
 * Created by Adrian on 10.04.2016.
 */
public class AreaLightIntegrator extends WhittedIntegrator
{

    Sampler sampler;
    SamplingTechnique samplingTechnique;
    Heuristic heuristic;

    public AreaLightIntegrator(Scene scene)
    {
        super(scene);
        this.sampler = scene.getSamplerFactory().make();
    }

    @Override
    protected void integrateHemisphere(HitRecord surfaceHit, Spectrum outgoing, int depth)
    {
        // If the surface has emission
        outgoing.add(surfaceHit.material.evaluateEmission(surfaceHit, surfaceHit.w));

        if(lightList.contains(surfaceHit.intersectable))
        {   // The surface point is on a light source
            return;
        }

        ImportanceSample brdfSample = new ImportanceSample();
        ImportanceSample lightSample = new ImportanceSample();

        if(samplingTechnique == SamplingTechnique.BRDF)
        {
            brdfSample = sampleBRDF(surfaceHit);
            outgoing.add(brdfSample.spectrum);
        }
        if(samplingTechnique == SamplingTechnique.Light)
        {
            lightSample = sampleLightSource(surfaceHit);
            outgoing.add(lightSample.spectrum);
        }
        if(samplingTechnique == SamplingTechnique.MIS)
        {
            brdfSample = sampleBRDF(surfaceHit);
            lightSample = sampleLightSource(surfaceHit);

            if(lightSample.areaProbability == 0 && brdfSample.directionalProbability == 0)
            {
                return;
            }

            // Apply heuristics for multiple importance sampling
            float weight1 = heuristic.evaluate(brdfSample.directionalProbability, lightSample.directionalProbability);
            float weight2 = heuristic.evaluate(lightSample.directionalProbability, brdfSample.directionalProbability);

            Spectrum s1 = new Spectrum(brdfSample.spectrum);
            Spectrum s2 = new Spectrum(lightSample.spectrum);
            s1.mult(weight1);
            s2.mult(weight2);

            outgoing.add(s1);
            outgoing.add(s2);
        }
    }

    protected ImportanceSample sampleBRDF(HitRecord surfaceHit)
    {
        Material.ShadingSample shadingSample = surfaceHit.material.getShadingSample(surfaceHit, sampler.makeSamples(1, 2)[0]);

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
        importanceSample.areaProbability = shadingSample.p * lightHit.w.dot(lightHit.normal) / d2;
        importanceSample.directionalProbability = shadingSample.p;;
        importanceSample.spectrum = s;

        return importanceSample;
    }

    protected ImportanceSample sampleLightSource(HitRecord surfaceHit)
    {
        // Randomly select a light source
        LightGeometry lightSource = getRandomLight();
        HitRecord lightHit = lightSource.sample(sampler.makeSamples(1, 2)[0]);
        lightHit.p /= lightList.size();

        Vector3f lightDir = StaticVecmath.sub(lightHit.position, surfaceHit.position);

        // Check if point on surface lies in shadow of current light source sample
        if (isInShadow(surfaceHit, lightDir))
        {
            // Shadow ray hit another occluding surface
            return new ImportanceSample();
        }

        // Check if the surface points towards the visible side of the area light
        if (lightHit.normal != null && lightHit.normal.dot(lightDir) >= 0)
        {
            return new ImportanceSample();
        }

        float d2 = lightDir.lengthSquared();
        lightDir.normalize();
        lightHit.w = StaticVecmath.negate(lightDir);

        /*
         *  Multiply together factors relevant for shading, that is, brdf * emission * geometry term / pdf
         */
        Spectrum s = new Spectrum(surfaceHit.material.evaluateBRDF(surfaceHit, surfaceHit.w, lightDir));

        // Multiply with emission
        s.mult(lightHit.material.evaluateEmission(lightHit, lightHit.w));

        // Multiply with geometry term
        float ndotl = Math.max(0, surfaceHit.normal.dot(lightDir));
        s.mult(ndotl / d2);

        // Divide by probability density function
        s.mult(1 / lightHit.p);

        // Return probabilities and spectrum for multiple importance sampling
        ImportanceSample importanceSample = new ImportanceSample();
        importanceSample.areaProbability = lightHit.p;
        importanceSample.directionalProbability = lightHit.p * d2 / lightHit.w.dot(lightHit.normal);
        importanceSample.spectrum = s;

        return importanceSample;
    }

    private LightGeometry getRandomLight()
    {
        float[][] rnd = (new RandomSampler()).makeSamples(1, 1);
        int index = (int) Math.floor(rnd[0][0] * lightList.size());
        return lightList.get(index);
    }
}
