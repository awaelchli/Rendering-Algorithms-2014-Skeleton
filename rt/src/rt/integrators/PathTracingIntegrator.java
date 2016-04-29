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

    int maxDepth;
    int minDepth;
    float terminationProbability;

    public PathTracingIntegrator(Scene scene)
    {
        super(scene);
        maxDepth = DEFAULT_MAX_DEPTH;
        minDepth = DEFAULT_MIN_DEPTH;
        terminationProbability = DEFAULT_TERMINATION_PROBABILITY;
    }

    @Override
    public Spectrum integrate(Ray r)
    {
        Spectrum color = new Spectrum();
        Spectrum alpha = new Spectrum(1, 1, 1);
        int k = 0;

        HitRecord surfaceHit = root.intersect(r);

        while(true)
        {
            if(surfaceHit == null) break;

            if(lightList.contains(surfaceHit.intersectable))
            {   // The ray 'accidentally' hit the light source, do not further trace the ray
                if(k == 0) // Eye ray directly hit the light source
                    color.add(surfaceHit.material.evaluateEmission(surfaceHit, surfaceHit.w));
                break;
            }

            Material.ShadingSample shadingSample = surfaceHit.material.getShadingSample(surfaceHit, (new RandomSampler().makeSamples(1, 2)[0]));

            if(!shadingSample.isSpecular)
            {   // Do not add light source contribution on specular surfaces (mirrors, refractive materials)
                HitRecord lightHit = sampleLight(surfaceHit);
                Spectrum lightSourceContribution = new Spectrum(alpha);
                lightSourceContribution.mult(shade(surfaceHit, lightHit));
                lightSourceContribution.mult(1 / lightHit.p);
                color.add(lightSourceContribution);
            }

            if(terminatePath(k)) break;

            alpha.mult(shadingSample.brdf);
            alpha.mult(Math.max(0, surfaceHit.normal.dot(shadingSample.w)));
            alpha.mult(1 / (shadingSample.p * (1 - terminationProbability)));

            // Go to next path segment
            Ray nextRay = new Ray(surfaceHit.position, shadingSample.w);
            epsilonTranslation(nextRay, nextRay.direction);
            surfaceHit = root.intersect(nextRay);
            k++;
        }

        return color;
    }

    protected HitRecord sampleLight(HitRecord surfaceHit)
    {
        LightGeometry light = getRandomLight();
        RandomSampler sampler = new RandomSampler();
        HitRecord lightHit = light.sample(sampler.makeSamples(1, 2)[0]);
        lightHit.p /= lightList.size();

        // Calculate direction of outgoing radiance relative to light source
        lightHit.w = StaticVecmath.sub(surfaceHit.position, lightHit.position);
        float d2 = lightHit.w.lengthSquared();
        lightHit.w.normalize();

        // Conversion to pdf over direction
        lightHit.p *= d2 / lightHit.w.dot(lightHit.normal);

        return lightHit;
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

    private boolean terminatePath(int depth)
    {
        if(depth >= maxDepth) return true;
        if(depth <= minDepth) return false;

        RandomSampler sampler = new RandomSampler();
        float p =  sampler.makeSamples(1, 1)[0][0];

        // Terminate with given probability above a certain path length
        return p < terminationProbability;
    }
}
