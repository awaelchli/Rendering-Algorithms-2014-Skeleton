package rt.integrators;

import rt.*;
import rt.samplers.RandomSampler;

import javax.vecmath.Point2f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import java.util.HashMap;

/**
 * Created by adrian on 04.05.16.
 */
public class BDPathTracingIntegrator extends AbstractIntegrator
{

    public static final int DEFAULT_MAX_VERTICES = 2;
    public static final int DEFAULT_MIN_VERTICES = 2;
    public static final float DEFAULT_TERMINATION_PROBABILITY = 0.5f;

    int minEyeVertices, maxEyeVertices;
    int minLightVertices, maxLightVertices;
    float eyeTerminationProbability, lightTerminationProbability;

    Film lightImage;

    private Scene scene;

    public BDPathTracingIntegrator(Scene scene)
    {
        super(scene);
        this.scene = scene;
        maxEyeVertices = DEFAULT_MAX_VERTICES;
        maxLightVertices = DEFAULT_MAX_VERTICES;
        minEyeVertices = DEFAULT_MIN_VERTICES;
        minLightVertices = DEFAULT_MIN_VERTICES;
        eyeTerminationProbability = DEFAULT_TERMINATION_PROBABILITY;
        lightTerminationProbability = DEFAULT_TERMINATION_PROBABILITY;
    }

