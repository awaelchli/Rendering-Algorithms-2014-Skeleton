package rt.materials;

import rt.HitRecord;
import rt.Material;
import rt.Spectrum;
import rt.StaticVecmath;

import javax.vecmath.Vector3f;

/**
 * Created by adrian on 14.04.16.
 */
public class Microfacets implements Material
{
    float smoothness;

    public Microfacets(float smoothness)
    {
        this.smoothness = smoothness;
    }

    @Override
    public Spectrum evaluateBRDF(HitRecord hitRecord, Vector3f wOut, Vector3f wIn)
    {
        Vector3f halfVector = computeHalfVector(wOut, wIn);
        Vector3f normal = hitRecord.normal;
        float e = this.smoothness;

        float nDotHalf = halfVector.dot(normal);
        float nDotOut = wOut.dot(normal);
        float nDotIn = wIn.dot(normal);
        float outDotHalf = halfVector.dot(wOut);

        // Blinn Microfacet Distribution (Beckmann Distribution)
        float d = (float) ((e + 2) * Math.pow(nDotHalf, e) / (2 * Math.PI));

        // Geometry term
        float g1 = 2 * nDotHalf * nDotOut / outDotHalf;
        float g2 = 2 * nDotHalf * nDotIn / outDotHalf;
        float g = Math.min(1, Math.min(g1, g2));

        // Fresnel term
        // TODO: Find out correct fresnel term
        float f = 1;

        // Cosine terms
        float cosTerms = 4 * nDotOut * nDotIn;

        // Torrance Sparrow BRDF for Microfacets
        Spectrum brdf = new Spectrum(1, 1, 1);
        brdf.mult(d * g * f / cosTerms);

        return brdf;
    }

    private Vector3f computeHalfVector(Vector3f wOut, Vector3f wIn)
    {
        Vector3f halfVector = new Vector3f(wIn);
        halfVector.add(wOut);
        halfVector.normalize();
        return halfVector;
    }

    @Override
    public Spectrum evaluateEmission(HitRecord hitRecord, Vector3f wOut)
    {
        return new Spectrum(0, 0, 0);
    }

    @Override
    public boolean hasSpecularReflection()
    {
        return false;
    }

    @Override
    public ShadingSample evaluateSpecularReflection(HitRecord hitRecord)
    {
        return null;
    }

    @Override
    public boolean hasSpecularRefraction()
    {
        return false;
    }

    @Override
    public ShadingSample evaluateSpecularRefraction(HitRecord hitRecord)
    {
        return null;
    }

    @Override
    public ShadingSample getShadingSample(HitRecord hitRecord, float[] sample)
    {
        return null;
    }

    @Override
    public ShadingSample getEmissionSample(HitRecord hitRecord, float[] sample)
    {
        return null;
    }

    @Override
    public boolean castsShadows()
    {
        return true;
    }
}
