package rt.materials;

import rt.HitRecord;
import rt.Material;
import rt.Spectrum;
import rt.textures.Texture;

import javax.vecmath.Vector3f;

/**
 * Created by Adrian on 15.03.2016.
 */
public class DiffuseTexture implements Material {

    Texture kd;

    public DiffuseTexture(Texture texture) {
        this.kd = texture;
    }

    @Override
    public Spectrum evaluateBRDF(HitRecord hitRecord, Vector3f wOut, Vector3f wIn) {
        Diffuse diffuse = new Diffuse(kd.lookUp(hitRecord.u, hitRecord.v));
        return diffuse.evaluateBRDF(hitRecord, wOut, wIn);
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
        return null;
    }

    @Override
    public boolean castsShadows() {
        return true;
    }

    @Override
    public float getProbability(HitRecord hitRecord, Vector3f direction)
    {
        return (new Diffuse()).getProbability(hitRecord, direction);
    }
}
