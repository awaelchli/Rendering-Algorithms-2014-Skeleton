package rt.materials;

import rt.HitRecord;
import rt.Material;
import rt.Spectrum;

import javax.vecmath.Vector3f;

/**
 * Created by Adrian on 10.03.2016.
 */
public class BlinnPlusMirror implements Material {

    Material blinn;
    Spectrum reflectivity;

    private Spectrum f;

    public  BlinnPlusMirror(Spectrum kd, Spectrum ks, float shininess, Spectrum reflectivity) {
        blinn = new Blinn(kd, ks, shininess);
        this.reflectivity = reflectivity;

        // Precompute the constant f for Schlick's approximation
        schlick_precompute(reflectivity);
    }

    private void schlick_precompute(Spectrum reflectivity) {
        float r_r = reflectivity.r;
        float r_g = reflectivity.g;
        float r_b = reflectivity.b;

        float f_r = (1 - r_r) / (1 + r_r);
        float f_g = (1 - r_g) / (1 + r_g);
        float f_b = (1 - r_b) / (1 + r_b);

        this.f = new Spectrum(f_r, f_g, f_b);
        this.f.mult(f);
    }

    @Override
    public Spectrum evaluateBRDF(HitRecord hitRecord, Vector3f wOut, Vector3f wIn) {
        return blinn.evaluateBRDF(hitRecord, wOut, wIn);
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
        // Using Schlick's approximation:
        // F = f + (1 - f) * (1 - w dot n)^5
        float nDotw = hitRecord.normal.dot(hitRecord.w);
        float x = 1 - nDotw;
        x = x * x * x * x * x;
        Spectrum r = new Spectrum(f);
        r.mult(-1);
        r.add(new Spectrum(1, 1, 1));
        r.mult(x);
        r.add(f);

        ShadingSample s = new ShadingSample();
        s.brdf = r;
        return s;
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
        return null;
    }

    @Override
    public boolean castsShadows() {
        return false;
    }
}
