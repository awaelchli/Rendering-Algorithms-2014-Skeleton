package rt.materials;

import rt.HitRecord;
import rt.Material;
import rt.Ray;
import rt.Spectrum;

import javax.vecmath.Vector3f;

/**
 * Created by Adrian on 16.03.2016.
 */
public class Mirror implements Material {

    Spectrum reflectivity;

    public Mirror()
    {
        this(new Spectrum(1, 1, 1));
    }

    public  Mirror(Spectrum reflectivity)
    {
        this.reflectivity = reflectivity;
    }

    @Override
    public Spectrum evaluateBRDF(HitRecord hitRecord, Vector3f wOut, Vector3f wIn)
    {
        return new Spectrum(reflectivity);
    }

    @Override
    public Spectrum evaluateEmission(HitRecord hitRecord, Vector3f wOut)
    {
        return new Spectrum(0, 0, 0);
    }

    @Override
    public boolean hasSpecularReflection()
    {
        return true;
    }

    @Override
    public ShadingSample evaluateSpecularReflection(HitRecord hitRecord)
    {
        Ray reflectedRay = Ray.reflect(hitRecord);
        ShadingSample s = new ShadingSample();
        s.brdf = evaluateBRDF(hitRecord, hitRecord.w, reflectedRay.direction);
        s.w = reflectedRay.direction;
        s.p = 1;
        s.isSpecular = true;
        return s;
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
        ShadingSample shadingSample = evaluateSpecularReflection(hitRecord);
        float cos = Math.abs(hitRecord.normal.dot(shadingSample.w));
        shadingSample.brdf.mult(1 / cos);
        return shadingSample;
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
}
