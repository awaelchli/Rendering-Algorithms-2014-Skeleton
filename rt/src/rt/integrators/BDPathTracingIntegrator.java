package rt.integrators;

import rt.*;
import rt.films.BoxFilterFilm;
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
    int minLightVertices, maxLightVerices;
    float eyeTerminationProbability, lightTerminationProbability;

    Scene scene;
    private HashMap<Point2f, Spectrum> lightImage;

    public BDPathTracingIntegrator(Scene scene)
    {
        super(scene);
        this.scene = scene;
        this.lightImage = new HashMap<>();
        maxEyeVertices = DEFAULT_MAX_VERTICES;
        maxLightVerices = DEFAULT_MAX_VERTICES;
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
                {   // Exception 1: Add the material emission if it is the first bounce (direct light hit).
                    // Exception 2: Add the emission if the path travels through a refractive material or mirror.
                    Spectrum emission = eyeVertex.hitRecord.material.evaluateEmission(eyeVertex.hitRecord, eyeVertex.hitRecord.w);
                    outgoing.add(emission);
                }
                break;
            }

            // Connect the current eye vertex with every vertex in the ligth path
            for(PathVertex lightVertex : lightPath)
            {
                if(eyeVertex.isRoot())
                {
                    connectToCameraVertex(eyeVertex, lightVertex);
                    continue;
                }

                Spectrum eyeToLightConnection = evaluateEyeToLightConnection(eyeVertex, lightVertex);

                eyeToLightConnection.mult(eyeVertex.alpha);
                eyeToLightConnection.mult(lightVertex.alpha);

                // Divide by probability of choosing this connection
                // TODO: MIS
                int s = eyeVertex.index;
                int t = lightVertex.index;
                eyeToLightConnection.mult(1f / (s + t));

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
        Spectrum alpha = new Spectrum(start.shadingSample.emission);
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

            // Update beta
            if(path.length() > 1) alpha.mult(current.shadingSample.brdf);
            alpha.mult(1 / current.shadingSample.p);
            alpha.mult(current.hitRecord.normal.dot(current.shadingSample.w));

            // Create the new vertex
            current = new PathVertex();
            current.hitRecord = hit;
            current.shadingSample = hit.material.getShadingSample(hit, (new RandomSampler()).makeSamples(1, 2)[0]);
            current.index = path.numberOfVertices();
            current.alpha = new Spectrum(alpha);

            path.add(current);
        }

        return path;
    }

    protected boolean terminateLightPath(int length)
    {
        return terminatePath(length, minLightVertices - 1, maxLightVerices - 1, lightTerminationProbability);
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
        return getTerminationProbability(length, lightTerminationProbability, minLightVertices - 1, maxLightVerices - 1);
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

        return vertex;
    }

    private void connectToCameraVertex(PathVertex cameraVertex, PathVertex lightVertex)
    {
        Vector3f lightToCamera = StaticVecmath.sub(cameraVertex.hitRecord.position, lightVertex.hitRecord.position);
        Vector3f cameraToLight = StaticVecmath.negate(lightToCamera);
        float d2 = cameraToLight.lengthSquared();

        Vector3f lightToCameraNorm = StaticVecmath.normalize(lightToCamera);
        Vector3f cameraToLightNorm = StaticVecmath.normalize(cameraToLight);

        // If the camera vertex is in shadow of the light vertex, nothing has to be done
        if(isInShadow(cameraVertex.hitRecord, cameraToLight))
            return;

        Ray cameraRay = new Ray(lightVertex.hitRecord.position, lightToCameraNorm);
        Point2f pixel = this.scene.getCamera().getImagePixel(cameraRay);
        boolean validPixel = pixel.x >= 0 && pixel.y >= 0 && pixel.x <= scene.getFilm().getWidth() && pixel.y <= scene.getFilm().getHeight();

        // Check if the connection contributes to the camera image
        if(!validPixel)
            return;

        // Contribution from light vertex
        Spectrum lightContribution;
        if(lightVertex.isRoot())
        {   // Light vertex is the first in the light path, need to evaluate emission
            lightContribution = lightVertex.hitRecord.material.evaluateEmission(lightVertex.hitRecord, lightToCameraNorm);
            lightContribution.mult(1 / lightVertex.hitRecord.p);
        } else {
            lightContribution = lightVertex.hitRecord.material.evaluateBRDF(lightVertex.hitRecord, lightVertex.hitRecord.w, lightToCameraNorm);
        }

        // Cosine term for light vertex
        float cosLight = 1; // In case it is a point light
        if(lightVertex.hitRecord.normal != null)
        {   // In case the path connects to the back of the surface, the cos is set to zero
            cosLight = Math.max(0, lightVertex.hitRecord.normal.dot(lightToCameraNorm));
        }

        Spectrum s = new Spectrum(1, 1, 1);
        s.mult(lightContribution);
        s.mult(lightVertex.alpha);
        s.mult(cosLight / d2);
        lightImage.put(pixel, s);
    }

    public void addLightImage(Film film)
    {
        for(Point2f p : lightImage.keySet()){
            Spectrum s = lightImage.get(p);
            film.addSample(p.x, p.y, s);
        }
    }

    public BoxFilterFilm getLightImage()
    {
        int width = scene.getFilm().getWidth();
        int height = scene.getFilm().getHeight();
        BoxFilterFilm film = new BoxFilterFilm(width, height);

        addLightImage(film);
        return film;
    }
}
