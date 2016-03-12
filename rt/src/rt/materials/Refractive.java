package rt.materials;

import rt.*;

import javax.vecmath.Vector3f;

/**
 * Created by Adrian on 11.03.2016.
 */
public class Refractive implements Material {

    float refractionRatio;

    public Refractive(float refractionRatio) {
        this.refractionRatio = refractionRatio;
    }

    @Override
    public Spectrum evaluateBRDF(HitRecord hitRecord, Vector3f wOut, Vector3f wIn) {
        return new Spectrum(0, 0, 0);
    }

    @Override
    public Spectrum evaluateEmission(HitRecord hitRecord, Vector3f wOut) {
        return null;
    }

    @Override
    public boolean hasSpecularReflection() {
        return true;
    }

    @Override
    public ShadingSample evaluateSpecularReflection(HitRecord hitRecord) {

        Ray reflectedRay = Ray.reflect(hitRecord);
        float schlick = rSchlick2(hitRecord.normal, StaticVecmath.negate(hitRecord.w), refractionRatio);

        ShadingSample s = new ShadingSample();
        s.brdf = new Spectrum(1, 1, 1);
        s.brdf.mult(schlick);
        s.isSpecular = true;
        s.w = reflectedRay.direction;
        return s;
    }

    @Override
    public boolean hasSpecularRefraction() {
        return true;
    }

    @Override
    public ShadingSample evaluateSpecularRefraction(HitRecord hitRecord) {

        Ray refractedRay = Ray.refract(hitRecord, refractionRatio);
        float schlick = rSchlick2(hitRecord.normal, StaticVecmath.negate(hitRecord.w), refractionRatio);

        ShadingSample s = new ShadingSample();

        if (refractedRay == null) {
            s.brdf = new Spectrum(0, 0, 0);
            return s;
        }
        s.brdf = new Spectrum(1, 1, 1);
        s.brdf.mult(1 - schlick);
        s.isSpecular = true;
        s.w = refractedRay.direction;

        return s;
    }

    @Override
    public ShadingSample getShadingSample(HitRecord hitRecord, float[] sample) {
        return null;
    }

    @Override
    public ShadingSample getEmissionSample(HitRecord hitRecord, float[] sample) {
        return null;
    }

    @Override
    public boolean castsShadows() {
        return false;
    }

    public static float rSchlick2(Vector3f normal, Vector3f incident, float n) {

        float r0 = (n - 1) / (n + 1);
        r0 *= r0;
        float cosI = -normal.dot(incident);

        if (n > 1) {
            // n1 > n2
            float sinT2 = n * n * (1 - cosI * cosI);
            if (sinT2 > 1) return 1; // Total internal reflection

            cosI = (float) Math.sqrt(1 - sinT2);
        }

        float x = 1 -  cosI;
        return r0 + (1 - r0) * x * x * x * x * x;
    }
}
