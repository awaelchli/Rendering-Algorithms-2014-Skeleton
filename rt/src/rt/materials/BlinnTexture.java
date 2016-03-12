package rt.materials;

import rt.HitRecord;
import rt.Material;
import rt.Spectrum;
import rt.Texture;

import javax.vecmath.Point2f;
import javax.vecmath.Vector3f;

/**
 * Created by Adrian on 12.03.2016.
 */
public class BlinnTexture implements Material {

    Texture texture;

    Spectrum ks;

    float shininess;

    public BlinnTexture(Texture texture, Spectrum ks, float shininess) {
        this.texture = texture;
        this.ks = ks;
        this.shininess = shininess;
    }

    @Override
    public Spectrum evaluateBRDF(HitRecord hitRecord, Vector3f wOut, Vector3f wIn) {

        Spectrum kd = texture.lookUp(hitRecord.u, hitRecord.v);
        Blinn blinn = new Blinn(kd, ks, shininess);

        return blinn.evaluateBRDF(hitRecord, wOut, wIn);
    }

    @Override
    public Spectrum evaluateEmission(HitRecord hitRecord, Vector3f wOut) {
        return null;
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
        return null;
    }

    @Override
    public boolean castsShadows() {
        return true;
    }
}
