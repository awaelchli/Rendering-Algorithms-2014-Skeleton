package rt.materials;

import rt.*;
import sun.security.provider.SHA;

import javax.vecmath.Vector3f;
import java.util.Random;

/**
 * Created by Adrian on 11.03.2016.
 */
public class Refractive implements Material {

    float refractiveIndex;

    /**
     * Creates a fully transparent refractive material.
     *
     * @param refractiveIndex   The refractive index of the material.
     */
    public Refractive(float refractiveIndex) {
        this.refractiveIndex = refractiveIndex;
    }

    @Override
    public Spectrum evaluateBRDF(HitRecord hitRecord, Vector3f wOut, Vector3f wIn) {
        return new Spectrum(1, 1, 1);
    }

    @Override
    public Spectrum evaluateEmission(HitRecord hitRecord, Vector3f wOut) {
        return new Spectrum(0, 0, 0);
    }

    @Override
    public boolean hasSpecularReflection() {
        return true;
    }

    @Override
    public ShadingSample evaluateSpecularReflection(HitRecord hitRecord)
    {
        return evaluateSpecularReflection(new RefractionData(hitRecord));
    }

    public ShadingSample evaluateSpecularReflection(RefractionData data)
    {
        ShadingSample s = new ShadingSample();
        Ray reflectedRay = Ray.reflect(data.hitRecord);
        s.w = reflectedRay.direction;
        s.brdf = evaluateBRDF(data.hitRecord, data.hitRecord.w, s.w);
        s.brdf.mult(data.fresnelTerm);
        s.emission = evaluateEmission(data.hitRecord, data.hitRecord.w);
        s.isSpecular = true;
        s.p = data.fresnelTerm;
        return s;
    }

    @Override
    public boolean hasSpecularRefraction() {
        return true;
    }

    @Override
    public ShadingSample evaluateSpecularRefraction(HitRecord hitRecord)
    {
        return evaluateSpecularRefraction(new RefractionData(hitRecord));
    }

    public ShadingSample evaluateSpecularRefraction(RefractionData data)
    {
        ShadingSample s = new ShadingSample();
        s.w = data.computeRefractedDirection();

        if(data.hasTotalInternalReflection())
        {
            s.brdf = new Spectrum();
        }
        else
        {
            s.brdf = evaluateBRDF(data.hitRecord, data.hitRecord.w, s.w);
            s.brdf.mult(1 - data.fresnelTerm);
        }

        s.emission = evaluateEmission(data.hitRecord, data.hitRecord.w);
        s.isSpecular = true;
        s.p = 1 - data.fresnelTerm;
        return s;
    }

    @Override
    public ShadingSample getShadingSample(HitRecord hitRecord, float[] sample)
    {
        RefractionData rData = new RefractionData(hitRecord);

        if(sample[0] < rData.fresnelTerm)
        {
            return evaluateSpecularReflection(rData);
        }
        else
        {
            return evaluateSpecularRefraction(rData);
        }
    }

    @Override
    public ShadingSample getEmissionSample(HitRecord hitRecord, float[] sample)
    {
        return null;
    }

    @Override
    public boolean castsShadows()
    {
        return false;
    }

    /**
     * Holds data for refraction of incident light with Schlick's approximation.
     */
    class RefractionData
    {
        HitRecord hitRecord;
        float fresnelTerm;
        Vector3f incident;
        float cosI, cosT, sinT2, n;

        RefractionData(HitRecord hitRecord)
        {
            this.hitRecord = hitRecord;
            incident = StaticVecmath.negate(hitRecord.w);
            incident.normalize();

            cosI = -hitRecord.normal.dot(incident);

            if(isRayEntering())
                n = 1 / refractiveIndex;

            sinT2 = n * n * (1 - cosI * cosI);
            cosT = (float) Math.sqrt(1 - sinT2);

            fresnelTerm = computeFresnelTerm();
        }

        private float computeFresnelTerm()
        {
            if(hasTotalInternalReflection())
                return 1;

            float x = (n > 1) ? (1 - cosT) : (1 - cosI);
            float r0 = (n - 1) / (n + 1);
            r0 *= r0;

            return r0 + (1 - r0) * x * x * x * x * x;
        }

        Vector3f computeRefractedDirection()
        {
            Vector3f dir = new Vector3f(hitRecord.normal);

            float cosI = this.cosI;
            if(!isRayEntering())
            {
                // Ray is leaving the material
                cosI *= -1;
                dir.negate();
            }

            if (hasTotalInternalReflection())
                return null;

            dir.scale(n * cosI - cosT);
            dir.scaleAdd(n, incident, dir);
            return dir;
        }

        boolean hasTotalInternalReflection()
        {
            return sinT2 > 1;
        }

        boolean isRayEntering()
        {
            return cosI > 0;
        }
    }
}
