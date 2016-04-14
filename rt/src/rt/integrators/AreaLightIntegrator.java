package rt.integrators;

import rt.*;
import rt.samplers.RandomSampler;

import javax.vecmath.Vector3f;

/**
 * Created by Adrian on 10.04.2016.
 */
public class AreaLightIntegrator extends WhittedIntegrator
{
    float areaLightSamplesPerUnitArea = 1;

    public AreaLightIntegrator(Scene scene, int recursionDepth)
    {
        super(scene, recursionDepth);
    }

    @Override
    protected Spectrum integrateLightSource(LightGeometry lightSource, HitRecord surfaceHit)
    {
        int numSamples = Math.round(areaLightSamplesPerUnitArea * lightSource.area());
        // At least one sample is required
        numSamples = Math.max(numSamples, 1);
        float[][] samples = this.makePixelSamples(new RandomSampler(), numSamples);

        Spectrum contribution = new Spectrum();

        // If the surface has emission
        contribution.add(surfaceHit.material.evaluateEmission(surfaceHit, surfaceHit.w));

        for (float[] sample : samples)
        {
            HitRecord lightHit = lightSource.sample(sample);
            Spectrum s = evaluateLightSample(lightHit, surfaceHit);
            contribution.add(s);
        }
        contribution.mult(1f / numSamples);

        return contribution;
    }

    protected Spectrum evaluateLightSample(HitRecord lightHit, HitRecord surfaceHit)
    {
        Vector3f lightDir = StaticVecmath.sub(lightHit.position, surfaceHit.position);

        // Check if point on surface lies in shadow of current light source sample
        if (isInShadow(surfaceHit, lightDir))
        {
            // Shadow ray hit another occluding surface
            return new Spectrum(0, 0, 0);
        }

        // Check if the surface points towards the visible side of the area light
        if (lightHit.normal != null && lightHit.normal.dot(lightDir) > 0)
        {
            return new Spectrum(0, 0, 0);
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

        return s;
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
}
