package rt.integrators;

import javafx.scene.effect.Light;
import rt.*;
import rt.samplers.RandomSampler;

import javax.vecmath.Vector3f;

/**
 * Created by Adrian on 23.04.2016.
 */
public class PathTracingIntegrator extends AbstractIntegrator
{
    public static final int DEFAULT_MAX_DEPTH = 3;

    int maxDepth;

    public PathTracingIntegrator(Scene scene)
    {
        super(scene);
        maxDepth = DEFAULT_MAX_DEPTH;
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

            HitRecord lightHit = sampleLight(surfaceHit);

            Spectrum lightSourceContribution = new Spectrum(alpha);
            lightSourceContribution.mult(shade(surfaceHit, lightHit));
            lightSourceContribution.mult(1 / lightHit.p);
            color.add(lightSourceContribution);

            if(terminatePath(k)) break;

            Material.ShadingSample shadingSample = surfaceHit.material.getShadingSample(surfaceHit, (new RandomSampler().makeSamples(1, 2)[0]));
            alpha.mult(shadingSample.brdf);
            alpha.mult(surfaceHit.normal.dot(shadingSample.w));
            alpha.mult(1 / (shadingSample.p * (1 - 0.5f)));

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
        // TODO: Randomly select a light source among VISIBLE light sources
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
        // TODO: Check for shadows
        Spectrum s = lightHit.material.evaluateEmission(lightHit, lightHit.w);
        Vector3f wIn = StaticVecmath.negate(lightHit.w);
        s.mult(surfaceHit.material.evaluateBRDF(surfaceHit, surfaceHit.w, wIn));
        s.mult(surfaceHit.normal.dot(wIn));
        return s;
    }

    private boolean terminatePath(int depth)
    {
        if(depth >= maxDepth) return true;

        RandomSampler sampler = new RandomSampler();
        float p =  sampler.makeSamples(1, 1)[0][0];

        // Terminate with probability 0.5
        // TODO: Generalize
        return p < 0.5f;
    }


}
