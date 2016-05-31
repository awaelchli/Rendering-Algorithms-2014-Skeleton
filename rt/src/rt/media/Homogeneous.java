package rt.media;

import rt.*;

import javax.vecmath.Vector3f;

/**
 * Created by adrian on 20.05.16.
 */
public abstract class Homogeneous implements Material, Medium
{
    int n = 10;
//    Spectrum sigma_t;
//    Spectrum sigma_s;
//    Spectrum emission;
//    int n;
//
//    public Homogeneous(Spectrum sigma_t, Spectrum sigma_s, Spectrum emission, int nSamples)
//    {
//        this.sigma_t = new Spectrum(sigma_t);
//        this.sigma_s = new Spectrum(sigma_s);
//        this.emission = new Spectrum(emission);
//        this.n = nSamples;
//    }

    @Override
    public Spectrum evaluateTransmission(Ray r, float s_in, float s_out)
    {
        Spectrum transmission = new Spectrum(1, 1, 1);

        float ds = (s_out - s_in) / n;
        for(float s_i = s_in; s_i <= s_out; s_i += ds)
        {
            transmission.mult(evaluateTransmission(ds));
        }

        return transmission;
    }

    @Override
    public Spectrum evaluateTransmission(float ds)
    {
        Spectrum sigma_t = getAbsorptionCoefficient();
        Spectrum transmission = new Spectrum();
        transmission.r = (float) Math.exp(-sigma_t.r * ds);
        transmission.g = (float) Math.exp(-sigma_t.g * ds);
        transmission.b = (float) Math.exp(-sigma_t.b * ds);

        return  transmission;
    }

    @Override
    public Spectrum evaluateBRDF(HitRecord hitRecord, Vector3f wOut, Vector3f wIn)
    {
        return new Spectrum();
    }

    @Override
    public Spectrum evaluateEmission(HitRecord hitRecord, Vector3f wOut)
    {
        return new Spectrum();
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
        ShadingSample s = new ShadingSample();
        s.w = StaticVecmath.negate(hitRecord.w);
        s.brdf = new Spectrum(1, 1, 1);
        s.brdf.mult(1f / Math.abs(hitRecord.normal.dot(s.w)));
        s.emission = evaluateEmission(hitRecord, hitRecord.w);
        s.isSpecular = false;
        s.p = 1;
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

    @Override
    public float getProbability(HitRecord hitRecord, Vector3f direction)
    {
        return 0;
    }

    @Override
    public Medium getMedium()
    {
        return this;
    }
}