    @Override
    public Spectrum integrate(Ray r)
    {
        Path eyePath = createEyePath(r);
        Path lightPath = createLightPath();

        Spectrum outgoing = new Spectrum();

        boolean previousMaterialWasSpecular = false;

        // Traverse eye path and connect to the vertices of the light path
        for(PathVertex eyeVertex : eyePath)
        {
            if(lightList.contains(eyeVertex.hitRecord.intersectable))
            {   // Do not trace eye path further if a light source is hit
                if(eyeVertex.index == 1 || previousMaterialWasSpecular)
                {   // Exception: Add the material emission if it is the first bounce (direct light hit).
                    Spectrum emission = eyeVertex.hitRecord.material.evaluateEmission(eyeVertex.hitRecord, eyeVertex.hitRecord.w);
                    outgoing.add(emission);
                }
                break;
            }

            if(eyeVertex.isRoot())
            {
                connectToCameraVertex(eyeVertex, lightPath);
                continue;
            }

            // Connect the current eye vertex with every vertex in the light path
            for(PathVertex lightVertex : lightPath)
            {
                Spectrum eyeToLightConnection = evaluateEyeToLightConnection(eyeVertex, lightVertex);

                eyeToLightConnection.mult(eyeVertex.alpha);
                eyeToLightConnection.mult(lightVertex.alpha);

                int s = lightVertex.index + 1;
                int t = eyeVertex.index + 1;
                eyeToLightConnection.mult(computeMISWeight(s, t, lightPath, eyePath));

                outgoing.add(eyeToLightConnection);
            }

            previousMaterialWasSpecular = !eyeVertex.isRoot() && eyeVertex.shadingSample.isSpecular;
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
        PathVertex cameraVertex = makeCameraVertex(r);
        path.add(cameraVertex);

        Spectrum alpha = new Spectrum(cameraVertex.alpha);

        Ray nextRay = r;
        while(true)
        {
            if(terminateEyePath(path.length())) break;

            HitRecord surfaceHit = root.intersect(nextRay);

            if(surfaceHit == null) break;

            // Make the current vertex
            PathVertex current = new PathVertex();
            current.hitRecord = surfaceHit;
            current.shadingSample = surfaceHit.material.getShadingSample(surfaceHit, (new RandomSampler().makeSamples(1, 2)[0]));
            current.index = path.numberOfVertices();
            current.alpha = new Spectrum(alpha);

            // Compute probabilities
            PathVertex previous = path.get(current.index - 1);
            float d2 = current.dist2(previous);

            if(path.length() == 0)
            {
                current.pE = surfaceHit.normal.dot(surfaceHit.w) / d2;
            } else {
                current.pE = previous.shadingSample.p;
                current.pE *= previous.hitRecord.normal.dot(previous.shadingSample.w);
                current.pE /= d2;
            }

            previous.pL = current.hitRecord.material.getProbability(current.hitRecord, current.hitRecord.w);
            previous.pL *= current.hitRecord.normal.dot(current.hitRecord.w);
            previous.pL /= d2;

            path.add(current);

            if(lightList.contains(surfaceHit.intersectable)) break;

            // Update alpha
            float q = getEyeTerminationProbability(current.index);
            alpha.mult(current.shadingSample.brdf);
            alpha.mult(Math.abs(current.hitRecord.normal.dot(current.shadingSample.w)));
            alpha.mult(1 / (current.shadingSample.p * (1 - q)));

            // Prepare the intersection for the next vertex
            nextRay = new Ray(surfaceHit.position, current.shadingSample.w);
            epsilonTranslation(nextRay, nextRay.direction);
        }

        assert path.numberOfVertices() <= maxEyeVertices;
        return path;
    }

    protected Path createLightPath()
    {
        Path path = new Path();

        // The first vertex in the light path is the sample on the light source
        PathVertex start = makeLightSourceVertex();
        path.add(start);

        // Trace ray from light source and create path
        PathVertex current = start;
        Spectrum alpha = new Spectrum(start.alpha);
        while(true)
        {
            if(terminateLightPath(path.length())) break;

            Point3f currentPos = new Point3f(current.hitRecord.position);
            Vector3f currentDir = new Vector3f(current.shadingSample.w);
            Ray nextRay = new Ray(currentPos, currentDir);
            epsilonTranslation(nextRay, currentDir);
            HitRecord hit = root.intersect(nextRay);

            if(hit == null) break;
            if(lightList.contains(hit.intersectable)) break;

            // Update alpha
            if(path.length() == 0) alpha.mult(current.shadingSample.emission);
            if(path.length() >= 1) alpha.mult(current.shadingSample.brdf);
            alpha.mult(1 / current.shadingSample.p);
            alpha.mult(Math.abs(current.hitRecord.normal.dot(current.shadingSample.w)));

            // Create the new vertex
            current = new PathVertex();
            current.hitRecord = hit;
            current.shadingSample = hit.material.getShadingSample(hit, (new RandomSampler()).makeSamples(1, 2)[0]);
            current.index = path.numberOfVertices();
            current.alpha = new Spectrum(alpha);

            // Compute probabilities
            PathVertex previous = path.get(current.index - 1);
            float d2 = previous.dist2(current);
            current.pL = previous.shadingSample.p;
            current.pL *= previous.hitRecord.normal.dot(previous.shadingSample.w);
            current.pL /= d2;

            previous.pE = current.hitRecord.material.getProbability(current.hitRecord, current.hitRecord.w);
            previous.pE *= current.hitRecord.normal.dot(current.hitRecord.w);
            previous.pE /= d2;

            path.add(current);
        }

        assert path.numberOfVertices() <= maxLightVertices;
        return path;
    }

    protected boolean terminateLightPath(int length)
    {
        return terminatePath(length, minLightVertices - 1, maxLightVertices - 1, lightTerminationProbability);
    }

    protected boolean terminateEyePath(int length)
    {
        return terminatePath(length, minEyeVertices - 1, maxEyeVertices - 1, eyeTerminationProbability);
    }

    private boolean terminatePath(int length, int minLength, int maxLength, float terminationProbability)
    {
        if(length >= maxLength) return true;
        if(length < minLength) return false;

        RandomSampler sampler = new RandomSampler();
        float p =  sampler.makeSamples(1, 1)[0][0];

        // Terminate with given probability above a certain path length
        return p < terminationProbability;
    }

    private float getLightTerminationProbability(int length)
    {
        return getTerminationProbability(length, lightTerminationProbability, minLightVertices - 1, maxLightVertices - 1);
    }

    private float getEyeTerminationProbability(int length)
    {
        return getTerminationProbability(length, eyeTerminationProbability, minEyeVertices - 1, maxEyeVertices - 1);
    }

    private float getTerminationProbability(int length, float terminationProbability, int minLength, int maxLength)
    {
        float q = terminationProbability;
        if(length < minLength) q = 0;
        if(length >= maxLength) q = 1;
        return q;
    }

    private PathVertex makeCameraVertex(Ray cameraRay)
    {
        PathVertex cameraVertex = new PathVertex();
        cameraVertex.index = 0;
        cameraVertex.hitRecord = new HitRecord();
        cameraVertex.hitRecord.position = new Point3f(cameraRay.origin);
        cameraVertex.alpha = new Spectrum(1, 1, 1);
        cameraVertex.pE = 1;
        cameraVertex.pL = 1;
        return cameraVertex;
    }

    private PathVertex makeLightSourceVertex()
    {
        // Sample the light source
        LightGeometry lightSource = getRandomLight();
        RandomSampler sampler = new RandomSampler();
        HitRecord lightHit = lightSource.sample(sampler.makeSamples(1, 2)[0]);
        lightHit.p /= lightList.size();

        PathVertex vertex = new PathVertex();
        vertex.hitRecord = lightHit;
        vertex.shadingSample = lightHit.material.getEmissionSample(lightHit, sampler.makeSamples(1, 2)[0]);
        vertex.index = 0;
        vertex.alpha = new Spectrum(1, 1, 1);
        vertex.alpha.mult(1 / lightHit.p);
        vertex.pL = lightHit.p;
        vertex.pE = 0;

        return vertex;
    }

    private void connectToCameraVertex(PathVertex cameraVertex, Path lightPath)
    {
        for(PathVertex lightVertex : lightPath)
        {
            if (lightVertex.isRoot())
                continue;

            Vector3f lightToCamera = StaticVecmath.sub(cameraVertex.hitRecord.position, lightVertex.hitRecord.position);
            Vector3f cameraToLight = StaticVecmath.negate(lightToCamera);
            float d2 = cameraToLight.lengthSquared();

            Vector3f lightToCameraNorm = StaticVecmath.normalize(lightToCamera);
            Vector3f cameraToLightNorm = StaticVecmath.normalize(cameraToLight);

            // If the camera vertex is in shadow of the light vertex, nothing has to be done
            if (isInShadow(lightVertex.hitRecord, lightToCamera))
                continue;

            Point2f pixel = this.scene.getCamera().project(lightVertex.hitRecord.position);
            boolean validPixel = pixel.x >= 0 && pixel.y >= 0 && pixel.x <= scene.getFilm().getWidth() && pixel.y <= scene.getFilm().getHeight();

            // Check if the connection contributes to the camera image
            if (!validPixel)
                continue;

            // Cosine term for light vertex
            float cosLight = 1; // In case it is a point light
            if (lightVertex.hitRecord.normal != null)
            {   // In case the path connects to the back of the surface, the cos is set to zero
                cosLight = Math.max(0, lightVertex.hitRecord.normal.dot(lightToCameraNorm));
            }

            // Cosine term for the eye vertex
            float cosEye = scene.getCamera().getImagePlaneNormal().dot(cameraToLightNorm);

            // Contribution from light vertex
            Spectrum s = lightVertex.hitRecord.material.evaluateBRDF(lightVertex.hitRecord, lightToCameraNorm, lightVertex.hitRecord.w);
            s.mult(cosLight * cosEye / d2);
            s.mult(lightVertex.alpha);

            lightImage.addSample(pixel.x, pixel.y, s);
        }
    }

    protected float computeMISWeight(int s, int t, Path lightPath, Path eyePath)
    {
        PathVertex lastEyeVertex = eyePath.get(t - 1);
        PathVertex lastLightVertex = lightPath.get(s - 1);

        /*
         *  Compute the values u_{s - i} for 0 < i <= s
         */
        HashMap<Integer, Float> uValues = new HashMap<>();

        for(int i = 2; i <= s; i++)
        {
            PathVertex vertex = lightPath.get(s - i);
            uValues.put(s - i, vertex.pE / vertex.pL);
        }
        // Need to re-compute pE for vertex s - 1 on the light path
        Vector3f eyeToLightDir = lastEyeVertex.vector(lastLightVertex);
        float d2 = eyeToLightDir.lengthSquared();
        eyeToLightDir.normalize();

        float pE_new = lastEyeVertex.hitRecord.material.getProbability(lastEyeVertex.hitRecord, eyeToLightDir);
        pE_new *= lastEyeVertex.hitRecord.normal.dot(eyeToLightDir);
        pE_new /= d2;
        uValues.put(s - 1, pE_new / lastLightVertex.pL);

        /*
         *  Compute the values v_{t - i} for 0 < i <= t
         */
        HashMap<Integer, Float> vValues = new HashMap<>();

        for(int i = 2; i <= t; i++)
        {
            PathVertex vertex = eyePath.get(t - i);
            vValues.put(t - i, vertex.pL / vertex.pE);
        }
        // Need to re-compute pE for vertex s - 1 on the light path
        Vector3f lightToEyeDir = StaticVecmath.negate(eyeToLightDir);
        float pL_new = lastLightVertex.hitRecord.material.getProbability(lastLightVertex.hitRecord, lightToEyeDir);
        pL_new *= lastLightVertex.hitRecord.normal.dot(lightToEyeDir);
        pL_new /= d2;
        vValues.put(t - 1, pL_new / lastEyeVertex.pE);

        /*
         *  Compute the sums for the uValues and the vValues
         */
        float uTempProduct = 1, vTempProduct = 1;
        float uSum = 0, vSum = 0;

        for(int i = 1; i <= s; i++)
        {
            uTempProduct *= uValues.get(s - i);
            uSum += uTempProduct;
        }

        for(int i = 1; i <= t; i++)
        {
            vTempProduct *= vValues.get(t - i);
            vSum += vTempProduct;
        }

        return 1 / (uSum + 1 + vSum);
    }
}
