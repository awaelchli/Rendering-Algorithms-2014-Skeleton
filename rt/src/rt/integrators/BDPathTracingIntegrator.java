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
        Spectrum alpha = new Spectrum(1, 1, 1);
        boolean previousMaterialWasSpecular = false;

        // Traverse eye path and connect to the vertices of the light path
        for(PathVertex eyeVertex : eyePath)
        {
            // Skip the camera vertex
            if(eyeVertex.isRoot())
                continue;

            if(lightList.contains(eyeVertex.hitRecord.intersectable))
            {   // Do not trace eye path further if a light source is hit
                if(eyeVertex.index == 1 || previousMaterialWasSpecular)
                {   // Exception 1: Add the material emission if it is the first bounce (direct light hit).
                    // Exception 2: Add the emission if the path travels through a refractive material or mirror.
                    Spectrum emission = eyeVertex.hitRecord.material.evaluateEmission(eyeVertex.hitRecord, eyeVertex.hitRecord.w);
                    outgoing.add(emission);
                }
                break;
            }

            // Connect the current eye vertex with every vertex in the ligth path
            Spectrum beta = new Spectrum(1, 1, 1);
            for(PathVertex lightVertex : lightPath)
            {
                Spectrum eyeToLightConnection = evaluateEyeToLightConnection(eyeVertex, lightVertex);
                eyeToLightConnection.mult(alpha);
                eyeToLightConnection.mult(beta);

                // Update beta
                if(lightVertex.isRoot())
                {   // The first light vertex has emission
                    beta.mult(lightVertex.shadingSample.emission);
                } else {
                    beta.mult(lightVertex.shadingSample.brdf);
                }
                beta.mult(1 / lightVertex.hitRecord.p);

                // Divide by probability of choosing this connection
                int s = eyeVertex.index;
                int t = lightVertex.index;
                eyeToLightConnection.mult(1f / (s + t + 1));

                outgoing.add(eyeToLightConnection);
            }

            previousMaterialWasSpecular = eyeVertex.shadingSample.isSpecular;

            // Update alpha
            float q = getEyeTerminationProbability(eyeVertex.index);
            alpha.mult(eyeVertex.shadingSample.brdf);
            alpha.mult(Math.abs(eyeVertex.hitRecord.normal.dot(eyeVertex.shadingSample.w)));
            alpha.mult(1 / (eyeVertex.shadingSample.p * (1 - q)));
        }

        return outgoing;
    }

    protected Spectrum evaluateEyeToLightConnection(PathVertex eyeVertex, PathVertex lightVertex)
    {
        Vector3f eyeToLight = StaticVecmath.sub(lightVertex.hitRecord.position, eyeVertex.hitRecord.position);
        Vector3f lightToEye = StaticVecmath.negate(eyeToLight);
        float d2 = eyeToLight.lengthSquared();

        // Check if eye vertex is in shadow of light vertex
        if(isInShadow(eyeVertex.hitRecord, eyeToLight)) return new Spectrum(0, 0, 0);

        // Normalized connection vectors
        Vector3f eyeToLightNorm = new Vector3f(eyeToLight);
        eyeToLightNorm.normalize();
        Vector3f lightToEyeNorm = new Vector3f(lightToEye);
        lightToEyeNorm.normalize();

        // BRDF at the eye vertex
        Spectrum brdfEye = eyeVertex.hitRecord.material.evaluateBRDF(eyeVertex.hitRecord, eyeVertex.hitRecord.w, eyeToLightNorm);

        // Contribution from light vertex
        Spectrum lightContribution;
        if(lightVertex.isRoot())
        {   // Light vertex is the first in the light path, need to evaluate emission
            lightContribution = lightVertex.hitRecord.material.evaluateEmission(lightVertex.hitRecord, lightToEyeNorm);
            lightContribution.mult(1 / lightVertex.hitRecord.p);
        } else {
            lightContribution = lightVertex.hitRecord.material.evaluateBRDF(lightVertex.hitRecord, lightVertex.hitRecord.w, lightToEyeNorm);
        }

        // Cosine term for eye vertex
        float cosEye = eyeVertex.hitRecord.normal.dot(eyeToLightNorm);

        // Cosine term for light vertex
        float cosLight = 1; // In case it is a point light
        if(lightVertex.hitRecord.normal != null)
        {   // In case the path connects to the back of the surface, the cos is set to zero
            cosLight = Math.max(0, lightVertex.hitRecord.normal.dot(lightToEyeNorm));
        }

        Spectrum s = new Spectrum(brdfEye);
        s.mult(lightContribution);
        s.mult(cosEye);
        s.mult(cosLight / d2);
        return s;
    }

    protected Path createEyePath(Ray r)
    {
        Path path = new Path();

        // The first vertex in the eye path is the camera
        PathVertex cameraVertex = new PathVertex();
        cameraVertex.index = 0;
        path.add(cameraVertex);

        Ray nextRay = r;
        int k = 1;
        while(true)
        {
            HitRecord surfaceHit = root.intersect(nextRay);

            if(surfaceHit == null) break;

            // Make the current vertex
            PathVertex current = new PathVertex();
            current.hitRecord = surfaceHit;
            current.shadingSample = surfaceHit.material.getShadingSample(surfaceHit, (new RandomSampler().makeSamples(1, 2)[0]));
            current.index = k;
            path.add(current);

            if(lightList.contains(surfaceHit.intersectable)) break;
            if(terminateEyePath(k)) break;

            // Prepare the intersection for the next vertex
            nextRay = new Ray(surfaceHit.position, current.shadingSample.w);
            epsilonTranslation(nextRay, nextRay.direction);
            k++;
        }

        return path;
    }

    protected Path createLightPath()
    {
        Path lightPath = new Path();

        // Sample the light source
        LightGeometry lightSource = getRandomLight();
        RandomSampler sampler = new RandomSampler();
        HitRecord lightHit = lightSource.sample(sampler.makeSamples(1, 2)[0]);
        lightHit.p /= lightList.size();

        // The first vertex in the light path is the sample on the light source
        PathVertex start = new PathVertex();
        start.hitRecord = lightHit;
        start.shadingSample = lightHit.material.getEmissionSample(lightHit, sampler.makeSamples(1, 2)[0]);
        start.index = 0;
        lightPath.add(start);

        // Trace ray from light source and create path
        PathVertex current = start;
        int k = 1;
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

            current = new PathVertex();
            current.hitRecord = hit;
            current.shadingSample = hit.material.getShadingSample(hit, sampler.makeSamples(1, 2)[0]);
            current.index = k;
            lightPath.add(current);
            k++;
        }

        return lightPath;
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

    private float getLightTerminationProbability(int k)
    {
        return getTerminationProbability(k, lightTerminationProbability, minLightDepth, maxLightDepth);
    }

    private float getEyeTerminationProbability(int k)
    {
        return getTerminationProbability(k, eyeTerminationProbability, minEyeDepth, maxEyeDepth);
    }

    private float getTerminationProbability(int k, float terminationProbability, int minDepth, int maxDepth)
    {
        float q = terminationProbability;
        if(k <= minDepth) q = 0;
        if(k >= maxDepth) q = 1;
        return q;
    }
}
