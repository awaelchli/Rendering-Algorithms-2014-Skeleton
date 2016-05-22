package rt.media;

import rt.*;

import javax.vecmath.Vector3f;

/**
 * Created by adrian on 20.05.16.
 */
public class Homogeneous implements Material, Medium
{
    Spectrum sigma_t;
    Spectrum emission;
    int n;

    public Homogeneous(Spectrum sigma_t, Spectrum emission, int nSamples)
    {
        this.sigma_t = new Spectrum(sigma_t);
        this.emission = new Spectrum(emission);
        this.n = nSamples;
    }

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
        Spectrum transmission = new Spectrum(sigma_t);
        transmission.mult(ds);
        transmission.mult(-1);
        transmission.add(1);
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
        return hitRecord.w.equals(StaticVecmath.negate(direction)) ? 1 : 0;
    }

    @Override
    public Medium getMedium()
    {
        return this;
    }
}
