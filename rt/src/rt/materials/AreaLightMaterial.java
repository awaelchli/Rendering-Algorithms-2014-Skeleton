package rt.materials;

import rt.HitRecord;
import rt.Material;
import rt.Spectrum;

import javax.vecmath.Vector3f;

/**
 * Created by Adrian on 10.04.2016.
 */
public class AreaLightMaterial implements Material
{
    Spectrum power;
    float area;

    public AreaLightMaterial(Spectrum power, float area)
    {
        this.power = power;
        this.area = area;
    }

    @Override
    public Spectrum evaluateBRDF(HitRecord hitRecord, Vector3f wOut, Vector3f wIn)
    {
        return new Spectrum(power);
    }

    @Override
    public Spectrum evaluateEmission(HitRecord hitRecord, Vector3f wOut)
    {
        Spectrum emission = new Spectrum(power);
        emission.mult(1 / area);
        emission.mult((float) (1 / Math.PI));
        return emission;
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
        return new ShadingSample(new Spectrum(), new Spectrum(), new Vector3f(), false, 0);
    }

    @Override
    public ShadingSample getEmissionSample(HitRecord hitRecord, float[] sample)
    {
        return (new Diffuse(power)).getShadingSample(hitRecord, sample);
    }

    @Override
    public boolean castsShadows()
    {
        return false;
    }
}
