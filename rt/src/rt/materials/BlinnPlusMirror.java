package rt.materials;

import rt.HitRecord;
import rt.Material;
import rt.Ray;
import rt.Spectrum;

import javax.vecmath.Vector3f;

/**
 * Created by Adrian on 10.03.2016.
 */
public class BlinnPlusMirror implements Material {

    Blinn blinn;
    Spectrum reflectivity;

    public  BlinnPlusMirror(Spectrum kd, Spectrum ks, float shininess, Spectrum reflectivity) {
        blinn = new Blinn(kd, ks, shininess);
        this.reflectivity = reflectivity;
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
        Ray reflectedRay = Ray.reflect(hitRecord);
        ShadingSample s = new ShadingSample();
        s.brdf = new Spectrum(reflectivity);
        s.w = reflectedRay.direction;
        s.p = 1;
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
        return true;
    }
}
