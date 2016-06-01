package rt.materials;

import rt.*;
import rt.Medium;

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

        Vector3f incident = new Vector3f(wIn);
        Vector3f normal = new Vector3f(hitRecord.normal);
        Vector3f out = new Vector3f(wOut);
        incident.normalize();
        normal.normalize();
        out.normalize();

        Vector3f h = new Vector3f();
        h.add(incident, out);
        h.normalize();

        float nDotl = normal.dot(incident);

        float hDotn = Math.max(h.dot(normal), 0);
        float hDotns = (float) Math.pow(hDotn, shininess);
        Spectrum brdf = new Spectrum(ks);
        brdf.mult(hDotns);

        brdf.mult(1 / nDotl);

        // Add constant diffuse term
        Spectrum kd = new Spectrum(this.kd);
        kd.mult(1 / (float) Math.PI);
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
        return new ShadingSample(new Spectrum(), new Spectrum(), new Vector3f(), false, 0);
    }

    @Override
    public ShadingSample getEmissionSample(HitRecord hitRecord, float[] sample) {
        return new ShadingSample();
    }

    @Override
    public boolean castsShadows() {
        return true;
    }

    @Override
    public float getProbability(HitRecord hitRecord, Vector3f direction)
    {
        Vector3f halfVector = new Vector3f(hitRecord.w);
        halfVector.add(direction);
        halfVector.normalize();

        float cosTheta = hitRecord.normal.dot(direction);
        float p_h = (float) ((shininess + 1) * Math.pow(cosTheta, shininess)/ (2 * Math.PI));
        float p_i = p_h / (4 * halfVector.dot(hitRecord.w));

        return p_i;
    }

    @Override
    public Medium getMedium()
    {
        return null;
    }
}
