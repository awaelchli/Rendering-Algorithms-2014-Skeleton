package rt.materials;

import rt.HitRecord;
import rt.Material;
import rt.Spectrum;
import rt.Medium;
import rt.textures.NormalDisplacement;

import javax.vecmath.Vector3f;

/**
 * Created by Adrian on 13.03.2016.
 */
public class BumpMaterial implements Material {

    Material material;
    NormalDisplacement normalDisplacement;

    public BumpMaterial(Material material, NormalDisplacement displacementMap) {
        this.material = material;
        this.normalDisplacement = displacementMap;
    }

    @Override
    public Spectrum evaluateBRDF(HitRecord hitRecord, Vector3f wOut, Vector3f wIn) {
        normalDisplacement.bumpNormal(hitRecord);
        return material.evaluateBRDF(hitRecord, wOut, wIn);
    }

    @Override
    public Spectrum evaluateEmission(HitRecord hitRecord, Vector3f wOut) {
        return material.evaluateEmission(hitRecord, wOut);
    }

    @Override
    public boolean hasSpecularReflection() {
        return material.hasSpecularReflection();
    }

    @Override
    public ShadingSample evaluateSpecularReflection(HitRecord hitRecord) {
        return material.evaluateSpecularReflection(hitRecord);
    }

    @Override
    public boolean hasSpecularRefraction() {
        return material.hasSpecularRefraction();
    }

    @Override
    public ShadingSample evaluateSpecularRefraction(HitRecord hitRecord) {
        return material.evaluateSpecularRefraction(hitRecord);
    }

    @Override
    public ShadingSample getShadingSample(HitRecord hitRecord, float[] sample) {
        return material.getShadingSample(hitRecord, sample);
    }

    @Override
    public ShadingSample getEmissionSample(HitRecord hitRecord, float[] sample) {
        return material.getEmissionSample(hitRecord, sample);
    }

    @Override
    public boolean castsShadows() {
        return material.castsShadows();
    }

    @Override
    public float getProbability(HitRecord hitRecord, Vector3f direction)
    {
        return material.getProbability(hitRecord, direction);
    }

    @Override
    public Medium getMedium()
    {
        return null;
    }
}
