package rt.materials;

import rt.HitRecord;
import rt.Material;
import rt.Spectrum;
import rt.StaticVecmath;

import javax.vecmath.Vector3f;

/**
 * Created by adrian on 14.04.16.
 */
public class Glossy implements Material
{
    Diffuse diffuse;
    float roughness;
    Spectrum eta;
    Spectrum k;

    public Glossy(float roughness, Spectrum refractiveIndex, Spectrum absorption)
    {
        this.diffuse = new Diffuse(new Spectrum(0, 0, 0));
        this.eta = refractiveIndex;
        this.k = absorption;
        this.roughness = roughness;
    }

    public Glossy(float roughness, Spectrum refractiveIndex, Spectrum absorption, Spectrum diffuse)
    {
        this.diffuse = new Diffuse(diffuse);
        this.eta = refractiveIndex;
        this.k = absorption;
        this.roughness = roughness;
    }

    public Glossy(Spectrum diffuse, float refractiveIndex, float absorption, float roughness)
    {
        this.diffuse = new Diffuse(diffuse);
        this.eta = new Spectrum(refractiveIndex, refractiveIndex, refractiveIndex);
        this.k = new Spectrum(absorption, absorption, absorption);
        this.roughness = roughness;
    }

    @Override
    public Spectrum evaluateBRDF(HitRecord hitRecord, Vector3f wOut, Vector3f wIn)
    {
        Vector3f halfVector = computeHalfVector(wOut, wIn);
        Vector3f normal = hitRecord.normal;
        float e = this.roughness;

        float nDotHalf = halfVector.dot(normal);
        float nDotOut = wOut.dot(normal);
        float nDotIn = wIn.dot(normal);
        float outDotHalf = halfVector.dot(wOut);

        // Microfacet Distribution (Beckmann Distribution)
        float d = (float) ((e + 2) * Math.pow(nDotHalf, e) / (2 * Math.PI));

        // Geometry term
        float g1 = 2 * nDotHalf * nDotOut / outDotHalf;
        float g2 = 2 * nDotHalf * nDotIn / outDotHalf;
        float g = Math.min(1, Math.min(g1, g2));

        // Fresnel term
        float f_r = fresnel_reflectance(this.eta.r, this.k.r, nDotIn);
        float f_g = fresnel_reflectance(this.eta.g, this.k.g, nDotIn);
        float f_b = fresnel_reflectance(this.eta.b, this.k.b, nDotIn);

        // Cosine terms
        float cosTerms = 4 * nDotOut * nDotIn;

        // Torrance Sparrow BRDF for Microfacets
        Spectrum brdf = new Spectrum(f_r, f_g, f_b);
        brdf.mult(d * g / cosTerms);

        // Add diffuse term
        Spectrum diff = diffuse.evaluateBRDF(hitRecord, wOut, wIn);
        diff.mult(new Spectrum(1 - f_r, 1 - f_g, 1 - f_b));
        brdf.add(diff);

        return brdf;
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
        // Construct a random direction over the hemisphere using the cosine distribution
        float phi = (float) (2 * Math.PI * sample[1]);
        float r = (float) Math.pow(sample[0], 1 / (roughness + 1));
        Vector3f halfVector = new Vector3f();
        float tmp = (float) Math.sqrt(sample[0]);
        halfVector.x = (float) (Math.cos(phi) * tmp);
        halfVector.y = (float) (Math.sin(phi) * tmp);
        halfVector.z = (float) (Math.sqrt(1 - sample[0]));

        // Transform the random direction to tangent space
        hitRecord.toTangentSpace(halfVector);

        // Direction of incident light
        Vector3f incident = StaticVecmath.reflect(hitRecord.w, halfVector);

        // PDF of sampled half vector
        float p_h = (float) ((roughness + 1) * Math.pow(r, roughness)/ (2 * Math.PI));

        // PDF of the incident direction
        float p_i = p_h / (4 * halfVector.dot(hitRecord.w));

        // Create the shading sample
        ShadingSample s = new ShadingSample();
        s.brdf = new Spectrum();
        if(incident.dot(hitRecord.normal) > 0)
        {   // Incident direction is pointing away from the surface
            s.brdf = evaluateBRDF(hitRecord, hitRecord.w, incident);
        }
        s.w = incident;
        s.p = p_i;
        s.isSpecular = false;
        s.emission = new Spectrum(); // No emission
        return s;
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

    private Vector3f computeHalfVector(Vector3f wOut, Vector3f wIn)
    {
        Vector3f halfVector = new Vector3f(wIn);
        halfVector.add(wOut);
        halfVector.normalize();
        return halfVector;
    }

    private float fresnel_reflectance(float eta, float k, float cosi)
    {
        return (rParl2(eta, k, cosi) + rPerp2(eta, k, cosi)) / 2;
    }

    private float rParl2(float eta, float k, float cosi)
    {
        float tmp1 = (eta * eta + k * k) * cosi * cosi;
        float tmp2 = 2 * eta * cosi;
        return (tmp1 - tmp2 + 1) / (tmp1 + tmp2 + 1);
    }

    private float rPerp2(float eta, float k, float cosi)
    {
        float tmp1 = eta * eta + k * k;
        float tmp2 = 2 * eta * cosi;
        float tmp3 = cosi * cosi;
        return (tmp1 - tmp2 + tmp3) / (tmp1 + tmp2 + tmp3);
    }
}
