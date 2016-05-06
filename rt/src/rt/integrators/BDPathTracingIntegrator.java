package rt.integrators;

import rt.*;
import rt.samplers.RandomSampler;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

/**
 * Created by adrian on 04.05.16.
 */
public class BDPathTracingIntegrator extends AbstractIntegrator
{

    public static final int DEFAULT_MAX_DEPTH = 5;
    public static final int DEFAULT_MIN_DEPTH = 2;
    public static final float DEFAULT_TERMINATION_PROBABILITY = 0.5f;

    int maxEyeDepth, minEyeDepth;
    int minLightDepth, maxLightDepth;
    float eyeTerminationProbability, lightTerminationProbability;

    public BDPathTracingIntegrator(Scene scene)
    {
        super(scene);
        maxEyeDepth = DEFAULT_MAX_DEPTH;
        maxLightDepth = DEFAULT_MAX_DEPTH;
        minEyeDepth = DEFAULT_MIN_DEPTH;
        minLightDepth = DEFAULT_MIN_DEPTH;
        eyeTerminationProbability = DEFAULT_TERMINATION_PROBABILITY;
        lightTerminationProbability = DEFAULT_TERMINATION_PROBABILITY;
    }

    @Override
    public Spectrum integrate(Ray r)
    {
        Path eyePath = createEyePath(r);
        Path lightPath = createLightPath();

        Spectrum outgoing = new Spectrum();
        return outgoing;
    }

    protected Path createEyePath(Ray r)
    {
        HitRecord surfaceHit = root.intersect(r);

        PathVertex start = new PathVertex();

        PathVertex current = start;
        int k = 0;
        while(true)
        {
            if(surfaceHit == null) break;
            if(lightList.contains(surfaceHit.intersectable)) break;
            if(terminateEyePath(k)) break;

            // Make the current vertex
            current.hitRecord = surfaceHit;
            current.shadingSample = surfaceHit.material.getShadingSample(surfaceHit, (new RandomSampler().makeSamples(1, 2)[0]));

            // Prepare the intersection for the next vertex
            Ray nextRay = new Ray(surfaceHit.position, current.shadingSample.w);
            epsilonTranslation(nextRay, nextRay.direction);
            surfaceHit = root.intersect(nextRay);
            current.next = new PathVertex();
            current = current.next;
            k++;
        }

        Path path = new Path();
        path.root = start;
        return path;
    }

    protected Path createLightPath()
    {
        LightGeometry light = getRandomLight();
        RandomSampler sampler = new RandomSampler();
        HitRecord lightHit = light.sample(sampler.makeSamples(1, 2)[0]);
        lightHit.p /= lightList.size();

        PathVertex start = new PathVertex();
        start.hitRecord = lightHit;
        start.shadingSample = lightHit.material.getEmissionSample(lightHit, sampler.makeSamples(1, 2)[0]);

        PathVertex current = start;
        int k = 0;
        while(true)
        {
            Point3f currentPos = new Point3f(current.hitRecord.position);
            Vector3f currentDir = new Vector3f(current.shadingSample.w);
            Ray nextRay = new Ray(currentPos, currentDir);
            epsilonTranslation(nextRay, currentDir);
            HitRecord hit = root.intersect(nextRay);

            if(hit == null) break;
            if(lightList.contains(hit.intersectable)) break;
            if(terminateLightPath(k)) break;

            current.next = new PathVertex();
            current = current.next;
            current.hitRecord = hit;
            current.shadingSample = hit.material.getShadingSample(hit, sampler.makeSamples(1, 2)[0]);
            k++;
        }

        Path path = new Path();
        path.root = start;
        return path;
    }

    protected boolean terminateLightPath(int depth)
    {
        return terminatePath(depth, minLightDepth, maxLightDepth, lightTerminationProbability);
    }

    protected boolean terminateEyePath(int depth)
    {
        return terminatePath(depth, minEyeDepth, maxEyeDepth, eyeTerminationProbability);
    }

    private boolean terminatePath(int depth, int minDepth, int maxDepth, float terminationProbability)
    {
        if(depth >= maxDepth) return true;
        if(depth <= minDepth) return false;

        RandomSampler sampler = new RandomSampler();
        float p =  sampler.makeSamples(1, 1)[0][0];

        // Terminate with given probability above a certain path length
        return p < terminationProbability;
    }
}
