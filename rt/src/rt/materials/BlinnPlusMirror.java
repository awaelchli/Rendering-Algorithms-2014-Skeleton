package rt.materials;

import rt.HitRecord;
import rt.Material;
import rt.Ray;
import rt.Spectrum;

import javax.vecmath.Vector3f;

/**
 * Created by Adrian on 10.03.2016.
 */
public class BlinnPlusMirror extends Mirror {

    Blinn blinn;

    public  BlinnPlusMirror(Spectrum kd, Spectrum ks, float shininess, Spectrum reflectivity) {
        super(reflectivity);
        blinn = new Blinn(kd, ks, shininess);
    }

    @Override
    public Spectrum evaluateBRDF(HitRecord hitRecord, Vector3f wOut, Vector3f wIn) {
        return blinn.evaluateBRDF(hitRecord, wOut, wIn);
    }

    @Override
    public float getProbability(HitRecord hitRecord, Vector3f direction)
    {
        // TODO: implement
        throw new UnsupportedOperationException();
    }
}
