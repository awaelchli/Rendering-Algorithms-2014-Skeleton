package rt.integrators;

import rt.*;
import rt.samplers.RandomSampler;

import javax.vecmath.Point3f;
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
        Path path = trace(r);


        for(PathVertex vertex : path)
        {
            if(vertex.index == 0) continue;

            if(lightList.contains(vertex.hitRecord.intersectable))
            {   // The ray 'accidentally' hit the light source, do not further trace the ray
                if(vertex.index == 1) // Eye ray directly hit the light source
                    color.add(vertex.hitRecord.material.evaluateEmission(vertex.hitRecord, vertex.hitRecord.w));
                break;
            }

            if(!vertex.shadingSample.isSpecular)
            {   // Do not add light source contribution on specular surfaces (mirrors, refractive materials)
                Spectrum lightSourceContribution = lightSourceContribution(vertex.hitRecord);
                lightSourceContribution.mult(vertex.alpha);
                color.add(lightSourceContribution);
            }

//            // Go to next path segment
//            Ray nextRay = new Ray(surfaceHit.position, shadingSample.w);
//            epsilonTranslation(nextRay, nextRay.direction);
//            HitRecord nextSurfaceHit = root.intersect(nextRay);
//
//            // Evaluate the transmission
//            if(nextSurfaceHit != null && cos < 0)
//            {
//                Spectrum transmission = surfaceHit.material.evaluateTransmission(nextRay, 0, nextSurfaceHit.t);
//                //System.out.println(transmission);
//                alpha.mult(transmission);
//            }
        }
        return color;
    }

    public Path trace(Ray r)
    {
        Path path = new Path();

        // The first vertex in the eye path is the camera
        PathVertex cameraVertex = makeCameraVertex(r);
        path.add(cameraVertex);

        Spectrum alpha = new Spectrum(cameraVertex.alpha);

        Ray nextRay = r;
        while(true)
        {
            if(terminatePath(path.length())) break;

            HitRecord surfaceHit = root.intersect(nextRay);

            if(surfaceHit == null) break;

            // Make the current vertex
            PathVertex current = new PathVertex();
            current.hitRecord = surfaceHit;
            current.shadingSample = surfaceHit.material.getShadingSample(surfaceHit, (new RandomSampler().makeSamples(1, 2)[0]));
            current.index = path.numberOfVertices();
            current.alpha = new Spectrum(alpha);
            path.add(current);

            if(lightList.contains(surfaceHit.intersectable)) break;

            // Update alpha
            float q = getTerminationProbability(current.index);
            alpha.mult(current.shadingSample.brdf);
            alpha.mult(Math.abs(current.hitRecord.normal.dot(current.shadingSample.w)));
            alpha.mult(1 / (current.shadingSample.p * (1 - q)));

            // Prepare the intersection for the next vertex
            nextRay = new Ray(surfaceHit.position, current.shadingSample.w);
            epsilonTranslation(nextRay, nextRay.direction);
        }

        assert path.numberOfVertices() <= maxDepth;
        return path;
    }

    private PathVertex makeCameraVertex(Ray cameraRay)
    {
        PathVertex cameraVertex = new PathVertex();
        cameraVertex.index = 0;
        cameraVertex.hitRecord = new HitRecord();
        cameraVertex.hitRecord.position = new Point3f(cameraRay.origin);
        cameraVertex.alpha = new Spectrum(1, 1, 1);
        return cameraVertex;
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
