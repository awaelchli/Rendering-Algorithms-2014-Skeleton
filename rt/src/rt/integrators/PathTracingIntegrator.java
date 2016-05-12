package rt.integrators;

import rt.*;
import rt.samplers.RandomSampler;

import javax.vecmath.Vector3f;

/**
 * Created by Adrian on 23.04.2016.
 */
public class PathTracingIntegrator extends AbstractIntegrator
{
    public static final int DEFAULT_MAX_DEPTH = 5;
    public static final int DEFAULT_MIN_DEPTH = 2;
    public static final float DEFAULT_TERMINATION_PROBABILITY = 0.5f;
    public static final float DEFAULT_SHADOWRAY_CONTRIBUTION_THRESHOLD = 0.01f;

    int maxDepth;
    int minDepth;
    float terminationProbability;
    float shadowRayContributionThreshold;

    public PathTracingIntegrator(Scene scene)
    {
        super(scene);
        maxDepth = DEFAULT_MAX_DEPTH;
        minDepth = DEFAULT_MIN_DEPTH;
        terminationProbability = DEFAULT_TERMINATION_PROBABILITY;
        shadowRayContributionThreshold = DEFAULT_SHADOWRAY_CONTRIBUTION_THRESHOLD;
    }

    @Override
    public Spectrum integrate(Ray r)
    {
        Spectrum color = new Spectrum();
        Spectrum alpha = new Spectrum(1, 1, 1);
        int k = 0;
        boolean previousMaterialWasSpecular = false;

        HitRecord surfaceHit = root.intersect(r);

        while(true)
        {
            if(surfaceHit == null) break;

            if(lightList.contains(surfaceHit.intersectable))
            {   // The ray 'accidentally' hit the light source, do not further trace the ray
                if(k == 0 || previousMaterialWasSpecular) // Eye ray directly hit the light source
                    color.add(surfaceHit.material.evaluateEmission(surfaceHit, surfaceHit.w));
                break;
            }

            Material.ShadingSample shadingSample = surfaceHit.material.getShadingSample(surfaceHit, (new RandomSampler().makeSamples(1, 2)[0]));

            if(!shadingSample.isSpecular)
            {   // Do not add light source contribution on specular surfaces (mirrors, refractive materials)
                Spectrum lightSourceContribution = lightSourceContribution(surfaceHit);
                lightSourceContribution.mult(alpha);
                color.add(lightSourceContribution);
            }

            if(terminatePath(k)) break;

            float q = getTerminationProbability(k);
            alpha.mult(shadingSample.brdf);
            alpha.mult(Math.abs(surfaceHit.normal.dot(shadingSample.w)));
            alpha.mult(1 / (shadingSample.p * (1 - q)));

            // Go to next path segment
            Ray nextRay = new Ray(surfaceHit.position, shadingSample.w);
            epsilonTranslation(nextRay, nextRay.direction);
            surfaceHit = root.intersect(nextRay);
            k++;
            previousMaterialWasSpecular = shadingSample.isSpecular;
        }

        return color;
    }

    protected Spectrum lightSourceContribution(HitRecord surfaceHit)
    {
        LightGeometry light = getRandomLight();
        RandomSampler sampler = new RandomSampler();
        HitRecord lightHit = light.sample(sampler.makeSamples(1, 2)[0]);
        lightHit.p /= lightList.size();

        // Point lights have undefined normal
        boolean isPointLight = lightHit.normal == null;

        // Calculate direction of outgoing radiance relative to light source
        lightHit.w = StaticVecmath.sub(surfaceHit.position, lightHit.position);

        float d2 = lightHit.w.lengthSquared();
        lightHit.w.normalize();

        // Conversion to pdf over direction
        float cos = 1;
        if(!isPointLight) cos = Math.max(0, lightHit.w.dot(lightHit.normal));
        float conversionFactor = cos / d2;

        // Russian Roulette on shadow ray
        Spectrum test = lightHit.material.evaluateEmission(lightHit, lightHit.w);
        test.mult(conversionFactor);
        float length = (float) Math.sqrt(test.r * test.r + test.g * test.g + test.b * test.b);
        // Do not trace the shadow ray if contribution is too low
        if(length < shadowRayContributionThreshold)
            return new Spectrum(0, 0, 0);

        Spectrum contribution = new Spectrum(1, 1, 1);
        contribution.mult(shade(surfaceHit, lightHit));
        if(!isPointLight) contribution.mult(1 / lightHit.p);
        contribution.mult(conversionFactor);

        return contribution;
    }

    protected Spectrum shade(HitRecord surfaceHit, HitRecord lightHit)
    {
        Vector3f lightDir = StaticVecmath.sub(lightHit.position, surfaceHit.position);

        if(isInShadow(surfaceHit, lightDir))
        {   // Point on surface is in shadow of light source
            return new Spectrum();
        }

        Spectrum s = lightHit.material.evaluateEmission(lightHit, lightHit.w);
        Vector3f wIn = StaticVecmath.negate(lightHit.w);
        s.mult(surfaceHit.material.evaluateBRDF(surfaceHit, surfaceHit.w, wIn));
        s.mult(Math.max(0, surfaceHit.normal.dot(wIn)));
        return s;
    }

    protected boolean terminatePath(int depth)
    {
        if(depth >= maxDepth) return true;
        if(depth <= minDepth) return false;

        RandomSampler sampler = new RandomSampler();
        float p =  sampler.makeSamples(1, 1)[0][0];

        // Terminate with given probability above a certain path length
        return p < terminationProbability;
    }

    protected float getTerminationProbability(int k)
    {
        float q = terminationProbability;
        if(k <= minDepth) q = 0;
        if(k >= maxDepth) q = 1;
        return q;
    }
}
