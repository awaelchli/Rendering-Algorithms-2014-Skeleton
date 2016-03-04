package rt.materials;

import rt.HitRecord;
import rt.Material;
import rt.Spectrum;

import javax.vecmath.Vector3f;

/**
 * Created by Adrian on 03.03.2016.
 */
public class Blinn implements Material {

    Spectrum kd;

    Spectrum ks;

    float shininess;

    public Blinn(Spectrum kd, Spectrum ks, float shininess) {
        this.kd = kd;
        this.ks = ks;
        this.shininess = shininess;
    }

    @Override
    public Spectrum evaluateBRDF(HitRecord hitRecord, Vector3f wOut, Vector3f wIn) {
        Vector3f h = new Vector3f();
        h.add(wIn, wOut);
        h.normalize();

        float hDotn = h.dot(hitRecord.normal);
        float hDotns = (float) Math.pow(hDotn, shininess);
        Spectrum brdf = new Spectrum(ks);
        brdf.mult(hDotns);

        // Add constant diffuse term
        brdf.add(kd);

        return brdf;
    }

    @Override
    public Spectrum evaluateEmission(HitRecord hitRecord, Vector3f wOut) {
        return new Spectrum(0, 0, 0);
    }

    @Override
    public boolean hasSpecularReflection() {
        return false;
    }

    @Override
    public ShadingSample evaluateSpecularReflection(HitRecord hitRecord) {
        return null;
    }

    @Override
    public boolean hasSpecularRefraction() {
        return false;
    }

    @Override
    public ShadingSample evaluateSpecularRefraction(HitRecord hitRecord) {
        return null;
    }

    @Override
    public ShadingSample getShadingSample(HitRecord hitRecord, float[] sample) {
        return null;
    }

    @Override
    public ShadingSample getEmissionSample(HitRecord hitRecord, float[] sample) {
        return new ShadingSample();
    }

    @Override
    public boolean castsShadows() {
        return true;
    }
}
