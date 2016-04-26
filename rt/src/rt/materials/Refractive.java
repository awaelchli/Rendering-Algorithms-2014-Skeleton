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
    public ShadingSample evaluateSpecularReflection(HitRecord hitRecord) {

//        Ray reflectedRay = Ray.reflect(hitRecord);
//
//        ShadingSample s = new ShadingSample();
//        s.brdf = evaluateBRDF(hitRecord, hitRecord.w, reflectedRay.direction);
//        s.w = reflectedRay.direction;
//        s.p = 1;
//        s.isSpecular = true;
//        return s;
        return evaluateSpecularReflection(new RefractionData(hitRecord));
    }

    public ShadingSample evaluateSpecularReflection(RefractionData data)
    {
        ShadingSample s = new ShadingSample();
        Ray reflectedRay = Ray.reflect(data.hitRecord);
        s.w = reflectedRay.direction;
        s.brdf = evaluateBRDF(data.hitRecord, data.hitRecord.w, s.w);
        s.emission = evaluateEmission(data.hitRecord, data.hitRecord.w);
        s.isSpecular = true;
        s.p = 1 - data.fresnelTerm;
        return s;
    }

    @Override
    public boolean hasSpecularRefraction() {
        return true;
    }

    @Override
    public ShadingSample evaluateSpecularRefraction(HitRecord hitRecord)
    {

//        Vector3f incident = new Vector3f(hitRecord.w);
//        incident.normalize();
//        incident.negate();
//
//        ShadingSample s = refract_schlick(hitRecord.normal, incident, refractiveIndex);
//
//        if (s.w == null) {
//            s.brdf = new Spectrum(0, 0, 0);
//            return s;
//        }
//
//        s.brdf = new Spectrum(1, 1, 1);
//        s.isSpecular = true;
//
//        return s;
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
        }

        s.emission = evaluateEmission(data.hitRecord, data.hitRecord.w);
        s.isSpecular = true;
        s.p = data.fresnelTerm;
        return s;
    }

    @Override
    public ShadingSample getShadingSample(HitRecord hitRecord, float[] sample)
    {
        RefractionData rData = new RefractionData(hitRecord);

        if(sample[0] < rData.fresnelTerm)
        {
            return evaluateSpecularRefraction(rData);
        }
        else
        {
            return evaluateSpecularReflection(rData);
        }
    }

    @Override
    public ShadingSample getEmissionSample(HitRecord hitRecord, float[] sample) {
        return null;
    }

    @Override
    public boolean castsShadows() {
        return false;
    }

    /**
     * Refraction of incident light with Schlick's approximation.
     *
     * @param normal    The surface normal (normalized)
     * @param incident  The incident light direction (normalized)
     * @param index     Refractive index of the material
     * @return          The shading sample containing the refracted direction w and fresnel term p.
     *                  If the total internal reflection occurs, w is set to null and p = 0.
     */
    public static ShadingSample refract_schlick(Vector3f normal, Vector3f incident, float index) {

        ShadingSample s = new ShadingSample();
        s.w = new Vector3f(normal);

        float cosI = -normal.dot(incident);
        float n = index;

        if (cosI > 0) {
            // Ray is entering the material
            n = 1 / index;
        } else {
            // Ray is leaving the material
            cosI *= -1;
            s.w.negate();
        }

        float r0 = (n - 1) / (n + 1);
        r0 *= r0;
        float sinT2 = n * n * (1 - cosI * cosI);

        if (sinT2 > 1) {
            // Total internal reflection
            s.w = null;
            s.p = 0;
            return s;
        }

        float cosT = (float) Math.sqrt(1 - sinT2);
        s.w.scale(n * cosI - cosT);
        s.w.scaleAdd(n, incident, s.w);

        if (n > 1) {
            cosI = cosT;
        }

        float x = 1 - cosI;
        s.p = r0 + (1 - r0) * x * x * x * x * x;

        return s;
    }

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
                return 0;

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
